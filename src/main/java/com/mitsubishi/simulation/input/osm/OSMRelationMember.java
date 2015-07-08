package com.mitsubishi.simulation.input.osm;

/**
 * Created by tiden on 7/3/2015.
 */
public class OSMRelationMember {
    private long ref;
    private String type;
    private String role;

    public OSMRelationMember(String type, long ref, String role) {
        this.ref = ref;
        this.type = type;
        this.role = role;
    }

    public long getRef() {
        return ref;
    }

    public String getType() {
        return type;
    }

    public String getRole() {
        return role;
    }
}
