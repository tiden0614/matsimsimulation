package com.mitsubishi.simulation.input.transit;

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

    public TransitStop(TransitStation station, int index) {
        this.station = station;
        this.index = index;
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
}
