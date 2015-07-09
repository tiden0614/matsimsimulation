package com.mitsubishi.simulation.input.transit;

import org.matsim.api.core.v01.network.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by tiden on 7/6/2015.
 * Represents a transit stop
 */
public class TransitStation {
    private String name;
    private Node node;
    private Map<Transit, TransitStop> passThroughTransitMap;

    public TransitStation(String name, Node node) {
        this.name = name;
        this.node = node;
        this.passThroughTransitMap = new HashMap<Transit, TransitStop>();
    }

    public TransitStation(Node node) {
        this(null, node);
    }

    public String getName() {
        return name;
    }

    public Node getNode() {
        return node;
    }

    public Set<Transit> getPassThroughTransits() {
        return passThroughTransitMap.keySet();
    }

    public Map<Transit, TransitStop> getPassThroughTransitMap() {
        return passThroughTransitMap;
    }

    public double getDistanceFrom(TransitStation anotherStop) {
        if (anotherStop == this || anotherStop.getNode() == node) {
            return 0;
        }
        double x1 = node.getCoord().getX();
        double y1 = node.getCoord().getY();
        double x2 = anotherStop.getNode().getCoord().getX();
        double y2 = anotherStop.getNode().getCoord().getY();
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }
}
