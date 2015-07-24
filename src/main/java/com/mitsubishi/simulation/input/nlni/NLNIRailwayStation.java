package com.mitsubishi.simulation.input.nlni;

import org.matsim.api.core.v01.Coord;

/**
 * Created by tiden on 7/14/2015.
 * Represents a station element in National Land Numerical Information's data
 */
public class NLNIRailwayStation {

    private String id;
    private String name;
    private String railwayType;
    private double x;
    private double y;
    private boolean deleted;

    public NLNIRailwayStation(String id) {
        this.id = id;
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

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRailwayType() {
        return railwayType;
    }

    public void setRailwayType(String railwayType) {
        this.railwayType = railwayType;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setCoord(Coord coord) {
        this.x = coord.getX();
        this.y = coord.getY();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NLNIRailwayStation)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return name.equals(((NLNIRailwayStation) obj).getName());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
