package com.mitsubishi.simulation.input.transit;

import org.matsim.api.core.v01.network.Link;

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
    private Map<String, List<Transfer>> possibleTransferMap;

    // The use of default constructor is forbidden
    private Transit() {}

    public Transit(String type, String name) {
        this.type = type;
        this.name = name;
        this.stops = new ArrayList<TransitStop>();
        this.links = new ArrayList<Link>();
        this.possibleTransferMap = new LinkedHashMap<String, List<Transfer>>();
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

    public Map<String, List<Transfer>> getPossibleTransferMap() {
        return possibleTransferMap;
    }

    /**
     * Compute possible transfers when starting from a particular station
     * @param currentStation the starting station
     * @return possible transfers
     */
    public List<Transfer> computeForwardPossibleTransfers(TransitStation currentStation) {
        List<Transfer> transfers = new ArrayList<Transfer>();
        int stopIndex;
        for (stopIndex = 0;
             stopIndex < stops.size() && stops.get(stopIndex).getStation() != currentStation; stopIndex++) ;

        if (stopIndex >= stops.size()) {
            return transfers;
        }

        // find all stops that has a greater index than that of the given stop
        // transfers from the current station are not included in the returned results,
        // because it makes no sense to transfer from this transit to another
        // without going forward at all
        for (List<Transfer> list : possibleTransferMap.values()) {
            for (Transfer transfer : list) {
                if (transfer.getFromStop().getIndex() > stopIndex) {
                    transfers.add(transfer);
                }
            }
        }

        return transfers;
    }

    /**
     * Find the stop at the given station
     * @param station the station where the stop locates
     * @return the corresponding stop or null if not found
     */
    public TransitStop getStopFromStation(TransitStation station) {
        for (TransitStop stop : stops) {
            if (stop.getStation() == station) {
                return stop;
            }
        }
        return null;
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
