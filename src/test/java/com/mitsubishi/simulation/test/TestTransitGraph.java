package com.mitsubishi.simulation.test;

import com.mitsubishi.simulation.input.network.NetworkUtils;
import com.mitsubishi.simulation.input.osm.OSMRelationTransitAdapter;
import com.mitsubishi.simulation.input.transit.Transit;
import com.mitsubishi.simulation.input.transit.TransitGraph;
import com.mitsubishi.simulation.input.transit.TransitStop;
import org.junit.Before;
import org.junit.Test;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Created by tiden on 7/7/2015.
 * This test case tests transit graph build algorithms
 */
public class TestTransitGraph {


    private List<Transit> transits;
    private List<TransitStop> stops;

    @Before
    public void setup() {
        Network network = NetworkUtils.convertOSMToNetwork(
                TestConstants.OSM_TEST_INPUT, TransformationFactory.WGS84, TransformationFactory.WGS84
        );
        OSMRelationTransitAdapter adapter = new OSMRelationTransitAdapter(TestConstants.OSM_TEST_INPUT, network);
        transits = adapter.getTransits();
        stops = adapter.getTransitStops();
    }

    @Test
    public void testTransitGraph() {
        TransitGraph graph = new TransitGraph(transits, stops, TestConstants.DISTANCE_VERY_SMALL);

        Map<String, Transit> transitMap = graph.getTransits();
        assertTrue(transitMap.size() == 6);

        Transit transit0 = transitMap.get("transit0");
        assertTrue("transit0 should have 2 possible transfers",
                transit0.getPossibleTransfers().size() == 2);

        Transit transit4 = transitMap.get("transit4");
        assertTrue("transit4 should have 4 possible transfers via pass through stops",
                transit4.getPossibleTransfers().size() == 4);

        Transit transit5 = transitMap.get("transit5");
        assertTrue("transit5 should have 4 possible transfers via nearby stops",
                transit5.getPossibleTransfers().size() == 4);

        QuadTree<TransitStop> tree = graph.getStops();
        assertTrue("there should be 28 stops in total", tree.size() == 28);
    }
}
