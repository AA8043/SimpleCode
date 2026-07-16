package org.a8043.simpleCode.cli;

import dev.tamboui.css.engine.StyleEngine;
import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.a8043.simpleCode.Folder;
import org.a8043.simpleCode.SimpleCode;
import org.a8043.simpleCode.cli.views.MainView;
import org.a8043.simpleCode.cli.views.WelcomeView;
import org.a8043.simpleCode.frontend.FrontendSettings;
import org.a8043.simpleCode.frontend.I18n;
import org.a8043.simpleCode.tools.windows.WindowsTools;

import java.awt.*;
import java.io.File;

@Slf4j
@Getter
public class Main extends ToolkitApp {
    public static final Main INSTANCE = new Main();
    private View view;
    private Folder folder;

    @SneakyThrows
    public static void main(String[] args) {
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> log.error(e.getMessage(), e));
        INSTANCE.run();
    }

    @SneakyThrows
    @Override
    protected void onStart() {
        StyleEngine styleEngine = StyleEngine.create();
        styleEngine.loadStylesheet("/styles/main.tcss");
        runner().styleEngine(styleEngine);

        boolean isNotFirst = SimpleCode.init() & FrontendSettings.load() & CliSettings.load();
        I18n.load();
        if (isNotFirst) {
            showMain();
        } else {
            setView(new WelcomeView());
        }
    }

    public void showMain() {
        if (System.getProperty("os.name").toLowerCase().contains("win") &&
            !GraphicsEnvironment.isHeadless() &&
            CliSettings.INSTANCE.isAllowWindowsTools()) {
            WindowsTools.install();
        }

        folder = new Folder(new File(System.getProperty("user.dir")));
        setView(MainView.INSTANCE);
    }

    @Override
    protected Element render() {
        Util.SPINNER_STATE.advance();
        Util.WAVE_TEXT_STATE.advance();

        try {
            return view != null ? view.render() : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public void setView(View view) {
        if (!view.isInited) {
            view.init();
            view.isInited = true;
        }
        this.view = view;
    }

    public void exit() {
        folder.close();
        SimpleCode.save();
        FrontendSettings.save();
        CliSettings.save();
        quit();
    }

    public abstract static class View {
        private boolean isInited;

        public abstract void init();

        public abstract Element render();
    }
}
