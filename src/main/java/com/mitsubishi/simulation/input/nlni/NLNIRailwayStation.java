package com.mitsubishi.simulation.input.nlni;

/**
 * Created by tiden on 7/14/2015.
 * Represents a station element in National Land Numerical Information's data
 */
public class NLNIRailwayStation {

    private String id;
    private String name;
    private double x;
    private double y;

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

    @Override
    public String toString() {
        return name;
    }
}
