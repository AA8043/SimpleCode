package org.a8043.simpleCode.tools.windows;

import lombok.extern.slf4j.Slf4j;
import org.a8043.simpleCode.Registry;

import java.io.File;

@Slf4j
public class WindowsTools {
    public static void install() {
        File jarFile = new File(WindowsTools.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        String dllPath = new File(jarFile.getParentFile(), "native.dll").getAbsolutePath();
        log.info("Loading native library from: {}", dllPath);
        System.load(dllPath);
        Registry.registerTool(GetAllWindowsTool.TOOL);
        Registry.registerTool(GetWindowTreeTool.TOOL);
        Registry.registerTool(ClickButtonTool.TOOL);
        Registry.registerTool(ClickElementTool.TOOL);
        Registry.registerTool(InputTextTool.TOOL);
        Registry.registerTool(ActivateWindowTool.TOOL);
        Registry.registerTool(CloseWindowTool.TOOL);
        Registry.registerTool(SetCheckedTool.TOOL);
        Registry.registerTool(SelectElementTool.TOOL);
        Registry.registerTool(ScrollElementTool.TOOL);
        Registry.registerTool(PressKeyTool.TOOL);
    }
}
