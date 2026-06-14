package org.a8043.simpleCode.cli.views;

import dev.tamboui.toolkit.element.Element;
import dev.tamboui.tui.bindings.ActionHandler;
import dev.tamboui.tui.bindings.Actions;
import dev.tamboui.tui.bindings.BindingSets;
import dev.tamboui.widgets.input.TextInputState;
import org.a8043.simpleCode.Registry;
import org.a8043.simpleCode.Settings;
import org.a8043.simpleCode.cli.CliSettings;
import org.a8043.simpleCode.cli.I18n;
import org.a8043.simpleCode.cli.Main;
import org.a8043.simpleCode.model.Provider;

import static dev.tamboui.toolkit.Toolkit.*;

public class WelcomeView implements Main.View {
    private final TextInputState languageInputState = new TextInputState(CliSettings.INSTANCE.getLanguage());
    private Step current = Step.WELCOME;

    @Override
    public void init() {

    }

    @Override
    public Element render() {
        return switch (current) {
            case WELCOME -> dialog("",
                text(I18n.get("welcome")).centered(),
                textInput(languageInputState).title(I18n.get("language")).onSubmit(() -> {
                    CliSettings.INSTANCE.setLanguage(languageInputState.text());
                    I18n.load();
                }).focusable().id("languageInput").rounded(),
                text(I18n.get("next")).centered().focusable().id("nextButton")
                    .onAction(new ActionHandler(BindingSets.standard())
                        .on(Actions.SELECT, event -> current = Step.SET_PROVIDERS))
            ).rounded();
            case SET_PROVIDERS -> dialog("",
                text(I18n.get("provider.set")).centered(),
                text(I18n.get("add")).centered().focusable().id("addButton")
                    .onAction(new ActionHandler(BindingSets.standard())
                        .on(Actions.SELECT, event -> Main.INSTANCE.setView(new AddProviderView()))),
                list(Settings.INSTANCE.getProviderList().stream().map(p -> p.getName() + ": " + p.getBaseUrl()).toList()),
                text(I18n.get("next")).centered().focusable().id("nextButton")
                    .onAction(new ActionHandler(BindingSets.standard())
                        .on(Actions.SELECT, event -> current = Step.SET_MODELS))
            ).rounded();
            case SET_MODELS -> null;// TODO: 设置模型
        };
    }

    private enum Step {
        WELCOME, SET_PROVIDERS, SET_MODELS
    }

    private class AddProviderView implements Main.View {
        private final TextInputState nameState = new TextInputState();
        private final TextInputState baseUrlState = new TextInputState();
        private final TextInputState keyState = new TextInputState();
        private final TextInputState apiState = new TextInputState();

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
                text(I18n.get("add")).centered().focusable().id("saveButton")
                    .onAction(new ActionHandler(BindingSets.standard())
                        .on(Actions.SELECT, event -> {
                            Settings.INSTANCE.getProviderList().add(new Provider(nameState.text(),
                                baseUrlState.text(), keyState.text(), Registry.API_MAP.get(apiState.text())));
                            Main.INSTANCE.setView(WelcomeView.this);
                        }))
            ).rounded();
        }
    }
}
