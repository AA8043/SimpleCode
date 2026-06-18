package org.a8043.simpleCode.cli;

import dev.tamboui.css.engine.StyleEngine;
import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;
import lombok.SneakyThrows;
import org.a8043.simpleCode.SimpleCode;
import org.a8043.simpleCode.cli.views.WelcomeView;

public class Main extends ToolkitApp {
    public static final Main INSTANCE = new Main();
    private View view;

    @SneakyThrows
    public static void main(String[] args) {
        INSTANCE.run();
    }

    @SneakyThrows
    @Override
    protected void onStart() {
        StyleEngine styleEngine = StyleEngine.create();
        styleEngine.loadStylesheet("main", "/styles/main.tcss");
        styleEngine.setActiveStylesheet("main");
        runner().styleEngine(styleEngine);

        boolean isNotFirst = SimpleCode.init() & CliSettings.load();
        I18n.load();
        if (isNotFirst) {
            // TODO: 主界面
        } else {
            setView(new WelcomeView());
        }
    }

    @Override
    protected Element render() {
        return view != null ? view.render() : null;
    }

    public void setView(View view) {
        view.init();
        this.view = view;
    }

    public interface View {
        void init();

        Element render();
    }
}
