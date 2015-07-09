package com.mitsubishi.simulation.input.transit;

import java.util.List;

/**
 * Created by tiden on 7/6/2015.
 * This interface abstracts different adapters to convert
 * data from various sources into transits and transit stops
 */
public interface TransitAdapter {
    List<Transit> getTransits();
    List<TransitStation> getTransitStations();
}
