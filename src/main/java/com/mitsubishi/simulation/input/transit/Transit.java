package com.mitsubishi.simulation.input.transit;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;

import java.util.*;

/**
 * Created by tiden on 7/3/2015.
 * Represents a public transit that consists of stops, links and possible transfers
 */
public class Transit {
    public static final String BUS = "bus";
    public static final String TRAIN = "train";

    private static final Logger logger = Logger.getLogger(Transit.class);

    // That if this transit is a duplex transit affects how the transit schedule file is generated
    // and also how the routing algorithm behaves
    // If the transit is a one way transit, given a particular station, the computeForwardPossibleTransfers
    // only gives out forward stations; otherwise it will return all transfers no matter forwards or backwards
    private boolean duplexTransit;
    private String type;
    private String name;
    private List<TransitStop> stops;
    private List<Link> links;
    // used to store edges in an directed graph
    private Map<String, List<Transfer>> possibleTransferMap;

    // The use of default constructor is forbidden
    private Transit() {}

    public Transit(String type, String name) {
        this.duplexTransit = false;
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

    public void setDuplexTransit(boolean duplexTransit) {
        this.duplexTransit = duplexTransit;
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

        // TODO implement duplex transit features

        return transfers;
    }

    /**
     * Since the order of stops loaded from some data source is incorrect, it needs to rearrange the stops
     *
     * The algorithm computes relative distances between stops and finds a most possible path connecting
     * the stops
     *
     * This algorithm still needs to be refined since it considers only *regular* transit data and it will
     * not be correct if there is peculiar/partial/incorrect data that makes the stops to form more than one
     * graphs
     *
     * This algorithm is tested by the data absorbed from National Land Numerical Information's train data
     *
     * Further tests should be conducted
     *
     * The time complexity is O(n^2) and the space complexity is O(n), where n is the number of stops of
     * this transit
     */
    public void rearrangeStops() {
        // There's no need to calculate transits that have none or only one stop
        if (stops.size() <= 1) {
            return;
        }

        // For transits that have only two stops, assign indexes directly
        if (stops.size() == 2) {
            for (int i = 0; i < stops.size(); i++) {
                stops.get(i).setIndex(i);
            }
            return;
        }

        // Iterate stops of this transit, assign them their nearest and 2nd nearest stops
        // The time complexity of this operation is O(n^2) where n is the number of stops of this transit
        for (TransitStop stop : stops) {

            // first find the nearest
            double minDistance = Double.MAX_VALUE;
            TransitStop nearest = null;
            for (TransitStop anotherStop : stops) {
                if (anotherStop != stop) {
                    double distance = stop.getDistanceFromStop(anotherStop);
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearest = anotherStop;
                    }
                }
            }

            // find the 2nd nearest
            minDistance = Double.MAX_VALUE;
            TransitStop secNearest = null;
            for (TransitStop anotherStop : stops) {
                if (anotherStop != stop && anotherStop != nearest) {
                    double distance = stop.getDistanceFromStop(anotherStop);
                    if (distance < minDistance) {
                        minDistance = distance;
                        secNearest = anotherStop;
                    }
                }
            }

            // set the nearest and the 2nd nearest
            stop.setNearestStopInLine(nearest);
            stop.setSecNearestStopInLine(secNearest);

            // initialize the indexes of each stop
            stop.setIndex(Integer.MIN_VALUE);
        }

        TransitStop currentStop = null;

        // If stop A has a nearest stop B, but A is not the nearest or 2nd nearest stop of B,
        // set A's nearest to null; do the same to the 2nd nearest of A
        // In other words, after this operation, all stops are either the nearest or 2nd nearest stops of their nearest
        // or 2nd nearest stops
        // The time complexity of this operation is O(n)
        for (TransitStop stop : stops) {

            // for debug use
            if (stop.getNearestStopInLine() == null || stop.getSecNearestStopInLine() == null) {
                StringBuilder builder = new StringBuilder("An error happened when rearranging stops for Transit: ");
                builder.append(name);
                builder.append("\n");
                for (TransitStop transitStop : stops) {
                    builder.append("\t");
                    builder.append(transitStop.getStation().getName());
                    builder.append("\n");
                }
                builder.append("\nThe stops of this Transit form more than one graph\n");
                logger.warn(builder.toString());
                return;
            }

            if (!stop.getNearestStopInLine().isMyNearestOrSecNearest(stop)) {
                stop.setNearestStopInLine(null);
            }
            if (!stop.getSecNearestStopInLine().isMyNearestOrSecNearest(stop)) {
                stop.setSecNearestStopInLine(null);
            }
            // If the above operation is correct, at least one of the nearest stops is not null
            assert stop.getNearestStopInLine() != null || stop.getSecNearestStopInLine() != null;
            // find one end of the line
            if (stop.getNearestStopInLine() == null || stop.getSecNearestStopInLine() == null) {
                currentStop = stop;
            }
        }

        if (currentStop == null) {
            // we cannot find an end; the stops actually form a ring; just pick one to start
            currentStop = stops.get(0);
        }


        // Treat the stops and their nearest stops as a graph
        // Traverse the graph and assign indexes
        // The time complexity of this operation is O(n)
        Set<TransitStop> visited = new HashSet<TransitStop>();
        Stack<TransitStop> stack = new Stack<TransitStop>();

        stack.push(currentStop);
        visited.add(currentStop);
        currentStop.setIndex(0);

        int nextIndex = 1;

        while (!stack.isEmpty()) {
            currentStop = stack.peek();
            TransitStop nearest = currentStop.getNearestStopInLine();
            TransitStop secNearest = currentStop.getSecNearestStopInLine();
            if (nearest != null && !visited.contains(nearest)) {
                stack.push(nearest);
                visited.add(nearest);
                nearest.setIndex(nextIndex++);
            } else if (secNearest != null && !visited.contains(secNearest)) {
                stack.push(secNearest);
                visited.add(secNearest);
                secNearest.setIndex(nextIndex++);
            } else {
                stack.pop();
            }
        }

        // Sort the stops according to their indexes
        // The time complexity of this operation is O(n*logn) (hopefully:)
        Collections.sort(stops);
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

    @Override
    public String toString() {
        return name;
    }
}
