package com.mitsubishi.simulation.input.route;

import com.mitsubishi.simulation.input.transit.*;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by tiden on 7/8/2015.
 * This class contains algorithms that compute routes on public transport network
 */
public class RouteOnTransit {
    private static final Logger logger = Logger.getLogger(RouteOnTransit.class);
    // 500m, 1km, 5km
    private static double[] searchDistances = {0.004521858, 0.009043716, 0.045218580};

    private TransitGraph graph;

    public RouteOnTransit(TransitGraph graph) {
        this.graph = graph;
    }

    public List<TransitRoute> getRoutes(double x1, double y1, double x2, double y2) {
        List<TransitRoute> routes = new ArrayList<TransitRoute>();
        Collection<TransitStation> startStations = null;
        double walkingDistance1;
        for (int i = 0; i < searchDistances.length && (startStations == null || startStations.size() == 0); i++) {
            walkingDistance1 = searchDistances[i];
            startStations = graph.getStations().get(x1, y1, walkingDistance1);
        }
        // cannot find a start stop for the start location
        if (startStations == null || startStations.size() == 0) {
            return routes;
        }
        Collection<TransitStation> endStations = null;
        double walkingDistance2;
        for (int i = 0; i < searchDistances.length && (endStations == null || endStations.size() == 0); i++) {
            walkingDistance2 = searchDistances[i];
            endStations = graph.getStations().get(x2, y2, walkingDistance2);
        }
        // cannot find an end stop for the end location
        if (endStations == null || endStations.size() == 0) {
            return routes;
        }
        return routes;
    }

    /**
     * Use some algorithm to traverse the graph and find a path between two given transits
     */
    public List<TransitTrip> routeFromStartToEnd(
            TransitStation startStation, TransitStation endStation, Transit startTransit, Transit endTransit) {
        List<TransitTrip> ret = new ArrayList<TransitTrip>();
        if (startTransit.equals(endTransit)) {
            ret.add(new TransitTrip(null, null, startTransit));
            return ret;
        }
        Stack<Transit> transitStack = new Stack<Transit>();
        Stack<Transfer> transferStack = new Stack<Transfer>();
        Set<Transit> transitSet = new HashSet<Transit>();

        transitSet.add(startTransit);
        transitStack.push(startTransit);

        TransitStop startStop = startTransit.getStopFromStation(startStation);
        if (startStop == null) {
            // the start station does not belong to the start transit; return empty list
            return ret;
        }
        transferStack.push(new Transfer(startTransit, null, startStop, null));

        while (!transitStack.isEmpty()) {
            Transit currentTransit = transitStack.peek();
            TransitStation currentStation = transferStack.peek().getFromStop().getStation();
            if (currentTransit.equals(endTransit)) {
                break;
            }
            boolean foundNextMove = false;
            // find possible transfers that has not been visited
            for (Transfer transfer : currentTransit.computeForwardPossibleTransfers(currentStation)) {
                Transit toTransit = transfer.getToTransit();
                if (!transitSet.contains(toTransit)) {
                    transitSet.add(toTransit);
                    transitStack.push(toTransit);
                    transferStack.push(transfer);
                    foundNextMove = true;
                    break;
                }
            }
            if (foundNextMove) {
                continue;
            }
            if (!transitStack.isEmpty()) {
                transitStack.pop();
                transferStack.pop();
            }
        }

        List<TransitTrip> trips = new ArrayList<TransitTrip>();
        trips.add(new TransitTrip(startStation, null, startTransit));
        for (int i = 1; i < transitStack.size(); i++) {
            Transit t = transitStack.elementAt(i);
            Transfer transfer = transferStack.elementAt(i);
            trips.get(trips.size() - 1).setToStation(transfer.getFromStop().getStation());
            trips.add(new TransitTrip(transfer.getToStop().getStation(), null, t));
        }
        if (transitStack.size() > 0) {
            trips.get(trips.size() - 1).setToStation(endStation);
        }
        return trips;
    }
}
