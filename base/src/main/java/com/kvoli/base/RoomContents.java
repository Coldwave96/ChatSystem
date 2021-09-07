package com.kvoli.base;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonPropertyOrder({"type", "roomid", "identities", "owner"})
@JsonIgnoreProperties(value = {"other"})

public class RoomContents {
    @JsonIgnore
    private Map<String, Object> other = new HashMap<>();

    private static final String type = "roomcontents";
    private String roomid;
    private String[] identities;
    private String owner;

    public static String getType() {
        return type;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }

    public String getRoomid() {
        return roomid;
    }

    public String[] getIdentities() {
        return identities;
    }

    public void setIdentities(String[] identities) {
        this.identities = identities;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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
