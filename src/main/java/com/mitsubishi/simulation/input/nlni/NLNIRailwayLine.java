package com.mitsubishi.simulation.input.nlni;

import java.util.*;

/**
 * Created by tiden on 7/14/2015.
 * Represents a railway line in National Land Numerical Information's data
 */
public class NLNIRailwayLine {

    private String name;
    private String railwayType;
    private List<NLNIRailwayStation> stations;

    public NLNIRailwayLine(String name) {
        this.name = name;
        this.stations = new LinkedList<NLNIRailwayStation>();
    }

    public String getName() {
        return name;
    }

    public String getRailwayType() {
        return railwayType;
    }

    public void setRailwayType(String railwayType) {
        this.railwayType = railwayType;
    }

    public List<NLNIRailwayStation> getStations() {
        return stations;
    }

    public Set<NLNIRailwayStation> getStationSet() {
        Set<NLNIRailwayStation> stationSet = new LinkedHashSet<NLNIRailwayStation>();
        stationSet.addAll(stations);
        return stationSet;
    }

    @Override
    public String toString() {
        return name;
    }
}
