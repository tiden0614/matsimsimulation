package com.mitsubishi.simulation.input.route;

import com.mitsubishi.simulation.input.transit.Transit;
import com.mitsubishi.simulation.input.transit.TransitStop;

/**
 * Created by tiden on 7/8/2015.
 * This class represents one trip in a route
 */
public class TransitTrip {
    private TransitStop fromStop;
    private TransitStop toStop;
    private Transit transit;

    public TransitTrip(TransitStop fromStop, TransitStop toStop, Transit transit) {
        this.fromStop = fromStop;
        this.toStop = toStop;
        this.transit = transit;
    }

    public TransitStop getFromStop() {
        return fromStop;
    }

    public TransitStop getToStop() {
        return toStop;
    }

    public Transit getTransit() {
        return transit;
    }
}
