package com.mitsubishi.simulation.input.route;

import com.mitsubishi.simulation.input.transit.TransitGraph;
import org.apache.log4j.Logger;

/**
 * Created by tiden on 7/8/2015.
 */
public class RouteOnTransit {
    private static final Logger logger = Logger.getLogger(RouteOnTransit.class);

    private TransitGraph graph;

    public RouteOnTransit(TransitGraph graph) {
        this.graph = graph;
    }


}
