package com.mitsubishi.simulation.test;

import com.mitsubishi.simulation.input.network.NetworkUtils;
import com.mitsubishi.simulation.input.osm.OSMRelationTransitAdapter;
import com.mitsubishi.simulation.input.transit.Transit;
import com.mitsubishi.simulation.input.transit.TransitStop;
import org.junit.Test;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by tiden on 7/7/2015.
 */
public class TestOSMRelationTransitAdapter {
    private final String input = "testinput/test-osm-basic.xml";

    @Test
    public void testAdapter() {
        Network network = NetworkUtils.convertOSMToNetwork(
                input, TransformationFactory.WGS84, TransformationFactory.WGS84
        );
        OSMRelationTransitAdapter adapter = new OSMRelationTransitAdapter(input, network);

        List<Transit> transits = adapter.getTransits();
        assertTrue(transits.size() > 0);

        List<TransitStop> stops = adapter.getTransitStops();
        assertTrue(stops.size() > 0);

    }
}
