package com.mitsubishi.simulation.input.matsimtransit;

import com.mitsubishi.simulation.input.transit.Transit;
import com.mitsubishi.simulation.input.transit.TransitStop;
import com.mitsubishi.simulation.utils.Constants;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.io.MatsimXmlWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private List<StopFacility> stopFacilities;
    private long idGen;

    public TransitWriter(String schedulePath, String vehiclesPath, List<Transit> transits) {
        this.schedulePath = schedulePath;
        this.vehiclesPath = vehiclesPath;
        this.transits = transits;
        this.idGen = 0;
        this.stopFacilities = new ArrayList<StopFacility>();
    }

    private Coord generateRandomPointAroundPoint(double x, double y) {
        double radius = Constants.WGS_DISTANCE_5M;
        Random r = new Random();
        double angle = 2 * r.nextDouble() * Math.PI;
        return new CoordImpl(x + radius * Math.cos(angle), y + radius * Math.sin(angle));
    }

    private void writeStopFacility(StopFacility stopFacility) {
        List<Tuple<String, String>> attrs = new ArrayList<Tuple<String, String>>(5);
        attrs.add(createTuple("id", stopFacility.getId()));
        attrs.add(createTuple("x", stopFacility.getX()));
        attrs.add(createTuple("y", stopFacility.getY()));
        attrs.add(createTuple("linkRefId", stopFacility.getLinkRefId()));
        String name = stopFacility.getName();
        if (name != null && !"".equals(name)) {
            attrs.add(createTuple("name", name));
        }
        writeStartTag("stopFacility", attrs, true);
    }

    private void writeRouteProfile(Transit transit, List<Id> linkIds, boolean backwards) {
        // Let's just focus on transits that have more than two stops
        assert transit.getStops().size() > 1;

        writeStartTag("routeProfile", null);

        String departureOffset = "00:01:00";
        int i = 0;
        int size = transit.getStops().size();
        int start = 0;
        int end = size - 1;
        if (backwards) {
            start = size - 1;
            end = 0;
            i = transit.getStops().size() - 1;
        }
        boolean stopLoop = false;
        StopFacility firstFacility = null;
        while (!stopLoop) {

            TransitStop stop = transit.getStops().get(i);
            // generate stop facility
            Coord coord = stop.getStation().getNode().getCoord();
            if (stop.getStation().getPassThroughTransits().size() > 1) {
                coord = generateRandomPointAroundPoint(coord.getX(), coord.getY());
            }
            StopFacility stopFacility = new StopFacility(
                    String.valueOf(idGen++),
                    coord.getX(),
                    coord.getY(),
                    stop.getStation().getName()
            );
            stopFacilities.add(stopFacility);

            List<Tuple<String, String>> stopAttrs = new ArrayList<Tuple<String, String>>(3);
            stopAttrs.add(createTuple("refId", stopFacility.getId()));

            if (i == start) {
                firstFacility = stopFacility;
            }

            if (i != start) {
                TransitStop lastStop = transit.getStops().get(i - 1);
                Id refLinkId = lastStop.getLinkTo(stop).getId();
                linkIds.add(refLinkId);
                stopFacility.setLinkRefId(refLinkId.toString());
                if (firstFacility.getLinkRefId() == null) {
                    firstFacility.setLinkRefId(refLinkId.toString());
                }

                // calculate arrivalOffset
                double distance = lastStop.getDistanceFromStop(stop);
                int seconds = (int) (((distance / Constants.WGS_DISTANCE_1KM) / transit.getSpeed()) * 3600);
                String arrivalOffset = String.format("%2d:%2d:%2d", seconds / 3600, seconds / 60, seconds % 60);
                stopAttrs.add(createTuple("arrivalOffset", arrivalOffset));

                if (i != end) {
                    stopAttrs.add(createTuple("departureOffset", departureOffset));
                }
            }

            writeStartTag("stop", stopAttrs, true);
            if (backwards) {
                i--;
                stopLoop = i < 0;
            } else {
                i++;
                stopLoop = i >= size;
            }
        }
        stopFacilities.get(0).setLinkRefId(linkIds.get(0).toString());

        writeEndTag("routeProfile");
    }

    private void writeTransitLine(Transit transit) {
        List<Tuple<String, String>> transitLineIdAttr = new ArrayList<Tuple<String, String>>(1);
        transitLineIdAttr.add(createTuple("id", transit.getName()));
        writeStartTag("transitLine", transitLineIdAttr);

        // Write forward route
        List<Tuple<String, String>> transitRouteForwardIdAttr = new ArrayList<Tuple<String, String>>(1);
        transitRouteForwardIdAttr.add(createTuple("id", "forward"));
        writeStartTag("transitRoute", transitRouteForwardIdAttr);

        writeStartTag("transportMode", null);
        writeContent(transit.getType(), false);
        writeEndTag("transportMode");


        List<Id> linkIds = new ArrayList<Id>();
        writeRouteProfile(transit, linkIds, false);

        writeEndTag("transitRoute");

        // Write forward route
        List<Tuple<String, String>> transitRouteBackwardIdAttr = new ArrayList<Tuple<String, String>>(1);
        transitRouteBackwardIdAttr.add(createTuple("id", "backward"));
        writeStartTag("transitRoute", transitRouteBackwardIdAttr);
        writeEndTag("transitRoute");

        writeEndTag("transitLine");
    }
}
