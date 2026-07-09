package org.a8043.simpleCode.cli.views;

import dev.tamboui.toolkit.element.Element;
import dev.tamboui.toolkit.elements.DialogElement;
import dev.tamboui.toolkit.elements.ListElement;
import dev.tamboui.toolkit.elements.TextElement;
import dev.tamboui.tui.bindings.ActionHandler;
import dev.tamboui.tui.bindings.Actions;
import dev.tamboui.tui.bindings.BindingSets;
import dev.tamboui.tui.bindings.KeyTrigger;
import dev.tamboui.widgets.input.TextInputState;
import lombok.extern.slf4j.Slf4j;
import org.a8043.simpleCode.Registry;
import org.a8043.simpleCode.Settings;
import org.a8043.simpleCode.cli.Main;
import org.a8043.simpleCode.frontend.FrontendSettings;
import org.a8043.simpleCode.frontend.I18n;
import org.a8043.simpleCode.model.Provider;
import org.a8043.simpleCode.model.RemoteModel;
import org.a8043.simpleCode.util.RpmLimiter;

import java.util.List;

import static dev.tamboui.toolkit.Toolkit.*;

@Slf4j
public class WelcomeView extends Main.View {
    private final TextInputState languageInputState = new TextInputState(FrontendSettings.INSTANCE.getLanguage());
    private Step current = Step.WELCOME;

    @Override
    public void init() {
    }

    private List<RemoteModel> modelList;
    private boolean isGettingModels = false;

    public List<RemoteModel> getModelList() {
        if (modelList == null && !isGettingModels) {
            isGettingModels = true;
            new Thread(() -> modelList = Settings.INSTANCE.getProviderList().stream()
                .flatMap(p -> p.requestModels().stream())
                .toList()).start();
        }
        return modelList;
    }

    @Override
    public DialogElement render() {
        DialogElement element = switch (current) {
            case WELCOME -> dialog("",
                text(I18n.get("welcome")).centered(),
                textInput(languageInputState).title(I18n.get("language")).onSubmit(() -> {
                    FrontendSettings.INSTANCE.setLanguage(languageInputState.text());
                    I18n.load();
                }).focusable().id("languageInput").rounded(),
                text(I18n.get("next")).centered().focusable().id("nextButton")
                    .onAction(new ActionHandler(BindingSets.standard())
                        .on(Actions.SELECT, event -> current = Step.SET_PROVIDERS))
            );
            case SET_PROVIDERS -> dialog("",
                text(I18n.get("provider.set")).centered(),
                text(I18n.get("add")).centered().focusable().id("addButton")
                    .onAction(new ActionHandler(BindingSets.standard())
                        .on(Actions.SELECT, event -> Main.INSTANCE.setView(new AddProviderView()))),
                list(Settings.INSTANCE.getProviderList().stream().map(p -> p.getName() + ": " + p.getBaseUrl()).toList()),
                text(I18n.get("next")).centered().focusable().id("nextButton")
                    .onAction(new ActionHandler(BindingSets.standard())
                        .on(Actions.SELECT, event -> current = Step.SET_MODELS))
            );
            case SET_MODELS -> dialog("",
                text(I18n.get("model.set")).centered(),
                getModelList() != null ? getModelListElement() : text(I18n.get("loading")).centered(),
                text(I18n.get("model.added")),
                list(Settings.INSTANCE.getModelList().stream()
                    .map(m -> I18n.get("model.addedListTip",
                        m.getProvider().getName(), m.getName(), String.valueOf(m.getLevel()))).toList())
                    .id("addedList"),
                text(I18n.get("ok")).centered().focusable().id("okButton")
                    .onAction(new ActionHandler(BindingSets.standard())
                        .on(Actions.SELECT, event -> Main.INSTANCE.showMain()))
            );
        };
        return element.rounded().minWidth(80);
    }

    private ListElement<RemoteModel> modelListElement;

    private ListElement<RemoteModel> getModelListElement() {
        if (modelListElement != null) {
            return modelListElement;
        }
        ListElement<RemoteModel> list = new ListElement<>();
        list.data(modelList, m -> new TextElement("[%s] %s".formatted(m.getProvider().getName(), m.getName())));
        for (char c = '0'; c <= '9'; c++) {
            int finalC = Character.getNumericValue(c);
            list.on(KeyTrigger.ch(c), e -> Settings.INSTANCE.getModelList()
                .add(modelList.get(list.selected()).toModel(finalC)));
        }
        return modelListElement = list;
    }

    private enum Step {
        WELCOME, SET_PROVIDERS, SET_MODELS
    }

    private class AddProviderView extends Main.View {
        private final TextInputState nameState = new TextInputState();
        private final TextInputState baseUrlState = new TextInputState();
        private final TextInputState keyState = new TextInputState();
        private final TextInputState apiState = new TextInputState();
        private final TextInputState maxRpmState = new TextInputState();

        @Override
        public void init() {
        }

        @Override
        public Element render() {
            return dialog("",
                text(I18n.get("provider.add")).centered(),
                textInput(nameState).title(I18n.get("provider.name")).focusable().id("nameInput").rounded(),
                textInput(baseUrlState).title(I18n.get("provider.base_url")).focusable().id("baseUrlInput").rounded(),
                textInput(keyState).title(I18n.get("provider.key")).focusable().id("keyInput").rounded(),
                textInput(apiState).title(I18n.get("provider.api")).focusable().id("apiInput").rounded(),
                textInput(maxRpmState).title(I18n.get("provider.maxRpm")).focusable().id("maxRpmInput").rounded(),
                text(I18n.get("add")).centered().focusable().id("saveButton")
                    .onAction(new ActionHandler(BindingSets.standard())
                        .on(Actions.SELECT, event -> {
                            Settings.INSTANCE.getProviderList().add(new Provider(nameState.text(),
                                baseUrlState.text(), keyState.text(), Registry.API_MAP.get(apiState.text()),
                                maxRpmState.text().isEmpty() ? null :
                                    new RpmLimiter(Integer.parseInt(maxRpmState.text()))));
                            Main.INSTANCE.setView(WelcomeView.this);
                        }))
            ).rounded();
        }
    }
}
