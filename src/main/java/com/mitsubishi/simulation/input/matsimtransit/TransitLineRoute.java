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

    private static final double DISTANCE_1M = Constants.get1MForCoordSystem(Transit.ACCEPT_COORD_SYSTEM);

    private static List<StopFacility> stopFacilities = new ArrayList<StopFacility>();
    private static int idGen = 0;

    private boolean duplex;
    private String transportMode;
    private List<TransitLineRouteStop> routeStops;
    private List<String> routeLinks;
    private List<TransitLineRouteDeparture> departures;
    private String id;
    private int startTime;
    private int endTime;
    private int interval;

    public static List<StopFacility> getStopFacilities() {
        return stopFacilities;
    }

    public TransitLineRoute(Transit transit, boolean backward, int startTime, int endTime, int interval) {
        this.duplex = false;
        this.routeStops = new ArrayList<>();
        this.routeLinks = new ArrayList<>();
        this.departures = new ArrayList<>();
        this.startTime = startTime;
        this.endTime = endTime;
        this.interval = interval;
        init(transit, backward);
    }

    private void init(Transit transit, boolean backwards) {
        // Let's just focus on transits that have more than two stops
        assert transit.getStops().size() > 1;

        setDuplex(transit.isDuplexTransit());
        setTransportMode(transit.getType());
        setId(backwards ? "backward" : "forward");

        int departureOffset = 30;
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
                routeStop.setDepartureOffset(0);
                Id refLinkId = stop.getLinkTo(stop).getId();
                routeLinks.add(refLinkId.toString());
                stopFacility.setLinkRefId(refLinkId.toString());
            }

            if (i != start) {
                int last = backwards? i + 1 : i - 1;
                TransitStop lastStop = transit.getStops().get(last);
                Id refLinkId = lastStop.getLinkTo(stop).getId();
                routeLinks.add(refLinkId.toString());
                stopFacility.setLinkRefId(refLinkId.toString());

                // calculate arrivalOffset
                double distance = lastStop.getDistanceFromStop(stop) / (DISTANCE_1M * 1000);
                int seconds = (int) ((distance / transit.getSpeed()) * 3600);
                if (seconds < departureOffset) {
                    // let's make sure that matsim doesn't add a midnight to this time T_T
                    seconds += departureOffset;
                }
                routeStop.setArrivalOffset(seconds);

                if (i != end) {
                    routeStop.setDepartureOffset(departureOffset);
                }
            }
            if (i == end) {
                Id refLinkId = stop.getLinkTo(stop).getId();
                routeLinks.add(refLinkId.toString());
                stopFacility.setLinkRefId(refLinkId.toString());
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


        // generate transit departures
        for (int t = startTime; t < endTime; t += interval) {
            TransitLineRouteDeparture departure = new TransitLineRouteDeparture(
                    String.valueOf(idGen++),
                    String.format("%02d:%02d:%02d", t / 3600, (t / 60) % 60, t % 60),
                    transit.getName() + "_" + id + "_" + idGen++
            );
            departures.add(departure);
        }
    }

    private Coord generateRandomPointAroundPoint(double x, double y) {
        double radius = Constants.WGS_DISTANCE_5M * 4;
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
