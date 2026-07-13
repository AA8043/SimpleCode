package org.a8043.simpleCode.tools.windows;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

public class Native {
    public static native JSONArray getAllWindows();

    public static native JSONObject getWindowTree(String windowId);

    public static native boolean clickButton(String buttonId);

    public static native boolean clickElement(String elementId);

    public static native boolean inputText(String inputId, String text);

    public static native boolean activateWindow(String windowId);

    public static native boolean closeWindow(String windowId);

    public static native boolean setChecked(String elementId, boolean checked);

    public static native boolean selectElement(String elementId);

    public static native boolean scrollElement(String elementId, int horizontalAmount, int verticalAmount);

    public static native boolean pressKey(String keyCombination);
}
