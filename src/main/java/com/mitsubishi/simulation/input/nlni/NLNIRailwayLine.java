package com.mitsubishi.simulation.input.nlni;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public Set<NLNIRailwayStation> getStationSet() {
        Set<NLNIRailwayStation> stationSet = new HashSet<NLNIRailwayStation>();
        stationSet.addAll(stations);
        return stationSet;
    }

    @Override
    public String toString() {
        return name;
    }
}
