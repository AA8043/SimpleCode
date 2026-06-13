package org.a8043.simpleCode;

import lombok.Getter;

import java.io.File;

@Getter
public class Folder {
    private final File file;
    private final File dataDir;
    private final File sessionsDir;

    public Folder(File file) {
        this.file = file;
        this.dataDir = new File(file, "data");
        this.sessionsDir = new File(file, "sessions");
    }
}
