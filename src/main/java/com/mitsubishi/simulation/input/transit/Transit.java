package com.mitsubishi.simulation.input.transit;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tiden on 7/3/2015.
 * Represents a public transit that consists of stops, links and possible transfers
 */
public class Transit {
    public static final String BUS = "bus";
    public static final String TRAIN = "train";

    private String type;
    private String name;
    private List<TransitStop> stops;
    private List<Link> links;
    // used to store edges in an directed graph
    private Map<String, List<Transfer>> possibleTransfers;

    // The use of default constructor is forbidden
    private Transit() {}

    public Transit(String type, String name) {
        this.type = type;
        this.name = name;
        this.stops = new ArrayList<TransitStop>();
        this.links = new ArrayList<Link>();
        this.possibleTransfers = new LinkedHashMap<String, List<Transfer>>();
    }

    public String getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<TransitStop> getStops() {
        return stops;
    }

    public List<Link> getLinks() {
        return links;
    }

    public Map<String, List<Transfer>> getPossibleTransfers() {
        return possibleTransfers;
    }

    @Override
    /**
     * Transits are really distinguished by their names
     */
    public boolean equals(Object object) {
        if (object instanceof Transit) {
            return this.name.equals(((Transit) object).getName());
        }
        return false;
    }
}
