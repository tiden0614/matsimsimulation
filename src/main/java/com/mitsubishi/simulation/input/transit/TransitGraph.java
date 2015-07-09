package com.mitsubishi.simulation.input.transit;

import org.apache.log4j.Logger;
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
    private QuadTree<TransitStation> stations;
    // The search distance of nearby stations
    private double searchDistance;

    public TransitGraph(Collection<Transit> transits, Collection<TransitStation> stations) {
        // this default distance is about 500 meters in reality
        this(transits, stations, 0.004521858);
    }

    public TransitGraph(Collection<Transit> transits, Collection<TransitStation> stations, double searchDistance) {
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

        for (TransitStation stop : stations) {
            double x = stop.getNode().getCoord().getX();
            double y = stop.getNode().getCoord().getY();
            minX = Math.min(x, minX);
            maxX = Math.max(x, maxX);
            minY = Math.min(y, minY);
            maxY = Math.max(y, maxY);
        }

        this.stations = new QuadTree<TransitStation>(minX, minY, maxX, maxY);

        // build this quad tree
        for (TransitStation stop : stations) {
            double x = stop.getNode().getCoord().getX();
            double y = stop.getNode().getCoord().getY();
            this.stations.put(x, y, stop);
        }

        // build the graph
        buildGraph();
    }

    private void buildGraph() {
        for (Transit fromTransit : transits.values()) {
            Map<String, List<Transfer>> possibleTransfers = fromTransit.getPossibleTransferMap();
            for (TransitStop stop : fromTransit.getStops()) {
                // first add pass-through transits into the structure
                for (Transit passThroughTransit : stop.getStation().getPassThroughTransits()) {
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

                // find nearby stations
                double x = stop.getStation().getNode().getCoord().getX();
                double y = stop.getStation().getNode().getCoord().getY();
                for (TransitStation nearbyStation : stations.get(x, y, searchDistance)) {
                    if (nearbyStation != stop.getStation()) {
                        for (Transit toTransit : nearbyStation.getPassThroughTransits()) {
                            if (!toTransit.equals(fromTransit)) {
                                // we have found a nearby stop to add
                                List<Transfer> transfers = possibleTransfers.get(toTransit.getName());
                                TransitStop toStop = nearbyStation.getPassThroughTransitMap().get(toTransit);
                                Transfer t = new Transfer(fromTransit, toTransit, stop, toStop);
                                if (transfers == null) {
                                    transfers = new ArrayList<Transfer>();
                                    possibleTransfers.put(toTransit.getName(), transfers);
                                    transfers.add(t);
                                } else {
                                    // loop to find if there is already some stop that we marked
                                    // can transfer to this nearby stop for the given transit
                                    // if we find any, compare their distance and preserve the nearer one
                                    Iterator<Transfer> iterator = transfers.listIterator();
                                    boolean foundExistingSameDestination = false;
                                    boolean addToTransfers = false;
                                    while (iterator.hasNext()) {
                                        Transfer existingTransfer = iterator.next();
                                        if (existingTransfer.getToStop().getStation() == nearbyStation) {
                                            foundExistingSameDestination = true;
                                            if (nearbyStation.getDistanceFrom(existingTransfer.getFromStop().getStation()) >
                                                    nearbyStation.getDistanceFrom(stop.getStation())) {
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

    public QuadTree<TransitStation> getStations() {
        return stations;
    }
}
