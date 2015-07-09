package com.mitsubishi.simulation.input.route;

import com.mitsubishi.simulation.input.transit.Transfer;
import com.mitsubishi.simulation.input.transit.Transit;
import com.mitsubishi.simulation.input.transit.TransitGraph;
import com.mitsubishi.simulation.input.transit.TransitStop;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by tiden on 7/8/2015.
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
        Collection<TransitStop> startStops = null;
        double walkingDistance1;
        for (int i = 0; i < searchDistances.length && (startStops == null || startStops.size() == 0); i++) {
            walkingDistance1 = searchDistances[i];
            startStops = graph.getStops().get(x1, y1, walkingDistance1);
        }
        // cannot find a start stop for the start location
        if (startStops == null || startStops.size() == 0) {
            return routes;
        }
        Collection<TransitStop> endStops = null;
        double walkingDistance2;
        for (int i = 0; i < searchDistances.length && (endStops == null || endStops.size() == 0); i++) {
            walkingDistance2 = searchDistances[i];
            endStops = graph.getStops().get(x2, y2, walkingDistance2);
        }
        // cannot find an end stop for the end location
        if (endStops == null || endStops.size() == 0) {
            return routes;
        }
        return routes;
    }

    /**
     * Use some algorithm to traverse the graph and find a path between two given transits
     * @param start the start transit
     * @param end the end transit
     * @return a list of trips indicating the path; note that this list needs further modification
     *         the starting stop and ending stop needs to be added to the first and last elements of the list
     */
    public List<TransitTrip> routeFromStartToEnd(Transit start, Transit end) {
        if (start.equals(end)) {
            List<TransitTrip> ret = new ArrayList<TransitTrip>();
            ret.add(new TransitTrip(null, null, start));
            return ret;
        }
        Stack<Transit> transitStack = new Stack<Transit>();
        Set<Transit> transitSet = new HashSet<Transit>();

        transitSet.add(start);
        transitStack.push(start);

        Transit currentTransit = start;
        while (!transitStack.isEmpty() && currentTransit != end) {
            boolean foundNextMove = false;
            // find possible transfers that has not been visited
            for (List<Transfer> transfers : currentTransit.getPossibleTransfers().values()) {
                if (transfers.size() > 0 && !transitSet.contains(transfers.get(0).getToTransit())) {
                    currentTransit = transfers.get(0).getToTransit();
                    transitSet.add(currentTransit);
                    transitStack.push(currentTransit);
                    foundNextMove = true;
                    break;
                }
            }
            if (foundNextMove) {
                continue;
            }
            if (!transitStack.isEmpty()) {
                currentTransit = transitStack.pop();
            }
        }

        List<TransitTrip> trips = new ArrayList<TransitTrip>();
        TransitStop lastStop = null;
        for (int i = 0; i < transitStack.size() - 1; i++) {
            Transit t1 = transitStack.elementAt(i);
            Transit t2 = transitStack.elementAt(i + 1);
            List<Transfer> transfers = t1.getPossibleTransfers().get(t2.getName());
            // if we have entered this loop, it means that we found a path
            // and the transfer list cannot be null or empty
            assert transfers != null;
            assert transfers.size() > 0;
            TransitStop toStop = transfers.get(0).getToStop();
            // transfer at the first possible stop
            trips.add(new TransitTrip(lastStop, toStop, t1));
            lastStop = toStop;
        }
        if (transitStack.size() > 0) {
            trips.add(new TransitTrip(lastStop, null, end));
        }
        return trips;
    }
}
