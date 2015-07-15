package com.mitsubishi.simulation.input.transit;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;

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

    private TransitStop nearestStopInLine;
    private TransitStop secNearestStopInLine;

    public TransitStop(TransitStation station, int index) {
        this.station = station;
        this.index = index;
        this.nearestStopInLine = null;
        this.secNearestStopInLine = null;
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

    public TransitStop getNearestStopInLine() {
        return nearestStopInLine;
    }

    public TransitStop getSecNearestStopInLine() {
        return secNearestStopInLine;
    }

    public void setNearestStopInLine(TransitStop nearestStopInLine) {
        this.nearestStopInLine = nearestStopInLine;
    }

    public void setSecNearestStopInLine(TransitStop secNearestStopInLine) {
        this.secNearestStopInLine = secNearestStopInLine;
    }

    public double getDistanceFromStop(TransitStop stop) {
        return station.getDistanceFrom(stop.getStation());
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isMyNearestOrSecNearest(TransitStop stop) {
        return stop == nearestStopInLine || stop == secNearestStopInLine;
    }

    @Override
    public String toString() {
        return station.getName();
    }
}
