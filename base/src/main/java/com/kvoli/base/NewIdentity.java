package com.kvoli.base;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonPropertyOrder({"type", "former", "identity"})
@JsonIgnoreProperties(value = {"other"})

public class NewIdentity {
    @JsonIgnore
    private Map<String, Object> other = new HashMap<>();

    private static final String type = "newidentity";
    private String former;
    private String identity;

    public String getType() {
        return type;
    }

    public String getIdentity() {
        return identity;
    }

    public String getFormer() {
        return former;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public void setFormer(String former) {
        this.former = former;
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
