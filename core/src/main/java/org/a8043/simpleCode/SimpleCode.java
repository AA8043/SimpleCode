package org.a8043.simpleCode;

import cn.hutool.core.convert.AbstractConverter;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.convert.ConverterRegistry;
import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.session.Session;
import org.a8043.simpleCode.session.Status;
import org.a8043.simpleCode.session.content.*;
import org.a8043.simpleCode.session.tool.ToolCall;

import java.io.File;

public class SimpleCode {
    public static final File SETTINGS_DIR = new File(System.getProperty("user.home") + "/.simpleCode");

    public static void init() {
        Settings.read();
        registerConverters();
    }

    public static void registerConverters() {
        ConverterRegistry registry = ConverterRegistry.getInstance();

        registry.putCustom(Status.class, new AbstractConverter<Status>() {
            @Override
            protected Status convertInternal(Object value) {
                JSONObject json = (JSONObject) value;
                return new Status(json.getBool("isSuccess"), json.getStr("failedReason"));
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
                    json.getJSONArray("toolCalls").toList(ToolCall.class));
            }
        });

        registry.putCustom(SystemContent.class, new AbstractConverter<SystemContent>() {
            @Override
            protected SystemContent convertInternal(Object value) {
                return new SystemContent(0);
            }
        });

        registry.putCustom(ToolContent.class, new AbstractConverter<ToolContent>() {
            @Override
            protected ToolContent convertInternal(Object value) {
                JSONObject json = (JSONObject) value;
                return new ToolContent(json.getLong("time"), json.getStr("toolCallId"),
                    json.getBean("status", Status.class), json.getStr("content"));
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
                    case "tool" -> Convert.convert(ToolContent.class, value);
                    default -> throw new RuntimeException();
                };
            }
        });

        registry.putCustom(Session.class, new AbstractConverter<Session>() {
            @Override
            protected Session convertInternal(Object value) {
                JSONObject json = (JSONObject) value;
                Session session = new Session(json.getStr("id"));
                session.setName(json.getStr("name"));
                session.getContentList().addAll(json.getJSONArray("contentList").toList(Content.class));
                return session;
            }
        });
    }
}
