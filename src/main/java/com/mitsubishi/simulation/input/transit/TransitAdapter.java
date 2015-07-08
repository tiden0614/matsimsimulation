package com.mitsubishi.simulation.input.transit;

import com.mitsubishi.simulation.input.transit.Transit;
import com.mitsubishi.simulation.input.transit.TransitStop;
import org.matsim.core.utils.collections.QuadTree;

import java.util.Iterator;
import java.util.List;

/**
 * Created by tiden on 7/6/2015.
 * This interface abstracts different adapters to convert
 * data from various sources into transits and transit stops
 */
public interface TransitAdapter {
    List<Transit> getTransits();
    List<TransitStop> getTransitStops();
}
