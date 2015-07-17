package com.mitsubishi.simulation.input.transit;

import org.matsim.api.core.v01.network.Link;

/**
 * Created by tiden on 7/9/2015.
 * This class represents a stop of a particular transit
 * The difference between TransitStop and TransitStation is that
 * TransitStop belongs to one and only one particular transit and has an index
 * TransitStation is a common station where multiple Transits can pass through
 */
public class TransitStop implements Comparable {
    private TransitStation station;
    private int index;

    private TransitStop linkedStopA;
    private TransitStop linkedStopB;

    public TransitStop(TransitStation station, int index) {
        this.station = station;
        this.index = index;
        this.linkedStopA = null;
        this.linkedStopB = null;
    }

    public TransitStation getStation() {
        return station;
    }

    public int getIndex() {
        return index;
    }

    public int compareTo(Object o) {
        if (o instanceof TransitStop) {
            return index - ((TransitStop) o).getIndex();
        }
        return -1;
    }

    public TransitStop getLinkedStopA() {
        return linkedStopA;
    }

    public TransitStop getLinkedStopB() {
        return linkedStopB;
    }

    public void setLinkedStopA(TransitStop linkedStopA) {
        this.linkedStopA = linkedStopA;
    }

    public void setLinkedStopB(TransitStop linkedStopB) {
        this.linkedStopB = linkedStopB;
    }

    public double getDistanceFromStop(TransitStop stop) {
        return station.getDistanceFrom(stop.getStation());
    }

    public double getAngleFromStop(TransitStop stop) {
        return station.getAngleFrom(stop.getStation());
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isMyNearestOrSecNearest(TransitStop stop) {
        return stop == linkedStopA || stop == linkedStopB;
    }

    public Link getLinkTo(TransitStop stop) {
        return station.getNode().getOutLinks().get(stop.getStation().getNode().getId());
    }

    public Link getLinkFrom(TransitStop stop) {
        return station.getNode().getInLinks().get(stop.getStation().getNode().getId());
    }

    @Override
    public String toString() {
        return station.getName();
    }
}
