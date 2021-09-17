package com.kvoli.base;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(value = {"other"})

/*
  The Packet class is used for generating response messages objects. And Jackson helps to
  transform the JSON format String which send by clients to the Packet object. The structure
  of response messages are shown in the README.md file.
 */
public class Packet {
    private String type; //message type

    private String former; //former identity/room
    private String identity; //client identity

    private String roomid; //room's name

    private List<String> identities = new ArrayList<>(); //all clients in the room
    private String owner; //room owner

    private String content; //message content

    private Map<String, Object> rooms = new HashMap<>(); //room list

    private Map<String, Object> other = new HashMap<>(); //other field

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
