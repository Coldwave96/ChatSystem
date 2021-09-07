package com.kvoli.base;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(value = {"other"})

public class Packet {
    private String type;

    private String former;
    private String identity;

    private String roomid;

    private List<String> identities = new ArrayList<>();
    private String owner;

    private String content;

    private Map<String, Object> rooms = new HashMap<>();

    private Map<String, Object> other = new HashMap<>();

    public String getType() {
        return type;
    }

    public String getFormer() {
        return former;
    }

    public String getIdentity() {
        return identity;
    }

    public String getRoomid() {
        return roomid;
    }

    public List<String> getIdentities() {
        return identities;
    }

    public String getOwner() {
        return owner;
    }

    public String getContent() {
        return content;
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
