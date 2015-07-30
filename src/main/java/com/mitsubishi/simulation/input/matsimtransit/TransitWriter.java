package com.mitsubishi.simulation.input.matsimtransit;

import com.mitsubishi.simulation.input.transit.Transit;
import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.io.MatsimXmlWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private static final String DEFAULT_SCHEDULE_FILE_NAME = "transitSchedule.xml";
    private static final String DEFAULT_VEHICLES_FILE_NAME = "transitVehicles.xml";

    private String schedulePath;
    private String vehiclesPath;
    private List<TransitLine> transitLines;

    public TransitWriter(String outputDir, List<Transit> transits, int startTime, int endTime, int interval) {
        this(
                outputDir + System.getProperty("file.separator") + DEFAULT_SCHEDULE_FILE_NAME,
                outputDir + System.getProperty("file.separator") + DEFAULT_VEHICLES_FILE_NAME,
                transits,
                startTime,
                endTime,
                interval
        );
    }

    public TransitWriter(String schedulePath, String vehiclesPath, List<Transit> transits,
                         int startTime, int endTime, int interval) {
        this.schedulePath = schedulePath;
        this.vehiclesPath = vehiclesPath;
        this.transitLines = new ArrayList<>();

        // build transit lines
        transitLines.addAll(transits.stream().map(transit -> new TransitLine(transit, startTime, endTime, interval)).collect(Collectors.toList()));
    }

    private void writeStopFacility(StopFacility stopFacility) {
        List<Tuple<String, String>> attrs = new ArrayList<>(5);
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

    private void writeStop(TransitLineRouteStop stop) {
        List<Tuple<String, String>> attrs = new ArrayList<>(3);
        attrs.add(createTuple("refId", stop.getRefId()));
        if (stop.getArrivalOffset() != null) {
            attrs.add(createTuple("arrivalOffset", stop.getArrivalOffset()));
        }
        if (stop.getDepartureOffset() != null) {
            attrs.add(createTuple("departureOffset", stop.getDepartureOffset()));
        }
        writeStartTag("stop", attrs, true);
    }

    private void writeDeparture(TransitLineRouteDeparture departure) {
        List<Tuple<String, String>> attrs = new ArrayList<>(3);
        attrs.add(createTuple("id", departure.getId()));
        attrs.add(createTuple("departureTime", departure.getDepartureTime()));
        attrs.add(createTuple("vehicleRefId", departure.getVehicleRefId()));
        writeStartTag("departure", attrs, true);
    }

    private void writeTransitLineRoute(TransitLineRoute route) {
        List<Tuple<String, String>> attrs = new ArrayList<>();

        // write the open tag for transitRoute
        attrs.add(createTuple("id", route.getId()));
        writeStartTag("transitRoute", attrs);

        // write the transportMode
        writeStartTag("transportMode", null);
        writeContent(route.getTransportMode(), false);
        writeEndTag("transportMode");

        // write routProfile
        writeStartTag("routeProfile", null);
        route.getRouteStops().forEach(this::writeStop);
        writeEndTag("routeProfile");

        // write route links
        writeStartTag("route", null);
        for (String linkRefId : route.getRouteLinks()) {
            attrs.clear();
            attrs.add(createTuple("refId", linkRefId));
            writeStartTag("link", attrs, true);
        }
        writeEndTag("route");

        // write departures
        writeStartTag("departures", null);
        route.getDepartures().forEach(this::writeDeparture);
        writeEndTag("departures");

        // write the end tag for transitRoute
        writeEndTag("transitRoute");
    }

    private void writeTransitLine(TransitLine line) {
        List<Tuple<String, String>> transitLineIdAttr = new ArrayList<>(1);
        transitLineIdAttr.add(createTuple("id", line.getId()));
        writeStartTag("transitLine", transitLineIdAttr);

        if (line.getForwardRoute() != null) {
            writeTransitLineRoute(line.getForwardRoute());
        }

        if (line.getBackwardRoute() != null) {
            writeTransitLineRoute(line.getBackwardRoute());
        }

        writeEndTag("transitLine");
    }

    public void writeTransitSchedule() {
        openFile(this.schedulePath);
        writeXmlHead();
        writeDoctype("transitSchedule", "http://www.matsim.org/files/dtd/transitSchedule_v1.dtd");
        writeStartTag("transitSchedule", null);

        // write transitStops
        writeStartTag("transitStops", null);
        TransitLineRoute.getStopFacilities().forEach(this::writeStopFacility);
        writeEndTag("transitStops");

        // write transitLines
        transitLines.forEach(this::writeTransitLine);

        writeEndTag("transitSchedule");
        close();
    }

    public void writeTransitVehicles() {
        openFile(this.vehiclesPath);
        writeXmlHead();
        List<Tuple<String, String>> attrs = new ArrayList<>();
        attrs.add(createTuple("xmlns", "http://www.matsim.org/files/dtd"));
        attrs.add(createTuple("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"));
        attrs.add(createTuple("xsi:schemaLocation",
                "http://www.matsim.org/files/dtd http://www.matsim.org/files/dtd/vehicleDefinitions_v1.0.xsd"));
        writeStartTag("vehicleDefinitions", attrs);

        // write vehicleTypes
        for (TransitLine line : transitLines) {
            attrs.clear();
            attrs.add(createTuple("id", line.getId()));
            writeStartTag("vehicleType", attrs);

            writeStartTag("description", null);
            // FIXME we need a real description here
            writeContent(line.getId(), false);
            writeEndTag("description");

            writeStartTag("capacity", null);
            attrs.clear();
            attrs.add(createTuple("persons", line.getSeats()));
            writeStartTag("seats", attrs, true);

            attrs.clear();
            attrs.add(createTuple("persons", line.getStandingRoom()));
            writeStartTag("standingRoom", attrs, true);
            writeEndTag("capacity");

            attrs.clear();
            attrs.add(createTuple("meter", line.getLength()));
            writeStartTag("length", attrs, true);

            writeEndTag("vehicleType");
        }


        // write actual vehicles
        for (TransitLine line : transitLines) {
            attrs.clear();
            attrs.add(createTuple("type", line.getId()));
            attrs.add(createTuple("id", ""));
            if (line.getForwardRoute() != null) {
                for (TransitLineRouteDeparture departure : line.getForwardRoute().getDepartures()) {
                    attrs.set(1, createTuple("id", departure.getVehicleRefId()));
                    writeStartTag("vehicle", attrs, true);
                }
            }
            if (line.getBackwardRoute() != null) {
                for (TransitLineRouteDeparture departure : line.getBackwardRoute().getDepartures()) {
                    attrs.set(1, createTuple("id", departure.getVehicleRefId()));
                    writeStartTag("vehicle", attrs, true);
                }
            }
        }

        writeEndTag("vehicleDefinitions");
        close();
    }
}
