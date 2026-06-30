package org.a8043.simpleCode;

import cn.hutool.core.convert.AbstractConverter;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.convert.ConverterRegistry;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.model.Model;
import org.a8043.simpleCode.session.Status;
import org.a8043.simpleCode.session.Todo;
import org.a8043.simpleCode.session.content.*;
import org.a8043.simpleCode.session.tool.ToolCall;

import java.io.File;

public class SimpleCode {
    public static final File DATA_DIR = new File(System.getProperty("user.home") + "/.simpleCode");
    public static final JSONObject PROMPT_JSON = new JSONObject(ResourceUtil.readUtf8Str("prompts.json"));
    public static final File FOLDERS_DATA_DIR = new File(DATA_DIR, "folders");

    public static boolean init() {
        registerConverters();
        boolean isNotFirst = Settings.read();
        Registry.AFTER_INIT_LIST.forEach(Runnable::run);
        return isNotFirst;
    }

    public static void save() {
        Settings.save();
    }

    private static void registerConverters() {
        ConverterRegistry registry = ConverterRegistry.getInstance();

        registry.putCustom(Status.class, new AbstractConverter<Status>() {
            @Override
            protected Status convertInternal(Object value) {
                JSONObject json = (JSONObject) value;
                return new Status(json.getBool("isSuccess"), json.getStr("failedReason"));
            }
        });

        registry.putCustom(Model.class, new AbstractConverter<Model>() {
            @Override
            protected Model convertInternal(Object value) {
                JSONObject json = (JSONObject) value;
                return new Model(Settings.INSTANCE.getProvider(json.getStr("provider")),
                    json.getStr("name"), json.getInt("level"));
            }
        });

        registry.putCustom(ToolCall.class, new AbstractConverter<ToolCall>() {
            @Override
            protected ToolCall convertInternal(Object value) {
                JSONObject json = (JSONObject) value;
                return new ToolCall(Registry.getTool(json.getStr("tool")), json.getStr("id"),
                    json.getJSONObject("args"));
            }
        });

        registry.putCustom(UserContent.class, new AbstractConverter<UserContent>() {
            @Override
            protected UserContent convertInternal(Object value) {
                JSONObject json = (JSONObject) value;
                return new UserContent(json.getLong("time"), json.getStr("text"));
            }
        });

        registry.putCustom(AssistantContent.class, new AbstractConverter<AssistantContent>() {
            @Override
            protected AssistantContent convertInternal(Object value) {
                JSONObject json = (JSONObject) value;
                return new AssistantContent(json.getLong("time"), json.getStr("text"),
                    json.getJSONArray("toolCalls").toList(String.class));
            }
        });

        registry.putCustom(SystemContent.class, new AbstractConverter<SystemContent>() {
            @Override
            protected SystemContent convertInternal(Object value) {
                return new SystemContent(0);
            }
        });

        registry.putCustom(RemindContent.class, new AbstractConverter<RemindContent>() {
            @Override
            protected RemindContent convertInternal(Object value) {
                JSONObject json = (JSONObject) value;
                return new RemindContent(json.getLong("time"), json.getStr("text"));
            }
        });

        registry.putCustom(ToolContent.class, new AbstractConverter<ToolContent>() {
            @Override
            protected ToolContent convertInternal(Object value) {
                JSONObject json = (JSONObject) value;
                return new ToolContent(json.getLong("time"), json.getStr("toolCall"),
                    registry.convert(Status.class, json.getJSONObject("status")), json.getStr("content"));
            }
        });

        registry.putCustom(Content.class, new AbstractConverter<Content>() {
            @Override
            protected Content convertInternal(Object value) {
                JSONObject json = (JSONObject) value;
                return switch (json.getStr("type")) {
                    case "user" -> Convert.convert(UserContent.class, value);
                    case "assistant" -> Convert.convert(AssistantContent.class, value);
                    case "system" -> Convert.convert(SystemContent.class, value);
                    case "remind" -> Convert.convert(RemindContent.class, value);
                    case "tool" -> Convert.convert(ToolContent.class, value);
                    default -> throw new RuntimeException();
                };
            }
        });

        registry.putCustom(Todo.class, new AbstractConverter<Todo>() {
            @Override
            protected Todo convertInternal(Object value) {
                JSONObject json = (JSONObject) value;
                Todo todo = new Todo(json.getStr("task"), json.getStr("id"));
                todo.setStatus(json.getEnum(Todo.Status.class, "status"));
                return todo;
            }
        });
    }
}
