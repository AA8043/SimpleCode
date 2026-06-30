package org.a8043.simpleCode;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.a8043.simpleCode.session.ReasoningEffort;
import org.a8043.simpleCode.session.Session;
import org.a8043.simpleCode.session.Todo;
import org.a8043.simpleCode.session.content.Content;
import org.a8043.simpleCode.session.tool.ToolCall;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Slf4j
@Getter
public class Folder {
    private static final List<Folder> openedFolderList = new ArrayList<>();
    private final File dir;
    private final File dataDir;
    private final File sessionsDir;
    private final List<Session> sessionList = new ArrayList<>();
    private final Thread autoSaveThread;

    public Folder(File dir) {
        openedFolderList.add(this);
        this.dir = dir;
        dataDir = new File(SimpleCode.FOLDERS_DATA_DIR, dir.getAbsolutePath().replace(":", ""));

        sessionsDir = new File(dataDir, "sessions");
        if (!sessionsDir.exists() && !sessionsDir.mkdirs()) {
            throw new RuntimeException();
        }

        Function<JSONObject, Session> convertToSession = new Function<>() {
            private Session convert(JSONObject json, Session parent) {
                Session session = new Session(Folder.this, json.getEnum(Session.Type.class, "type"),
                    json.getStr("id"), parent);
                session.setName(json.getStr("name"));
                session.getContentList().addAll(json.getJSONArray("contentList").toList(Content.class));
                session.getToolCallList().addAll(json.getJSONArray("toolCallList").toList(ToolCall.class));
                session.getTodoList().addAll(json.getJSONArray("todoList").toList(Todo.class));
                session.setReasoningEffort(json.getEnum(ReasoningEffort.class, "reasoningEffort"));
                session.setAutoModeDirectly(json.getBool("isAutoMode"));
                session.setPlanModeDirectly(json.getBool("isPlanMode"));
                session.getSubList().addAll(json.getJSONArray("subList")
                    .stream().map(j -> convert((JSONObject) j, session)).toList());
                return session;
            }

            @Override
            public Session apply(JSONObject json) {
                return convert(json, null);
            }
        };
        for (File sessionFile : Objects.requireNonNull(sessionsDir.listFiles())) {
            JSONObject json = new JSONObject(FileUtil.readUtf8String(sessionFile));
            sessionList.add(convertToSession.apply(json));
        }

        autoSaveThread = new Thread(() -> {
            while (ThreadUtil.sleep(3000)) {
                saveSessions();
            }
        });
        autoSaveThread.start();
    }

    public Session createSession() {
        Session session = Session.create(Session.Type.NORMAL, this, null, "normal");
        sessionList.add(session);
        return session;
    }

    public Session getSession(String id) {
        return sessionList.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }

    public void saveSessions() {
        sessionList.forEach(session -> {
            File sessionFile = new File(sessionsDir, session.getId() + ".json");
            FileUtil.writeUtf8String(new JSONObject(session).toString(), sessionFile);
        });
    }

    public void close() {
        autoSaveThread.interrupt();
        saveSessions();
    }

    public static Folder where(Session session) {
        return openedFolderList.stream().filter(folder ->
            folder.getSessionList().contains(session)).findFirst().orElse(null);
    }
}
