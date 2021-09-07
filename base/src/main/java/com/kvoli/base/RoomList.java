package com.kvoli.base;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonPropertyOrder({"type", "rooms"})
@JsonIgnoreProperties(value = {"other"})

public class RoomList {
    @JsonIgnore
    private Map<String, Object> other = new HashMap<>();

    private static final String type = "roomlist";
    private Map<String, Object> rooms = new HashMap<>();

    public static String getType() {
        return type;
    }

    public Map<String, Object> getRooms() {
        return rooms;
    }

    public void setRooms(String key, Object value) {
        other.put(key, value);
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
