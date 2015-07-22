package com.mitsubishi.simulation.input.matsimtransit;

import com.mitsubishi.simulation.input.transit.Transit;
import com.mitsubishi.simulation.input.transit.TransitStop;
import com.mitsubishi.simulation.utils.Constants;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.core.utils.geometry.CoordImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by tiden on 7/22/2015.
 */
public class TransitLineRoute {

    private static List<StopFacility> stopFacilities = new ArrayList<StopFacility>();
    private static int idGen = 0;

    private boolean duplex;
    private String transportMode;
    private List<TransitLineRouteStop> routeStops;
    private List<String> routeLinks;
    private List<TransitLineRouteDeparture> departures;
    private String id;

    public static List<StopFacility> getStopFacilities() {
        return stopFacilities;
    }

    public TransitLineRoute(Transit transit, boolean backward) {
        this.duplex = false;
        this.routeStops = new ArrayList<TransitLineRouteStop>();
        this.routeLinks = new ArrayList<String>();
        this.departures = new ArrayList<TransitLineRouteDeparture>();
        init(transit, backward);
    }

    private void init(Transit transit, boolean backwards) {
        // Let's just focus on transits that have more than two stops
        assert transit.getStops().size() > 1;

        setDuplex(transit.isDuplexTransit());
        setTransportMode(transit.getType());
        setId(backwards ? "backward" : "forward");

        int departureOffset = 60;
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

            TransitLineRouteStop routeStop = new TransitLineRouteStop();
            routeStop.setRefId(stopFacility.getId());

            if (i == start) {
                firstFacility = stopFacility;
                routeStop.setDepartureOffset(0);
            }

            if (i != start) {
                int next = backwards? i + 1 : i - 1;
                TransitStop lastStop = transit.getStops().get(next);
                Id refLinkId = lastStop.getLinkTo(stop).getId();
                routeLinks.add(refLinkId.toString());
                stopFacility.setLinkRefId(refLinkId.toString());
                if (firstFacility.getLinkRefId() == null) {
                    firstFacility.setLinkRefId(refLinkId.toString());
                }

                // calculate arrivalOffset
                double distance = lastStop.getDistanceFromStop(stop);
                int seconds = (int) (((distance / Constants.WGS_DISTANCE_1KM) / transit.getSpeed()) * 3600);
                routeStop.setArrivalOffset(seconds);

                if (i != end) {
                    routeStop.setDepartureOffset(departureOffset);
                }
            }

            routeStops.add(routeStop);
            if (backwards) {
                i--;
                stopLoop = i < 0;
            } else {
                i++;
                stopLoop = i >= size;
            }
        }
        stopFacilities.get(0).setLinkRefId(routeLinks.get(0));


        // generate transit departures
        int firstEmit = 6 * 3600; // 6:00 AM
        int lastEmit = 21 * 3600; // 9:00 PM
        int emitInterval = 15 * 60; // emit a transit every 15 minutes

        for (int t = firstEmit; t < lastEmit; t += emitInterval) {
            TransitLineRouteDeparture departure = new TransitLineRouteDeparture(
                    String.valueOf(idGen++),
                    String.format("%02d:%02d:%02d", t / 3600, (t / 60) % 60, t % 60),
                    transit.getName() + "_" + id + "_" + idGen++
            );
            departures.add(departure);
        }
    }

    private Coord generateRandomPointAroundPoint(double x, double y) {
        double radius = Constants.WGS_DISTANCE_5M;
        Random r = new Random();
        double angle = 2 * r.nextDouble() * Math.PI;
        return new CoordImpl(x + radius * Math.cos(angle), y + radius * Math.sin(angle));
    }

    public void setDuplex(boolean duplex) {
        this.duplex = duplex;
    }

    public void setTransportMode(String transportMode) {
        this.transportMode = transportMode;
    }

    public boolean isDuplex() {
        return duplex;
    }

    public String getTransportMode() {
        return transportMode;
    }

    public List<TransitLineRouteStop> getRouteStops() {
        return routeStops;
    }

    public List<String> getRouteLinks() {
        return routeLinks;
    }

    public List<TransitLineRouteDeparture> getDepartures() {
        return departures;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
