package com.mitsubishi.simulation.input.route;

import com.mitsubishi.simulation.input.transit.Transit;
import com.mitsubishi.simulation.input.transit.TransitStation;

/**
 * Created by tiden on 7/8/2015.
 * This class represents one trip in a route
 */
public class TransitTrip {
    private TransitStation fromStation;
    private TransitStation toStation;
    private Transit transit;

    public TransitTrip(TransitStation fromStation, TransitStation toStation, Transit transit) {
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.transit = transit;
    }

    public TransitStation getFromStation() {
        return fromStation;
    }

    public TransitStation getToStation() {
        return toStation;
    }

    public Transit getTransit() {
        return transit;
    }

    public void setFromStation(TransitStation fromStation) {
        this.fromStation = fromStation;
    }

    public void setToStation(TransitStation toStation) {
        this.toStation = toStation;
    }

    public void setTransit(Transit transit) {
        this.transit = transit;
    }
}
