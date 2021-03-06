package com.mitsubishi.simulation.test;

import com.mitsubishi.simulation.input.network.NetworkUtils;
import com.mitsubishi.simulation.input.osm.OSMRelationTransitAdapter;
import com.mitsubishi.simulation.input.route.RouteOnTransit;
import com.mitsubishi.simulation.input.route.TransitTrip;
import com.mitsubishi.simulation.input.transit.Transit;
import com.mitsubishi.simulation.input.transit.TransitGraph;
import com.mitsubishi.simulation.input.transit.TransitStation;
import com.mitsubishi.simulation.utils.Constants;
import org.junit.Before;
import org.junit.Test;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by tiden on 7/9/2015.
 * Test case for routing algorithms
 */
public class TestRouteOnTransit {

    private TransitGraph graph;
    private RouteOnTransit routeOnTransit;

    @Before
    public void setup() {
        Network network = NetworkUtils.convertOSMToNetwork(
                Constants.OSM_TEST_INPUT, TransformationFactory.WGS84, Transit.ACCEPT_COORD_SYSTEM
        );
        OSMRelationTransitAdapter adapter = new OSMRelationTransitAdapter(Constants.OSM_TEST_INPUT, network);
        this.graph = new TransitGraph(
                adapter.getTransits(), adapter.getTransitStations(), Constants.WGS_DISTANCE_VERY_SMALL
        );
        this.routeOnTransit = new RouteOnTransit(this.graph);
    }

    @Test
    public void testRouteFromStartToEnd() {
        Transit t0 = graph.getTransits().get("transit0");
        Transit t3 = graph.getTransits().get("transit3");
        TransitStation s1 = graph.getStations().get(0.0000, 0.0000);
        TransitStation s2 = graph.getStations().get(0.0150, 0.0001);
        List<TransitTrip> trips = this.routeOnTransit.routeFromStartToEnd(s1, s2, t0, t3);
        assertTrue("there should be a path from transit0 to transit3", trips.size() > 0);
    }
}
