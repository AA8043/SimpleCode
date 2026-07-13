#define WIN32_LEAN_AND_MEAN
#define NOMINMAX

#include <Windows.h>
#include <UIAutomation.h>
#include <jni.h>
#include <wrl/client.h>

#include <algorithm>
#include <cstdint>
#include <cwctype>
#include <sstream>
#include <stdexcept>
#include <string>
#include <vector>

#pragma comment(lib, "Ole32.lib")
#pragma comment(lib, "OleAut32.lib")
#pragma comment(lib, "Uiautomationcore.lib")

using Microsoft::WRL::ComPtr;

namespace {

constexpr int kMaxTreeDepth = 128;
constexpr size_t kMaxTreeNodes = 50000;

class ComInitializer {
public:
    ComInitializer() {
        hr_ = CoInitializeEx(nullptr, COINIT_MULTITHREADED);

        shouldUninitialize_ = (hr_ == S_OK || hr_ == S_FALSE);
    }

    ~ComInitializer() {
        if (shouldUninitialize_) {
            CoUninitialize();
        }
    }

    bool available() const {
        return SUCCEEDED(hr_) || hr_ == RPC_E_CHANGED_MODE;
    }

    HRESULT result() const {
        return hr_;
    }

private:
    HRESULT hr_ = E_FAIL;
    bool shouldUninitialize_ = false;
};

std::wstring jStringToWide(JNIEnv* env, jstring value) {
    if (value == nullptr) {
        return {};
    }

    const jchar* chars = env->GetStringChars(value, nullptr);
    if (chars == nullptr) {
        return {};
    }

    const jsize length = env->GetStringLength(value);

    std::wstring result(
        reinterpret_cast<const wchar_t*>(chars),
        static_cast<size_t>(length)
    );

    env->ReleaseStringChars(value, chars);
    return result;
}

std::string wideToUtf8(const std::wstring& value) {
    if (value.empty()) {
        return {};
    }

    int length = WideCharToMultiByte(
        CP_UTF8,
        WC_ERR_INVALID_CHARS,
        value.data(),
        static_cast<int>(value.size()),
        nullptr,
        0,
        nullptr,
        nullptr
    );

    if (length <= 0) {
        length = WideCharToMultiByte(
            CP_UTF8,
            0,
            value.data(),
            static_cast<int>(value.size()),
            nullptr,
            0,
            nullptr,
            nullptr
        );
    }

    if (length <= 0) {
        return {};
    }

    std::string result(static_cast<size_t>(length), '\0');

    WideCharToMultiByte(
        CP_UTF8,
        0,
        value.data(),
        static_cast<int>(value.size()),
        result.data(),
        length,
        nullptr,
        nullptr
    );

    return result;
}

std::wstring utf8ToWide(const std::string& value) {
    if (value.empty()) {
        return {};
    }

    int length = MultiByteToWideChar(
        CP_UTF8,
        MB_ERR_INVALID_CHARS,
        value.data(),
        static_cast<int>(value.size()),
        nullptr,
        0
    );

    if (length <= 0) {
        length = MultiByteToWideChar(
            CP_UTF8,
            0,
            value.data(),
            static_cast<int>(value.size()),
            nullptr,
            0
        );
    }

    if (length <= 0) {
        return {};
    }

    std::wstring result(static_cast<size_t>(length), L'\0');

    MultiByteToWideChar(
        CP_UTF8,
        0,
        value.data(),
        static_cast<int>(value.size()),
        result.data(),
        length
    );

    return result;
}

jstring utf8ToJString(JNIEnv* env, const std::string& value) {
    const std::wstring wide = utf8ToWide(value);

    return env->NewString(
        reinterpret_cast<const jchar*>(wide.data()),
        static_cast<jsize>(wide.size())
    );
}

std::wstring bstrToWide(BSTR value) {
    if (value == nullptr) {
        return {};
    }

    return std::wstring(value, SysStringLen(value));
}

std::string jsonEscape(const std::wstring& value) {
    const std::string utf8 = wideToUtf8(value);
    std::string result;
    result.reserve(utf8.size() + 16);

    static const char* hex = "0123456789abcdef";

    for (const unsigned char c : utf8) {
        switch (c) {
            case '"':
                result += "\\\"";
                break;
            case '\\':
                result += "\\\\";
                break;
            case '\b':
                result += "\\b";
                break;
            case '\f':
                result += "\\f";
                break;
            case '\n':
                result += "\\n";
                break;
            case '\r':
                result += "\\r";
                break;
            case '\t':
                result += "\\t";
                break;
            default:
                if (c < 0x20) {
                    result += "\\u00";
                    result += hex[(c >> 4) & 0x0F];
                    result += hex[c & 0x0F];
                } else {
                    result.push_back(static_cast<char>(c));
                }
                break;
        }
    }

    return result;
}

void appendJsonString(
    std::ostringstream& output,
    const char* key,
    const std::wstring& value,
    bool& first
) {
    if (!first) {
        output << ',';
    }

    first = false;
    output << '"' << key << "\":\"" << jsonEscape(value) << '"';
}

template<typename T>
void appendJsonNumber(
    std::ostringstream& output,
    const char* key,
    T value,
    bool& first
) {
    if (!first) {
        output << ',';
    }

    first = false;
    output << '"' << key << "\":" << value;
}

void appendJsonBoolean(
    std::ostringstream& output,
    const char* key,
    bool value,
    bool& first
) {
    if (!first) {
        output << ',';
    }

    first = false;
    output << '"' << key << "\":" << (value ? "true" : "false");
}

void throwJavaException(
    JNIEnv* env,
    const char* className,
    const std::string& message
) {
    jclass exceptionClass = env->FindClass(className);

    if (exceptionClass != nullptr) {
        env->ThrowNew(exceptionClass, message.c_str());
    }
}

void throwRuntimeException(JNIEnv* env, const std::string& message) {
    throwJavaException(env, "java/lang/RuntimeException", message);
}

jobject createJsonObject(
    JNIEnv* env,
    const char* className,
    const std::string& json
) {
    jclass jsonClass = env->FindClass(className);
    if (jsonClass == nullptr) {
        return nullptr;
    }

    jmethodID constructor = env->GetMethodID(
        jsonClass,
        "<init>",
        "(Ljava/lang/String;)V"
    );

    if (constructor == nullptr) {
        return nullptr;
    }

    jstring jsonString = utf8ToJString(env, json);
    if (jsonString == nullptr) {
        return nullptr;
    }

    jobject result = env->NewObject(
        jsonClass,
        constructor,
        jsonString
    );

    env->DeleteLocalRef(jsonString);
    env->DeleteLocalRef(jsonClass);

    return result;
}

jobject createJsonObject(JNIEnv* env, const std::string& json) {
    jclass utilClass = env->FindClass("cn/hutool/json/JSONUtil");

    jmethodID parseObj = env->GetStaticMethodID(
        utilClass,
        "parseObj",
        "(Ljava/lang/String;)Lcn/hutool/json/JSONObject;"
    );

    jstring jJson = env->NewStringUTF(json.c_str());
    jobject obj = env->CallStaticObjectMethod(utilClass, parseObj, jJson);

    env->DeleteLocalRef(jJson);
    env->DeleteLocalRef(utilClass);

    return obj;
}

jobject createJsonArray(JNIEnv* env, const std::string& json) {
    jclass utilClass = env->FindClass("cn/hutool/json/JSONUtil");

    jmethodID parseArray = env->GetStaticMethodID(
        utilClass,
        "parseArray",
        "(Ljava/lang/String;)Lcn/hutool/json/JSONArray;"
    );

    jstring jJson = env->NewStringUTF(json.c_str());
    jobject obj = env->CallStaticObjectMethod(utilClass, parseArray, jJson);

    env->DeleteLocalRef(jJson);
    env->DeleteLocalRef(utilClass);

    return obj;
}

ComPtr<IUIAutomation> createAutomation() {
    ComPtr<IUIAutomation> automation;

    const HRESULT hr = CoCreateInstance(
        CLSID_CUIAutomation,
        nullptr,
        CLSCTX_INPROC_SERVER,
        __uuidof(IUIAutomation),
        reinterpret_cast<void**>(automation.GetAddressOf())
    );

    if (FAILED(hr)) {
        return nullptr;
    }

    return automation;
}

std::wstring getElementName(IUIAutomationElement* element) {
    if (element == nullptr) {
        return {};
    }

    BSTR value = nullptr;
    const HRESULT hr = element->get_CurrentName(&value);

    if (FAILED(hr) || value == nullptr) {
        return {};
    }

    const std::wstring result = bstrToWide(value);
    SysFreeString(value);
    return result;
}

std::wstring getElementAutomationId(IUIAutomationElement* element) {
    if (element == nullptr) {
        return {};
    }

    BSTR value = nullptr;
    const HRESULT hr = element->get_CurrentAutomationId(&value);

    if (FAILED(hr) || value == nullptr) {
        return {};
    }

    const std::wstring result = bstrToWide(value);
    SysFreeString(value);
    return result;
}

std::wstring getElementClassName(IUIAutomationElement* element) {
    if (element == nullptr) {
        return {};
    }

    BSTR value = nullptr;
    const HRESULT hr = element->get_CurrentClassName(&value);

    if (FAILED(hr) || value == nullptr) {
        return {};
    }

    const std::wstring result = bstrToWide(value);
    SysFreeString(value);
    return result;
}

std::wstring getElementFrameworkId(IUIAutomationElement* element) {
    if (element == nullptr) {
        return {};
    }

    BSTR value = nullptr;
    const HRESULT hr = element->get_CurrentFrameworkId(&value);

    if (FAILED(hr) || value == nullptr) {
        return {};
    }

    const std::wstring result = bstrToWide(value);
    SysFreeString(value);
    return result;
}

std::wstring controlTypeToString(CONTROLTYPEID type) {
    switch (type) {
        case UIA_ButtonControlTypeId:
            return L"Button";
        case UIA_CalendarControlTypeId:
            return L"Calendar";
        case UIA_CheckBoxControlTypeId:
            return L"CheckBox";
        case UIA_ComboBoxControlTypeId:
            return L"ComboBox";
        case UIA_EditControlTypeId:
            return L"Edit";
        case UIA_HyperlinkControlTypeId:
            return L"Hyperlink";
        case UIA_ImageControlTypeId:
            return L"Image";
        case UIA_ListItemControlTypeId:
            return L"ListItem";
        case UIA_ListControlTypeId:
            return L"List";
        case UIA_MenuControlTypeId:
            return L"Menu";
        case UIA_MenuBarControlTypeId:
            return L"MenuBar";
        case UIA_MenuItemControlTypeId:
            return L"MenuItem";
        case UIA_ProgressBarControlTypeId:
            return L"ProgressBar";
        case UIA_RadioButtonControlTypeId:
            return L"RadioButton";
        case UIA_ScrollBarControlTypeId:
            return L"ScrollBar";
        case UIA_SliderControlTypeId:
            return L"Slider";
        case UIA_SpinnerControlTypeId:
            return L"Spinner";
        case UIA_StatusBarControlTypeId:
            return L"StatusBar";
        case UIA_TabControlTypeId:
            return L"Tab";
        case UIA_TabItemControlTypeId:
            return L"TabItem";
        case UIA_TextControlTypeId:
            return L"Text";
        case UIA_ToolBarControlTypeId:
            return L"ToolBar";
        case UIA_ToolTipControlTypeId:
            return L"ToolTip";
        case UIA_TreeControlTypeId:
            return L"Tree";
        case UIA_TreeItemControlTypeId:
            return L"TreeItem";
        case UIA_CustomControlTypeId:
            return L"Custom";
        case UIA_GroupControlTypeId:
            return L"Group";
        case UIA_ThumbControlTypeId:
            return L"Thumb";
        case UIA_DataGridControlTypeId:
            return L"DataGrid";
        case UIA_DataItemControlTypeId:
            return L"DataItem";
        case UIA_DocumentControlTypeId:
            return L"Document";
        case UIA_SplitButtonControlTypeId:
            return L"SplitButton";
        case UIA_WindowControlTypeId:
            return L"Window";
        case UIA_PaneControlTypeId:
            return L"Pane";
        case UIA_HeaderControlTypeId:
            return L"Header";
        case UIA_HeaderItemControlTypeId:
            return L"HeaderItem";
        case UIA_TableControlTypeId:
            return L"Table";
        case UIA_TitleBarControlTypeId:
            return L"TitleBar";
        case UIA_SeparatorControlTypeId:
            return L"Separator";
        default:
            return L"Unknown";
    }
}

std::wstring getRuntimeId(IUIAutomationElement* element) {
    if (element == nullptr) {
        return {};
    }

    SAFEARRAY* runtimeId = nullptr;
    const HRESULT hr = element->GetRuntimeId(&runtimeId);

    if (FAILED(hr) || runtimeId == nullptr) {
        return {};
    }

    LONG lowerBound = 0;
    LONG upperBound = -1;

    if (FAILED(SafeArrayGetLBound(runtimeId, 1, &lowerBound)) ||
        FAILED(SafeArrayGetUBound(runtimeId, 1, &upperBound))) {
        SafeArrayDestroy(runtimeId);
        return {};
    }

    std::wostringstream output;
    output << L"rid:";

    bool first = true;

    for (LONG i = lowerBound; i <= upperBound; ++i) {
        int value = 0;

        if (FAILED(SafeArrayGetElement(runtimeId, &i, &value))) {
            continue;
        }

        if (!first) {
            output << L',';
        }

        first = false;
        output << value;
    }

    SafeArrayDestroy(runtimeId);
    return output.str();
}

bool runtimeIdEquals(
    IUIAutomationElement* element,
    const std::wstring& expectedId
) {
    return getRuntimeId(element) == expectedId;
}

ComPtr<IUIAutomationElement> findElementById(
    IUIAutomation* automation,
    const std::wstring& id
) {
    if (automation == nullptr || id.empty()) {
        return nullptr;
    }

    ComPtr<IUIAutomationElement> root;
    HRESULT hr = automation->GetRootElement(&root);

    if (FAILED(hr) || root == nullptr) {
        return nullptr;
    }

    if (runtimeIdEquals(root.Get(), id)) {
        return root;
    }

    ComPtr<IUIAutomationCondition> trueCondition;
    hr = automation->CreateTrueCondition(&trueCondition);

    if (FAILED(hr) || trueCondition == nullptr) {
        return nullptr;
    }

    ComPtr<IUIAutomationElementArray> elements;
    hr = root->FindAll(
        TreeScope_Descendants,
        trueCondition.Get(),
        &elements
    );

    if (FAILED(hr) || elements == nullptr) {
        return nullptr;
    }

    int count = 0;
    elements->get_Length(&count);

    for (int i = 0; i < count; ++i) {
        ComPtr<IUIAutomationElement> element;

        if (FAILED(elements->GetElement(i, &element)) ||
            element == nullptr) {
            continue;
        }

        if (runtimeIdEquals(element.Get(), id)) {
            return element;
        }
    }

    return nullptr;
}

void appendTreeElementProperties(
    std::ostringstream& output,
    IUIAutomationElement* element,
    bool& first
) {
    const std::wstring runtimeId = getRuntimeId(element);
    const std::wstring name = getElementName(element);

    appendJsonString(output, "id", runtimeId, first);
    appendJsonString(output, "name", name, first);
}

void appendElementProperties(
    std::ostringstream& output,
    IUIAutomationElement* element,
    bool& first
) {
    const std::wstring runtimeId = getRuntimeId(element);
    const std::wstring name = getElementName(element);
    const std::wstring automationId = getElementAutomationId(element);
    const std::wstring className = getElementClassName(element);
    const std::wstring frameworkId = getElementFrameworkId(element);

    CONTROLTYPEID controlType = 0;
    int processId = 0;
    UIA_HWND nativeHandle = nullptr;
    BOOL enabled = FALSE;
    BOOL offscreen = FALSE;
    BOOL keyboardFocusable = FALSE;
    BOOL hasKeyboardFocus = FALSE;
    RECT bounds{};

    element->get_CurrentControlType(&controlType);
    element->get_CurrentProcessId(&processId);
    element->get_CurrentNativeWindowHandle(&nativeHandle);
    element->get_CurrentIsEnabled(&enabled);
    element->get_CurrentIsOffscreen(&offscreen);
    element->get_CurrentIsKeyboardFocusable(&keyboardFocusable);
    element->get_CurrentHasKeyboardFocus(&hasKeyboardFocus);
    element->get_CurrentBoundingRectangle(&bounds);

    appendJsonString(output, "id", runtimeId, first);
    appendJsonString(output, "name", name, first);
    appendJsonString(output, "automationId", automationId, first);
    appendJsonString(output, "className", className, first);
    appendJsonString(output, "frameworkId", frameworkId, first);
    appendJsonString(output, "controlType", controlTypeToString(controlType), first);
    appendJsonNumber(output, "controlTypeId", controlType, first);
    appendJsonNumber(output, "processId", processId, first);

    const auto handleValue = static_cast<unsigned long long>(
        reinterpret_cast<std::uintptr_t>(nativeHandle)
    );
    appendJsonNumber(output, "nativeHandle", handleValue, first);
    appendJsonBoolean(output, "enabled", enabled == TRUE, first);
    appendJsonBoolean(output, "offscreen", offscreen == TRUE, first);
    appendJsonBoolean(output, "keyboardFocusable", keyboardFocusable == TRUE, first);
    appendJsonBoolean(output, "hasKeyboardFocus", hasKeyboardFocus == TRUE, first);

    if (!first) {
        output << ',';
    }
    first = false;
    output
        << "\"bounds\":{"
        << "\"left\":" << bounds.left << ','
        << "\"top\":" << bounds.top << ','
        << "\"right\":" << bounds.right << ','
        << "\"bottom\":" << bounds.bottom << ','
        << "\"width\":" << std::max<LONG>(0, bounds.right - bounds.left) << ','
        << "\"height\":" << std::max<LONG>(0, bounds.bottom - bounds.top)
        << '}';
}

void serializeElementTree(
    std::ostringstream& output,
    IUIAutomationElement* element,
    IUIAutomationTreeWalker* walker,
    int depth,
    size_t& nodeCount
) {
    output << '{';

    bool first = true;
    appendTreeElementProperties(output, element, first);

    ++nodeCount;

    if (!first) {
        output << ',';
    }

    output << "\"children\":[";

    bool firstChild = true;

    if (depth >= kMaxTreeDepth || nodeCount >= kMaxTreeNodes) {
    } else {
        ComPtr<IUIAutomationElement> child;
        HRESULT hr = walker->GetFirstChildElement(element, &child);

        while (SUCCEEDED(hr) && child != nullptr) {
            if (nodeCount >= kMaxTreeNodes) {
                break;
            }

            if (!firstChild) {
                output << ',';
            }

            firstChild = false;

            serializeElementTree(
                output,
                child.Get(),
                walker,
                depth + 1,
                nodeCount
            );

            ComPtr<IUIAutomationElement> next;
            hr = walker->GetNextSiblingElement(child.Get(), &next);
            child = next;
        }
    }

    output << ']';

    output << '}';
}

std::string elementToJson(IUIAutomationElement* element) {
    std::ostringstream output;
    output << '{';

    bool first = true;
    appendElementProperties(output, element, first);

    output << '}';
    return output.str();
}

std::string buildWindowTreeJson(
    IUIAutomation* automation,
    IUIAutomationElement* windowElement
) {
    ComPtr<IUIAutomationTreeWalker> walker;

    HRESULT hr = automation->get_ControlViewWalker(&walker);

    if (FAILED(hr) || walker == nullptr) {
        throw std::runtime_error("Cannot get ControlViewWalker");
    }

    size_t nodeCount = 0;
    std::ostringstream output;

    serializeElementTree(
        output,
        windowElement,
        walker.Get(),
        0,
        nodeCount
    );

    return output.str();
}

bool invokeElement(IUIAutomationElement* element) {
    if (element == nullptr) {
        return false;
    }

    ComPtr<IUIAutomationInvokePattern> invokePattern;

    HRESULT hr = element->GetCurrentPatternAs(
        UIA_InvokePatternId,
        __uuidof(IUIAutomationInvokePattern),
        reinterpret_cast<void**>(invokePattern.GetAddressOf())
    );

    if (SUCCEEDED(hr) && invokePattern != nullptr) {
        hr = invokePattern->Invoke();

        if (SUCCEEDED(hr)) {
            return true;
        }
    }

    ComPtr<IUIAutomationLegacyIAccessiblePattern> legacyPattern;

    hr = element->GetCurrentPatternAs(
        UIA_LegacyIAccessiblePatternId,
        __uuidof(IUIAutomationLegacyIAccessiblePattern),
        reinterpret_cast<void**>(legacyPattern.GetAddressOf())
    );

    if (SUCCEEDED(hr) && legacyPattern != nullptr) {
        hr = legacyPattern->DoDefaultAction();

        if (SUCCEEDED(hr)) {
            return true;
        }
    }

    return false;
}

bool setElementText(
    IUIAutomationElement* element,
    const std::wstring& text
) {
    if (element == nullptr) {
        return false;
    }

    element->SetFocus();

    ComPtr<IUIAutomationValuePattern> valuePattern;

    HRESULT hr = element->GetCurrentPatternAs(
        UIA_ValuePatternId,
        __uuidof(IUIAutomationValuePattern),
        reinterpret_cast<void**>(valuePattern.GetAddressOf())
    );

    if (SUCCEEDED(hr) && valuePattern != nullptr) {
        BOOL readOnly = TRUE;

        if (SUCCEEDED(valuePattern->get_CurrentIsReadOnly(&readOnly)) &&
            readOnly == FALSE) {
            BSTR bstrText = SysAllocString(text.c_str());
            if (bstrText == nullptr) {
                return false;
            }

            hr = valuePattern->SetValue(bstrText);
            SysFreeString(bstrText);

            if (SUCCEEDED(hr)) {
                return true;
            }
        }
    }

    ComPtr<IUIAutomationLegacyIAccessiblePattern> legacyPattern;

    hr = element->GetCurrentPatternAs(
        UIA_LegacyIAccessiblePatternId,
        __uuidof(IUIAutomationLegacyIAccessiblePattern),
        reinterpret_cast<void**>(legacyPattern.GetAddressOf())
    );

    if (SUCCEEDED(hr) && legacyPattern != nullptr) {
        hr = legacyPattern->SetValue(text.c_str());

        if (SUCCEEDED(hr)) {
            return true;
        }
    }

    return false;
}

bool activateWindowElement(IUIAutomationElement* element) {
    if (element == nullptr) {
        return false;
    }

    UIA_HWND nativeHandle = nullptr;
    element->get_CurrentNativeWindowHandle(&nativeHandle);
    const HWND windowHandle = reinterpret_cast<HWND>(nativeHandle);

    if (windowHandle != nullptr && IsIconic(windowHandle)) {
        ShowWindow(windowHandle, SW_RESTORE);
    }

    const bool focused = SUCCEEDED(element->SetFocus());
    const bool foreground = windowHandle != nullptr &&
        SetForegroundWindow(windowHandle) != FALSE;

    return focused || foreground;
}

bool closeWindowElement(IUIAutomationElement* element) {
    if (element == nullptr) {
        return false;
    }

    ComPtr<IUIAutomationWindowPattern> windowPattern;
    const HRESULT hr = element->GetCurrentPatternAs(
        UIA_WindowPatternId,
        __uuidof(IUIAutomationWindowPattern),
        reinterpret_cast<void**>(windowPattern.GetAddressOf())
    );

    return SUCCEEDED(hr) && windowPattern != nullptr &&
        SUCCEEDED(windowPattern->Close());
}

bool clickElement(IUIAutomationElement* element) {
    if (invokeElement(element)) {
        return true;
    }

    ComPtr<IUIAutomationSelectionItemPattern> selectionPattern;
    HRESULT hr = element->GetCurrentPatternAs(
        UIA_SelectionItemPatternId,
        __uuidof(IUIAutomationSelectionItemPattern),
        reinterpret_cast<void**>(selectionPattern.GetAddressOf())
    );

    if (SUCCEEDED(hr) && selectionPattern != nullptr &&
        SUCCEEDED(selectionPattern->Select())) {
        return true;
    }

    ComPtr<IUIAutomationTogglePattern> togglePattern;
    hr = element->GetCurrentPatternAs(
        UIA_TogglePatternId,
        __uuidof(IUIAutomationTogglePattern),
        reinterpret_cast<void**>(togglePattern.GetAddressOf())
    );

    return SUCCEEDED(hr) && togglePattern != nullptr &&
        SUCCEEDED(togglePattern->Toggle());
}

bool setElementChecked(IUIAutomationElement* element, bool checked) {
    if (element == nullptr) {
        return false;
    }

    ComPtr<IUIAutomationTogglePattern> togglePattern;
    HRESULT hr = element->GetCurrentPatternAs(
        UIA_TogglePatternId,
        __uuidof(IUIAutomationTogglePattern),
        reinterpret_cast<void**>(togglePattern.GetAddressOf())
    );

    if (FAILED(hr) || togglePattern == nullptr) {
        return false;
    }

    for (int attempt = 0; attempt < 3; ++attempt) {
        ToggleState state = ToggleState_Indeterminate;
        if (FAILED(togglePattern->get_CurrentToggleState(&state))) {
            return false;
        }

        if ((state == ToggleState_On) == checked) {
            return true;
        }

        if (FAILED(togglePattern->Toggle())) {
            return false;
        }
    }

    ToggleState state = ToggleState_Indeterminate;
    return SUCCEEDED(togglePattern->get_CurrentToggleState(&state)) &&
        ((state == ToggleState_On) == checked);
}

bool selectElement(IUIAutomationElement* element) {
    if (element == nullptr) {
        return false;
    }

    ComPtr<IUIAutomationSelectionItemPattern> selectionPattern;
    const HRESULT hr = element->GetCurrentPatternAs(
        UIA_SelectionItemPatternId,
        __uuidof(IUIAutomationSelectionItemPattern),
        reinterpret_cast<void**>(selectionPattern.GetAddressOf())
    );

    return SUCCEEDED(hr) && selectionPattern != nullptr &&
        SUCCEEDED(selectionPattern->Select());
}

ScrollAmount toScrollAmount(int amount) {
    if (amount < 0) {
        return ScrollAmount_SmallDecrement;
    }
    if (amount > 0) {
        return ScrollAmount_SmallIncrement;
    }
    return ScrollAmount_NoAmount;
}

bool scrollElement(
    IUIAutomationElement* element,
    int horizontalAmount,
    int verticalAmount
) {
    if (element == nullptr || horizontalAmount < -1 || horizontalAmount > 1 ||
        verticalAmount < -1 || verticalAmount > 1 ||
        (horizontalAmount == 0 && verticalAmount == 0)) {
        return false;
    }

    ComPtr<IUIAutomationScrollPattern> scrollPattern;
    const HRESULT hr = element->GetCurrentPatternAs(
        UIA_ScrollPatternId,
        __uuidof(IUIAutomationScrollPattern),
        reinterpret_cast<void**>(scrollPattern.GetAddressOf())
    );

    return SUCCEEDED(hr) && scrollPattern != nullptr && SUCCEEDED(
        scrollPattern->Scroll(
            toScrollAmount(horizontalAmount),
            toScrollAmount(verticalAmount)
        )
    );
}

WORD virtualKeyForName(const std::wstring& name) {
    if (name.size() == 1) {
        const wchar_t character = name[0];
        if ((character >= L'A' && character <= L'Z') ||
            (character >= L'0' && character <= L'9')) {
            return static_cast<WORD>(character);
        }
    }

    if (name == L"ENTER") return VK_RETURN;
    if (name == L"TAB") return VK_TAB;
    if (name == L"ESC" || name == L"ESCAPE") return VK_ESCAPE;
    if (name == L"SPACE") return VK_SPACE;
    if (name == L"BACKSPACE") return VK_BACK;
    if (name == L"DELETE" || name == L"DEL") return VK_DELETE;
    if (name == L"INSERT" || name == L"INS") return VK_INSERT;
    if (name == L"HOME") return VK_HOME;
    if (name == L"END") return VK_END;
    if (name == L"PAGEUP" || name == L"PGUP") return VK_PRIOR;
    if (name == L"PAGEDOWN" || name == L"PGDN") return VK_NEXT;
    if (name == L"LEFT") return VK_LEFT;
    if (name == L"RIGHT") return VK_RIGHT;
    if (name == L"UP") return VK_UP;
    if (name == L"DOWN") return VK_DOWN;

    if (name.size() == 2 && name[0] == L'F' &&
        name[1] >= L'1' && name[1] <= L'9') {
        return static_cast<WORD>(VK_F1 + name[1] - L'1');
    }
    if (name.size() == 3 && name[0] == L'F' && name[1] == L'1' &&
        name[2] >= L'0' && name[2] <= L'2') {
        return static_cast<WORD>(VK_F10 + name[2] - L'0');
    }

    return 0;
}

bool pressKeyCombination(const std::wstring& value) {
    std::wstring normalized;
    normalized.reserve(value.size());

    for (const wchar_t character : value) {
        if (!std::iswspace(character)) {
            normalized.push_back(static_cast<wchar_t>(std::towupper(character)));
        }
    }

    if (normalized.empty()) {
        return false;
    }

    std::vector<std::wstring> parts;
    size_t start = 0;
    while (start <= normalized.size()) {
        const size_t end = normalized.find(L'+', start);
        const std::wstring part = normalized.substr(start, end - start);
        if (part.empty()) {
            return false;
        }
        parts.push_back(part);
        if (end == std::wstring::npos) {
            break;
        }
        start = end + 1;
    }

    std::vector<WORD> modifiers;
    WORD key = 0;
    for (size_t index = 0; index < parts.size(); ++index) {
        const std::wstring& part = parts[index];
        if (index + 1 < parts.size()) {
            WORD modifier = 0;
            if (part == L"CTRL" || part == L"CONTROL") modifier = VK_CONTROL;
            if (part == L"ALT") modifier = VK_MENU;
            if (part == L"SHIFT") modifier = VK_SHIFT;
            if (modifier == 0 || std::find(modifiers.begin(), modifiers.end(), modifier) != modifiers.end()) {
                return false;
            }
            modifiers.push_back(modifier);
        } else {
            key = virtualKeyForName(part);
        }
    }

    if (key == 0) {
        return false;
    }

    std::vector<INPUT> inputs;
    inputs.reserve(modifiers.size() * 2 + 2);
    const auto appendKey = [&inputs](WORD virtualKey, bool keyUp) {
        INPUT input{};
        input.type = INPUT_KEYBOARD;
        input.ki.wVk = virtualKey;
        input.ki.dwFlags = keyUp ? KEYEVENTF_KEYUP : 0;
        inputs.push_back(input);
    };

    for (const WORD modifier : modifiers) appendKey(modifier, false);
    appendKey(key, false);
    appendKey(key, true);
    for (auto iterator = modifiers.rbegin(); iterator != modifiers.rend(); ++iterator) {
        appendKey(*iterator, true);
    }

    return SendInput(static_cast<UINT>(inputs.size()), inputs.data(), sizeof(INPUT)) ==
        inputs.size();
}

ComPtr<IUIAutomationElement> findActionElement(
    JNIEnv* env,
    jstring id,
    ComPtr<IUIAutomation>& automation
) {
    if (id == nullptr) {
        throwJavaException(env, "java/lang/IllegalArgumentException", "ID cannot be null");
        return nullptr;
    }

    const std::wstring elementId = jStringToWide(env, id);
    if (elementId.empty()) {
        throwJavaException(env, "java/lang/IllegalArgumentException", "ID cannot be empty");
        return nullptr;
    }

    automation = createAutomation();
    if (automation == nullptr) {
        throwRuntimeException(env, "Failed to create IUIAutomation instance");
        return nullptr;
    }

    return findElementById(automation.Get(), elementId);
}

}

extern "C" {

JNIEXPORT jobject JNICALL
Java_org_a8043_simpleCode_tools_windows_Native_getAllWindows(
    JNIEnv* env,
    jclass
) {
    ComInitializer com;

    if (!com.available()) {
        throwRuntimeException(env, "Failed to initialize COM");
        return nullptr;
    }

    ComPtr<IUIAutomation> automation = createAutomation();

    if (automation == nullptr) {
        throwRuntimeException(env, "Failed to create IUIAutomation instance");
        return nullptr;
    }

    ComPtr<IUIAutomationElement> root;
    HRESULT hr = automation->GetRootElement(&root);

    if (FAILED(hr) || root == nullptr) {
        throwRuntimeException(env, "Failed to get root element");
        return nullptr;
    }

    VARIANT controlTypeValue;
    VariantInit(&controlTypeValue);

    controlTypeValue.vt = VT_I4;
    controlTypeValue.lVal = UIA_WindowControlTypeId;

    ComPtr<IUIAutomationCondition> windowCondition;

    hr = automation->CreatePropertyCondition(
        UIA_ControlTypePropertyId,
        controlTypeValue,
        &windowCondition
    );

    VariantClear(&controlTypeValue);

    if (FAILED(hr) || windowCondition == nullptr) {
        throwRuntimeException(env, "Failed to create window condition");
        return nullptr;
    }

    ComPtr<IUIAutomationElementArray> windows;

    hr = root->FindAll(
        TreeScope_Children,
        windowCondition.Get(),
        &windows
    );

    if (FAILED(hr) || windows == nullptr) {
        throwRuntimeException(env, "Failed to find window elements");
        return nullptr;
    }

    int count = 0;
    windows->get_Length(&count);

    std::ostringstream output;
    output << '[';

    bool first = true;

    for (int i = 0; i < count; ++i) {
        ComPtr<IUIAutomationElement> window;

        if (FAILED(windows->GetElement(i, &window)) ||
            window == nullptr) {
            continue;
        }

        if (!first) {
            output << ',';
        }

        first = false;
        output << elementToJson(window.Get());
    }

    output << ']';

    return createJsonArray(env, output.str());
}

JNIEXPORT jobject JNICALL
Java_org_a8043_simpleCode_tools_windows_Native_getWindowTree(
    JNIEnv* env,
    jclass,
    jstring id
) {
    if (id == nullptr) {
        throwJavaException(
            env,
            "java/lang/IllegalArgumentException",
            "ID cannot be null"
        );
        return nullptr;
    }

    const std::wstring elementId = jStringToWide(env, id);

    if (elementId.empty()) {
        throwJavaException(
            env,
            "java/lang/IllegalArgumentException",
            "ID cannot be empty"
        );
        return nullptr;
    }

    ComInitializer com;

    if (!com.available()) {
        throwRuntimeException(env, "Failed to initialize COM");
        return nullptr;
    }

    ComPtr<IUIAutomation> automation = createAutomation();

    if (automation == nullptr) {
        throwRuntimeException(env, "Failed to create IUIAutomation instance");
        return nullptr;
    }

    ComPtr<IUIAutomationElement> window =
        findElementById(automation.Get(), elementId);

    if (window == nullptr) {
        throwJavaException(
            env,
            "java/lang/IllegalArgumentException",
            "Failed to find the specified window or control, the RuntimeId may have expired"
        );
        return nullptr;
    }

    try {
        const std::string json = buildWindowTreeJson(
            automation.Get(),
            window.Get()
        );

        return createJsonObject(env, json);
    } catch (const std::exception& exception) {
        throwRuntimeException(env, exception.what());
        return nullptr;
    }
}

JNIEXPORT jboolean JNICALL
Java_org_a8043_simpleCode_tools_windows_Native_clickButton(
    JNIEnv* env,
    jclass,
    jstring id
) {
    if (id == nullptr) {
        throwJavaException(
            env,
            "java/lang/IllegalArgumentException",
            "ID cannot be null"
        );
        return JNI_FALSE;
    }

    const std::wstring elementId = jStringToWide(env, id);

    if (elementId.empty()) {
        throwJavaException(
            env,
            "java/lang/IllegalArgumentException",
            "ID cannot be empty"
        );
        return JNI_FALSE;
    }

    ComInitializer com;

    if (!com.available()) {
        throwRuntimeException(env, "Failed to initialize COM");
        return JNI_FALSE;
    }

    ComPtr<IUIAutomation> automation = createAutomation();

    if (automation == nullptr) {
        throwRuntimeException(env, "Failed to create IUIAutomation instance");
        return JNI_FALSE;
    }

    ComPtr<IUIAutomationElement> element =
        findElementById(automation.Get(), elementId);

    if (element == nullptr) {
        return JNI_FALSE;
    }

    return clickElement(element.Get()) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_org_a8043_simpleCode_tools_windows_Native_clickElement(
    JNIEnv* env,
    jclass,
    jstring id
) {
    ComInitializer com;

    if (!com.available()) {
        throwRuntimeException(env, "Failed to initialize COM");
        return JNI_FALSE;
    }

    ComPtr<IUIAutomation> automation;
    ComPtr<IUIAutomationElement> element = findActionElement(env, id, automation);
    if (element == nullptr) {
        return JNI_FALSE;
    }

    return clickElement(element.Get()) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_org_a8043_simpleCode_tools_windows_Native_inputText(
    JNIEnv* env,
    jclass,
    jstring id,
    jstring text
) {
    if (id == nullptr) {
        throwJavaException(
            env,
            "java/lang/IllegalArgumentException",
            "ID cannot be null"
        );
        return JNI_FALSE;
    }

    if (text == nullptr) {
        throwJavaException(
            env,
            "java/lang/IllegalArgumentException",
            "Text cannot be null"
        );
        return JNI_FALSE;
    }

    const std::wstring elementId = jStringToWide(env, id);
    const std::wstring inputValue = jStringToWide(env, text);

    if (elementId.empty()) {
        throwJavaException(
            env,
            "java/lang/IllegalArgumentException",
            "ID cannot be empty"
        );
        return JNI_FALSE;
    }

    ComInitializer com;

    if (!com.available()) {
        throwRuntimeException(env, "Failed to initialize COM");
        return JNI_FALSE;
    }

    ComPtr<IUIAutomation> automation = createAutomation();

    if (automation == nullptr) {
        throwRuntimeException(env, "Failed to create IUIAutomation instance");
        return JNI_FALSE;
    }

    ComPtr<IUIAutomationElement> element =
        findElementById(automation.Get(), elementId);

    if (element == nullptr) {
        return JNI_FALSE;
    }

    return setElementText(element.Get(), inputValue)
        ? JNI_TRUE
        : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_org_a8043_simpleCode_tools_windows_Native_activateWindow(
    JNIEnv* env,
    jclass,
    jstring id
) {
    ComInitializer com;

    if (!com.available()) {
        throwRuntimeException(env, "Failed to initialize COM");
        return JNI_FALSE;
    }

    ComPtr<IUIAutomation> automation;
    ComPtr<IUIAutomationElement> element = findActionElement(env, id, automation);
    if (element == nullptr) {
        return JNI_FALSE;
    }

    return activateWindowElement(element.Get()) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_org_a8043_simpleCode_tools_windows_Native_closeWindow(
    JNIEnv* env,
    jclass,
    jstring id
) {
    ComInitializer com;

    if (!com.available()) {
        throwRuntimeException(env, "Failed to initialize COM");
        return JNI_FALSE;
    }

    ComPtr<IUIAutomation> automation;
    ComPtr<IUIAutomationElement> element = findActionElement(env, id, automation);
    if (element == nullptr) {
        return JNI_FALSE;
    }

    return closeWindowElement(element.Get()) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_org_a8043_simpleCode_tools_windows_Native_setChecked(
    JNIEnv* env,
    jclass,
    jstring id,
    jboolean checked
) {
    ComInitializer com;

    if (!com.available()) {
        throwRuntimeException(env, "Failed to initialize COM");
        return JNI_FALSE;
    }

    ComPtr<IUIAutomation> automation;
    ComPtr<IUIAutomationElement> element = findActionElement(env, id, automation);
    if (element == nullptr) {
        return JNI_FALSE;
    }

    return setElementChecked(element.Get(), checked == JNI_TRUE)
        ? JNI_TRUE
        : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_org_a8043_simpleCode_tools_windows_Native_selectElement(
    JNIEnv* env,
    jclass,
    jstring id
) {
    ComInitializer com;

    if (!com.available()) {
        throwRuntimeException(env, "Failed to initialize COM");
        return JNI_FALSE;
    }

    ComPtr<IUIAutomation> automation;
    ComPtr<IUIAutomationElement> element = findActionElement(env, id, automation);
    if (element == nullptr) {
        return JNI_FALSE;
    }

    return selectElement(element.Get()) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_org_a8043_simpleCode_tools_windows_Native_scrollElement(
    JNIEnv* env,
    jclass,
    jstring id,
    jint horizontalAmount,
    jint verticalAmount
) {
    ComInitializer com;

    if (!com.available()) {
        throwRuntimeException(env, "Failed to initialize COM");
        return JNI_FALSE;
    }

    ComPtr<IUIAutomation> automation;
    ComPtr<IUIAutomationElement> element = findActionElement(env, id, automation);
    if (element == nullptr) {
        return JNI_FALSE;
    }

    return scrollElement(element.Get(), horizontalAmount, verticalAmount)
        ? JNI_TRUE
        : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_org_a8043_simpleCode_tools_windows_Native_pressKey(
    JNIEnv* env,
    jclass,
    jstring keyCombination
) {
    if (keyCombination == nullptr) {
        throwJavaException(env, "java/lang/IllegalArgumentException", "Key combination cannot be null");
        return JNI_FALSE;
    }

    return pressKeyCombination(jStringToWide(env, keyCombination))
        ? JNI_TRUE
        : JNI_FALSE;
}

}
