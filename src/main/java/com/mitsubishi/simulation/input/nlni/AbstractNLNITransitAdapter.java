package com.mitsubishi.simulation.input.nlni;

import com.mitsubishi.simulation.input.transit.TransitAdapter;
import org.matsim.core.utils.io.MatsimXmlParser;

/**
 * Created by tiden on 7/14/2015.
 */
public abstract class AbstractNLNITransitAdapter extends MatsimXmlParser implements TransitAdapter {

    // unfortunately we cannot get the inner ID generator of Matsim network
    // although it is possible to access the ID generator by reflection
    // it is dangerous to do so since the IDs may crush in someway
    // assume that the higher ids are not used, which is *often* true
    // having said that, better solution is expected

    private static long idGen = Long.MAX_VALUE;

    protected synchronized long getNextId() {
        return idGen--;
    }
}
