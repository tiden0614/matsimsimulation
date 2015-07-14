package com.mitsubishi.simulation.input.transit;


/**
 * Created by tiden on 7/3/2015.
 * Represents a possible transfer from a transit to another transit
 */
public class Transfer {
    private Transit fromTransit;
    private Transit toTransit;
    private TransitStop fromStop;
    private TransitStop toStop;

    public Transfer(Transit fromTransit, Transit toTransit, TransitStop fromStop, TransitStop toStop) {
        this.fromTransit = fromTransit;
        this.toTransit = toTransit;
        this.fromStop = fromStop;
        this.toStop = toStop;
    }

    public Transit getFromTransit() {
        return fromTransit;
    }

    public Transit getToTransit() {
        return toTransit;
    }

    public TransitStop getFromStop() {
        return fromStop;
    }

    public TransitStop getToStop() {
        return toStop;
    }

    @Override
    public String toString() {
        return fromTransit.getName() + ":" + fromStop.getStation().getName() + " -> " +
                toTransit.getName() + ":" + toStop.getStation().getName();
    }
}
