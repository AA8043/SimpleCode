package org.a8043.simpleCode.api;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.a8043.simpleCode.Registry;
import org.a8043.simpleCode.model.Model;
import org.a8043.simpleCode.model.Provider;
import org.a8043.simpleCode.model.RemoteModel;
import org.a8043.simpleCode.session.Session;
import org.a8043.simpleCode.session.content.AssistantContent;
import org.a8043.simpleCode.session.content.ToolContent;
import org.a8043.simpleCode.session.tool.ToolCall;
import org.a8043.simpleCode.session.tool.parameter.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class OpenAIApi implements Api {
    @Override
    public CompleteResult complete(Model model, Session session) {
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", model.getName());
        requestBody.set("stream_options", new JSONObject().set("include_usage", true));

        requestBody.set("reasoning_effort", switch (session.getReasoningEffort()) {
            case LOW -> "low";
            case DEFAULT -> "medium";
            case HIGH -> "high";
            case MAX -> "xhigh";
        });

        session.getContentList().forEach(content -> {
            JSONObject message = new JSONObject(JSONConfig.create().setIgnoreNullValue(false));
            message.set("role", content.getRole().name().toLowerCase());
            switch (content) {
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
                    message.set("tool_call_id", tc.getToolCall().getId());
                    message.set("content", tc.getText());
                }
                default -> message.set("content", content.getText());
            }
            requestBody.append("messages", message);
        });

        Registry.TOOL_LIST.forEach(tool -> {
            JSONObject toolJson = new JSONObject();
            toolJson.set("name", tool.getName());
            toolJson.set("description", tool.getDescription());

            JSONObject argsJson = new JSONObject();
            convertParameterToJson(new ObjectParameter(null, null,
                true, tool.getParameterList()), argsJson);
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
                String content = message.getStr("content");
                contentList.add(new AssistantContent(System.currentTimeMillis(),
                    content != null ? content : "", toolCallList));
            } else {
                throw new RuntimeException();
            }
        });
        JSONObject usage = responseBody.getJSONObject("usage");
        return new CompleteResult(isEnd.get(), contentList,
            usage.getInt("prompt_tokens"),
            usage.getJSONObject("prompt_tokens_details").getInt("cached_tokens"),
            usage.getInt("completion_tokens") +
            usage.getJSONObject("completion_tokens_details").getInt("reasoning_tokens"));
    }

    private void convertParameterToJson(ToolParameter parameter, JSONObject json) {
        json.set("description", parameter.getDescription());
        json.set("type", switch (parameter) {
            case StringParameter ignored -> "string";
            case BooleanParameter ignored -> "boolean";
            case NumberParameter ignored -> "number";
            case ArrayParameter ignored -> "array";
            case ObjectParameter ignored -> "object";
            default -> throw new RuntimeException();
        });
        switch (parameter) {
            case StringParameter sp -> {
                if (sp.getEnumList() != null) {
                    for (String value : sp.getEnumList()) {
                        json.append("enum", value);
                    }
                }
            }
            case BooleanParameter ignored -> {
            }
            case NumberParameter np -> {
                if (np.getMin() != null) {
                    json.set("minimum", np.getMin());
                }
                if (np.getMax() != null) {
                    json.set("maximum", np.getMax());
                }
            }
            case ArrayParameter ap -> {
                JSONObject items = new JSONObject();
                convertParameterToJson(ap.getType(), items);
                json.set("items", items);
            }
            case ObjectParameter op -> {
                JSONObject properties = new JSONObject();
                op.getContent().forEach(p -> {
                    JSONObject json1 = new JSONObject();
                    convertParameterToJson(p, json1);
                    properties.set(p.getName(), json1);
                });
                json.set("properties", properties);

                JSONArray requiredArray = new JSONArray();
                op.getContent().stream().filter(ToolParameter::isRequired)
                    .map(ToolParameter::getName).forEach(requiredArray::add);
                if (!requiredArray.isEmpty()) {
                    json.set("required", requiredArray);
                }
            }
            default -> throw new RuntimeException();
        }
    }

    @Override
    public List<RemoteModel> getModels(Provider provider) {
        HttpRequest get = HttpUtil.createGet(provider.getBaseUrl() + "/v1/models");
        get.addHeaders(Map.of("Authorization", "Bearer " + provider.getKey()));
        String response = get.execute().body();

        JSONObject responseBody = new JSONObject(response);
        List<RemoteModel> modelList = new ArrayList<>();
        responseBody.getJSONArray("data").forEach(o -> {
            JSONObject json = (JSONObject) o;
            modelList.add(new RemoteModel(provider, json.getStr("id")));
        });
        return modelList;
    }
}
