package com.mitsubishi.simulation.input.transit;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.utils.collections.QuadTree;

import java.util.*;

/**
 * Created by tiden on 7/3/2015.
 * This class is intended to support extracting information from an OSM file
 * Routing algorithms on public transit network would need this data structure
 */
public class TransitGraph {

    private static final Logger logger = Logger.getLogger(TransitGraph.class);

    // Transits are stored in a map, indexed by their names
    private Map<String, Transit> transits;
    // Stops of each transit are stored in a quadtree, indexed by their coordinates
    private QuadTree<TransitStop> stops;
    // The search distance of nearby stops
    private double searchDistance;

    public TransitGraph(Collection<Transit> transits, Collection<TransitStop> stops) {
        // this default distance is about 500 meters in reality
        this(transits, stops, 0.004521858);
    }

    public TransitGraph(Collection<Transit> transits, Collection<TransitStop> stops, double searchDistance) {
        this.transits = new HashMap<String, Transit>();
        this.searchDistance = searchDistance;
        // load transits into the map
        for (Transit transit : transits) {
            if (this.transits.get(transit.getName()) != null) {
                // there is already one transit that has the same name in the map
                logger.info("Found a transit that has the same name " + transit.getName() + " as the one being added");
                // TODO deal with transits that have identical names
            }
            this.transits.put(transit.getName(), transit);
        }

        // initialize the quadtree
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (TransitStop stop : stops) {
            double x = stop.getNode().getCoord().getX();
            double y = stop.getNode().getCoord().getY();
            minX = Math.min(x, minX);
            maxX = Math.max(x, maxX);
            minY = Math.min(y, minY);
            maxY = Math.max(y, maxY);
        }

        this.stops = new QuadTree<TransitStop>(minX, minY, maxX, maxY);

        // build this quad tree
        for (TransitStop stop : stops) {
            double x = stop.getNode().getCoord().getX();
            double y = stop.getNode().getCoord().getY();
            this.stops.put(x, y, stop);
        }

        // build the graph
        buildGraph();
    }

    private void buildGraph() {
        for (Transit fromTransit : transits.values()) {
            Map<String, List<Transfer>> possibleTransfers = fromTransit.getPossibleTransfers();
            for (TransitStop stop : fromTransit.getStops()) {
                // first add pass-through transits into the structure
                for (Transit passThroughTransit : stop.getPassThroughs()) {
                    if (!passThroughTransit.equals(fromTransit)) {
                        Transfer t = new Transfer(fromTransit, passThroughTransit, stop, stop);
                        List<Transfer> transfers = possibleTransfers.get(passThroughTransit.getName());
                        if (transfers == null) {
                            transfers = new ArrayList<Transfer>();
                            possibleTransfers.put(passThroughTransit.getName(), transfers);
                        }
                        transfers.add(t);
                    }
                }

                // find nearby stops
                double x = stop.getNode().getCoord().getX();
                double y = stop.getNode().getCoord().getY();
                for (TransitStop nearbyStop : stops.get(x, y, searchDistance)) {
                    if (nearbyStop != stop) {
                        for (Transit toTransit : nearbyStop.getPassThroughs()) {
                            if (!toTransit.equals(fromTransit)) {
                                // we have found a nearby stop to add
                                List<Transfer> transfers = possibleTransfers.get(toTransit.getName());
                                Transfer t = new Transfer(fromTransit, toTransit, stop, nearbyStop);
                                if (transfers == null) {
                                    transfers = new ArrayList<Transfer>();
                                    possibleTransfers.put(toTransit.getName(), transfers);
                                    transfers.add(t);
                                } else {
                                    // loop to find if there is already some stop that we marked
                                    // can transfer to this nearby stop for the given transit
                                    // if we find any, compare their distance and preserve the nearer one
                                    Iterator<Transfer> iterator = transfers.listIterator();
                                    Transfer existingTransfer;
                                    boolean foundExistingSameDestination = false;
                                    boolean addToTransfers = false;
                                    while ((existingTransfer = iterator.next()) != null) {
                                        if (existingTransfer.getToStop() == nearbyStop) {
                                            foundExistingSameDestination = true;
                                            if (nearbyStop.getDistanceFrom(existingTransfer.getFromStop()) >
                                                    nearbyStop.getDistanceFrom(stop)) {
                                                iterator.remove();
                                                addToTransfers = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (!foundExistingSameDestination || addToTransfers) {
                                        transfers.add(t);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public Map<String, Transit> getTransits() {
        return transits;
    }

    public QuadTree<TransitStop> getStops() {
        return stops;
    }
}
