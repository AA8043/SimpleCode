package org.a8043.simpleCode.api;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONObject;
import org.a8043.simpleCode.Registry;
import org.a8043.simpleCode.model.Model;
import org.a8043.simpleCode.session.content.AssistantContent;
import org.a8043.simpleCode.session.content.Content;
import org.a8043.simpleCode.session.content.ToolContent;
import org.a8043.simpleCode.session.content.UserContent;
import org.a8043.simpleCode.session.tool.ToolCall;
import org.a8043.simpleCode.session.tool.ToolParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class OpenAIApi implements Api {
    @Override
    public CompleteResult complete(Model model, List<Content> context) {
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", model.getName());

        context.forEach(content -> {
            JSONObject message = new JSONObject(JSONConfig.create().setIgnoreNullValue(false));
            message.set("role", content.getRole().name().toLowerCase());
            switch (content) {
                case UserContent uc -> message.set("content", uc.getText());
                case AssistantContent ac -> {
                    message.set("content", ac.getText());
                    message.set("tool_calls", ac.getToolCallList().stream().map(toolCall -> {
                        JSONObject toolCallJson = new JSONObject();
                        toolCallJson.set("id", toolCall.getId());
                        toolCallJson.set("type", "function");
                        toolCallJson.set("function", new JSONObject().set("name", toolCall.getTool().getName())
                            .set("arguments", toolCall.getArgs().toString()));
                        return toolCallJson;
                    }).toList());
                }
                case ToolContent tc -> {
                    message.set("tool_call_id", tc.getToolCallId());
                    message.set("content", tc.getText());
                }
                default -> throw new RuntimeException();
            }
            requestBody.append("messages", message);
        });

        Registry.TOOL_LIST.forEach(tool -> {
            JSONObject toolJson = new JSONObject();
            toolJson.set("name", tool.getName());
            toolJson.set("description", tool.getDescription());

            JSONObject argsJson = new JSONObject();
            argsJson.set("type", "object");
            JSONObject propertiesJson = new JSONObject();
            tool.getParameterList().forEach(param -> {
                JSONObject paramJson = new JSONObject();
                paramJson.set("description", param.getDescription());
                paramJson.set("type", param.getType().name().toLowerCase());
                propertiesJson.set(param.getName(), paramJson);
            });
            argsJson.set("properties", propertiesJson);
            tool.getParameterList().stream().filter(ToolParameter::isRequired)
                .forEach(param -> argsJson.append("required", param.getName()));
            toolJson.set("parameters", argsJson);

            requestBody.append("tools", new JSONObject().set("type", "function").set("function", toolJson));
        });

        HttpRequest post = HttpUtil.createPost(model.getProvider().getBaseUrl() + "/v1/chat/completions");
        post.addHeaders(Map.of("Authorization", "Bearer " + model.getProvider().getKey()));
        post.body(requestBody.toString());
        String response = post.execute().body();

        JSONObject responseBody = new JSONObject(response);
        List<AssistantContent> contentList = new ArrayList<>();
        AtomicBoolean isEnd = new AtomicBoolean();
        responseBody.getJSONArray("choices").forEach(o -> {
            JSONObject json = (JSONObject) o;
            JSONObject message = json.getJSONObject("message");
            isEnd.set(json.getStr("finish_reason").equals("stop"));
            if (message.getStr("role").equals("assistant")) {
                List<ToolCall> toolCallList = new ArrayList<>();
                JSONArray toolCallsJson = message.getJSONArray("tool_calls");
                if (toolCallsJson != null) {
                    toolCallsJson.forEach(o1 -> {
                        JSONObject json1 = (JSONObject) o1;
                        toolCallList.add(new ToolCall(
                            Registry.getTool(json1.getByPath("function.name", String.class)),
                            json1.getStr("id"),
                            new JSONObject(json1.getByPath("function.arguments", String.class))));
                    });
                }
                contentList.add(new AssistantContent(System.currentTimeMillis(),
                    message.getStr("content"), toolCallList));
            } else {
                throw new RuntimeException();
            }
        });
        return new CompleteResult(isEnd.get(), contentList);
    }
}
