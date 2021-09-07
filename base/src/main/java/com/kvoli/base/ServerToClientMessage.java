package com.kvoli.base;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonPropertyOrder({"type", "identity", "content"})
@JsonIgnoreProperties(value = {"other"})

public class ServerToClientMessage {
    @JsonIgnore
    private Map<String, Object> other = new HashMap<>();

    private static final String type = "message";
    private String identity;
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public static String getType() {
        return type;
    }

    @JsonAnySetter
    public void setOther(String key, Object value) {
        other.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getOther() {
        return other;
    }
}
