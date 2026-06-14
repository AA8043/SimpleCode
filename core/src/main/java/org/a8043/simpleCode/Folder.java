package org.a8043.simpleCode;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import lombok.Getter;
import org.a8043.simpleCode.session.Session;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class Folder {
    private static final List<Folder> openedFolderList = new ArrayList<>();
    private final File dir;
    private final File dataDir;
    private final File sessionsDir;
    private final List<Session> sessionList = new ArrayList<>();

    public Folder(File dir) {
        openedFolderList.add(this);
        this.dir = dir;
        this.dataDir = new File(dir, ".simpleCode");
        this.sessionsDir = new File(dataDir, "sessions");

        if (!sessionsDir.exists() && !sessionsDir.mkdirs()) {
            throw new RuntimeException();
        }
        for (File sessionFile : Objects.requireNonNull(sessionsDir.listFiles())) {
            System.out.println(FileUtil.readUtf8String(sessionFile));
            sessionList.add(Convert.convert(Session.class, new JSONObject(FileUtil.readUtf8String(sessionFile))));
        }
    }

    public Session createSession() {
        Session session = Session.create();
        sessionList.add(session);
        return session;
    }

    public void saveSessions() {
        sessionList.forEach(session -> {
            File sessionFile = new File(sessionsDir, session.getId() + ".json");
            FileUtil.writeUtf8String(session.toJSONString(), sessionFile);
        });
    }

    public static Folder where(Session session) {
        return openedFolderList.stream().filter(folder ->
            folder.getSessionList().contains(session)).findFirst().orElse(null);
    }
}
