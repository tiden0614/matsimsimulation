package com.mitsubishi.simulation.test;

import com.mitsubishi.simulation.input.network.NetworkUtils;
import com.mitsubishi.simulation.input.osm.OSMRelationTransitAdapter;
import com.mitsubishi.simulation.input.transit.Transit;
import com.mitsubishi.simulation.input.transit.TransitStation;
import com.mitsubishi.simulation.utils.Constants;
import org.junit.Test;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by tiden on 7/7/2015.
 */
public class TestOSMRelationTransitAdapter {

    @Test
    public void testAdapter() {
        Network network = NetworkUtils.convertOSMToNetwork(
                Constants.OSM_TEST_INPUT, TransformationFactory.WGS84, Transit.ACCEPT_COORD_SYSTEM
        );
        OSMRelationTransitAdapter adapter = new OSMRelationTransitAdapter(Constants.OSM_TEST_INPUT, network);

        List<Transit> transits = adapter.getTransits();
        assertTrue(transits.size() > 0);

        List<TransitStation> stops = adapter.getTransitStations();
        assertTrue(stops.size() > 0);

    }
}
