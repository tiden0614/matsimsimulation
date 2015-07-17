package com.mitsubishi.simulation.input.matsimtransit;

/**
 * Created by tiden on 7/17/2015.
 */
public class StopFacility {
    private String id;
    private double x;
    private double y;
    private String linkRefId;
    private String name;

    public StopFacility(String id, double x, double y, String name) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.name = name;
        this.linkRefId = null;
    }

    public String getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getLinkRefId() {
        return linkRefId;
    }

    public String getName() {
        return name;
    }

    public void setLinkRefId(String linkRefId) {
        this.linkRefId = linkRefId;
    }
}
