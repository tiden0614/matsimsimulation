package com.mitsubishi.simulation.input.nlni;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiden on 7/14/2015.
 * Represents a railway line in National Land Numerical Information's data
 */
public class NLNIRailwayLine {

    private String name;
    private List<NLNIRailwayStation> stations;

    public NLNIRailwayLine(String name) {
        this.name = name;
        this.stations = new ArrayList<NLNIRailwayStation>();
    }

    public String getName() {
        return name;
    }

    public List<NLNIRailwayStation> getStations() {
        return stations;
    }

    @Override
    public String toString() {
        return name;
    }
}
