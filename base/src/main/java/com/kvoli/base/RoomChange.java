package com.kvoli.base;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonPropertyOrder({"type", "identity", "former", "roomid"})
@JsonIgnoreProperties(value = {"other"})

public class RoomChange {
    @JsonIgnore
    private Map<String, Object> other = new HashMap<>();

    private static final String type = "roomchange";
    private String identity;
    private String former;
    private String roomid;

    public static String getType() {
        return type;
    }

    public void setFormer(String former) {
        this.former = former;
    }

    public String getFormer() {
        return former;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getIdentity() {
        return identity;
    }

    public String getRoomid() {
        return roomid;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
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
