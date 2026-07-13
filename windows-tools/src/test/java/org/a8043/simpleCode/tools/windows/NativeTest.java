package org.a8043.simpleCode.tools.windows;

import org.junit.Before;
import org.junit.Test;

public class NativeTest {
    @Before
    public void init() {
        System.loadLibrary("native");
    }

    @Test
    public void getAllWindows() {
        System.out.println(Native.getAllWindows().toStringPretty());
    }

    @Test
    public void getWindowTree() {
        String windowId = Native.getAllWindows().getJSONObject(3).getStr("id");
        System.out.println(Native.getWindowTree(windowId).toStringPretty());
    }
}
