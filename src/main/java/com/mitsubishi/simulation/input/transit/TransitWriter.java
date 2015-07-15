package com.mitsubishi.simulation.input.transit;

import org.apache.log4j.Logger;
import org.matsim.core.utils.io.MatsimXmlWriter;

import java.util.List;

/**
 * Created by tiden on 7/15/2015.
 * This class is used to write a list of Transits into Matsim's transit files
 *
 * I still don't understand why Matsim defines both stopFacility and stop
 * if one stopFacility can only be served by one transit (because of the referenced link)
 *
 * This writer will generate a stopFacility for every stop of every transit,
 * regardless if they are actually referring to the same station
 */
public class TransitWriter extends MatsimXmlWriter {

    private static final Logger logger = Logger.getLogger(TransitWriter.class);

    private String schedulePath;
    private String vehiclesPath;
    private List<Transit> transits;

    public TransitWriter(String schedulePath, String vehiclesPath, List<Transit> transits) {
        this.schedulePath = schedulePath;
        this.vehiclesPath = vehiclesPath;
        this.transits = transits;
    }
}
