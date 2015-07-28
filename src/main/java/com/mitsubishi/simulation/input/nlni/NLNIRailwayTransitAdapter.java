package com.mitsubishi.simulation.input.nlni;

import com.mitsubishi.simulation.input.transit.Transit;
import com.mitsubishi.simulation.input.transit.TransitStation;
import com.mitsubishi.simulation.input.transit.TransitStop;
import com.mitsubishi.simulation.utils.Constants;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.network.NodeImpl;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.WGS84toCH1903LV03;
import org.xml.sax.Attributes;

import java.util.*;

/**
 * Created by tiden on 7/14/2015.
 * This adapter transforms National Land Numerical Information's railway data
 * into Transit and TransitStation.
 */
public class NLNIRailwayTransitAdapter extends AbstractNLNITransitAdapter {

    private static final Logger logger = Logger.getLogger(NLNIRailwayTransitAdapter.class);
    private static final int LONG_DISTANCE_FACTOR = 10;
    private static final int TRAIN_SPEED = 120; // 120km/h

    private List<Transit> transits;
    private Map<String, TransitStation> transitStations;

    private NLNICurve currentCurve;
    private Map<String, NLNICurve> curves;

    private NLNIRailwayStation currentStation;
    private Map<String, NLNIRailwayStation> stations;

    private Map<String, NLNIRailwayLine> lines;

    private Network network;

    private Set<String> trainMode;
    private Set<String> trainAndCarMode;
    private QuadTree.Rect boundary;

    private CoordinateTransformation transformation = new WGS84toCH1903LV03();

    public NLNIRailwayTransitAdapter(String inputFile, Network network) {
        this(inputFile, network, null);
    }

    public NLNIRailwayTransitAdapter(String inputFile, Network network, QuadTree.Rect boundary) {

        this.network = network;
//        this.boundary = boundary;

        this.boundary = Constants.convertBoundryToCoordSystem(boundary, Transit.ACCEPT_COORD_SYSTEM);

        transits = new ArrayList<>();
        transitStations = new HashMap<>();
        curves = new HashMap<>();
        stations = new HashMap<>();
        lines = new HashMap<>();
        trainMode = new HashSet<>();
        trainMode.add("train");
        trainAndCarMode = new HashSet<>();
        trainAndCarMode.add(TransportMode.car);
        trainAndCarMode.add("train");
        setValidating(false);

        logger.info("Starting to load lines and stations from " + inputFile);

        // get raw types from the xml file
        parse(inputFile);

        logger.info(inputFile + " has been loaded");
        logger.info(lines.size() + " lines and " + stations.size() + " stations are loaded");


        filterRawData();


        logger.info("Converting the data into Transits and TransitStations");

        // convert the raw types into Transits and TransitStations
        convert();

        logger.info("Conversion done");
        logger.info(transits.size() + " Transits and " + transitStations.size() + " TransitStations are generated");

        filterTransitStations();
    }

    public List<Transit> getTransits() {
        return transits;
    }

    public List<TransitStation> getTransitStations() {
        List<TransitStation> retList = new ArrayList<>();
        retList.addAll(transitStations.values());
        return retList;
    }

    private void filterTransitStations() {
        for (Iterator<Map.Entry<String, TransitStation>> i = transitStations.entrySet().iterator(); i.hasNext();) {
            Map.Entry<String, TransitStation> entry = i.next();
            TransitStation s = entry.getValue();
            if (s.getPassThroughTransits().size() == 0) {
                network.removeNode(s.getNode().getId());
                i.remove();
            }
        }
    }

    private void filterRawData() {

        // If we have a boundary defined, filter out those points that fall outside the boundary
        if (boundary != null) {
            double minX = boundary.minX;
            double minY = boundary.minY;
            double maxX = boundary.maxX;
            double maxY = boundary.maxY;
            Iterator<Map.Entry<String, NLNIRailwayStation>> stationIter = stations.entrySet().iterator();
            while (stationIter.hasNext()) {
                Map.Entry<String, NLNIRailwayStation> entry = stationIter.next();
                double x = entry.getValue().getX();
                double y = entry.getValue().getY();
                if (!(minX <= x && x <= maxX && minY <= y && y <= maxY)) {
                    // mark the station as deleted and then delete it from the map
                    // the reason for marking is that we need to delete it from the line later
                    entry.getValue().setDeleted(true);
                    stationIter.remove();
                }
            }
        }

        // Split those lines who has more than one type of stations
        // Create a new line for each corresponding type of stations
        Iterator<Map.Entry<String, NLNIRailwayLine>> lineIter = lines.entrySet().iterator();
        Map<String, NLNIRailwayLine> toBeAdded = new HashMap<>();
        while (lineIter.hasNext()) {
            Map.Entry<String, NLNIRailwayLine> entry = lineIter.next();
            Map<String, List<NLNIRailwayStation>> stationCategories = new HashMap<>();
            NLNIRailwayLine l = entry.getValue();
            for (Iterator<NLNIRailwayStation> sIter = l.getStations().iterator(); sIter.hasNext();) {
                NLNIRailwayStation station = sIter.next();
                if (station.isDeleted()) {
                    // If this station is marked as deleted, remove it from this line
                    sIter.remove();
                    continue;
                }
                String stationType = station.getRailwayType();
                List<NLNIRailwayStation> slist = stationCategories.get(stationType);
                if (slist == null) {
                    slist = new LinkedList<>();
                    stationCategories.put(stationType, slist);
                }
                slist.add(station);
            }
            if (l.getStations().size() < 2) {
                // This line may have none or only one station left due to the filter operation
                // If so, remove this line
                lineIter.remove();
                continue;
            }
            if (stationCategories.size() > 1) {
                lineIter.remove();
                for (Map.Entry<String, List<NLNIRailwayStation>> e : stationCategories.entrySet()) {
                    String neoName = l.getName() + "_" + e.getKey();
                    NLNIRailwayLine neoLine = new NLNIRailwayLine(neoName);
                    neoLine.getStations().addAll(e.getValue());
                    toBeAdded.put(neoName, neoLine);
                }
            }
        }
        lines.putAll(toBeAdded);
    }

    private void convert() {

        for (NLNIRailwayLine line : lines.values()) {

            Transit transit = new Transit(Transit.TRAIN, line.getName());
            transit.setDuplexTransit(true);
            // Assume the train can run at a speed of 270 km/h
            transit.setSpeed(TRAIN_SPEED);

            // create nodes and add them to the network
            // create stations
            // create stops
            List<TransitStop> stops = transit.getStops();
            for (NLNIRailwayStation s : line.getStationSet()) {
                TransitStation transitStation = transitStations.get(s.getId());
                if (transitStation == null) {
                    // add new station to the data structure
                    double x = s.getX();
                    double y = s.getY();
                    NodeImpl node = new NodeImpl(new IdImpl(s.getId()));
                    node.setCoord(new CoordImpl(x, y));
                    synchronized (AbstractNLNITransitAdapter.class) {
                        network.addNode(node);
                    }
                    transitStation = new TransitStation(s.getName(), node);
                    transitStations.put(s.getId(), transitStation);
                }
                TransitStop stop = new TransitStop(transitStation, Integer.MIN_VALUE);
                stops.add(stop);
            }

            // rearrange the stops of this transit
//            transit.rearrangeStops();
            Set<TransitStop> stopsBeingKickedOut = transit.neoRearrangeStops();

            // after rearrangement, there might be some transits that we don't need anymore
            if (transit.getStops().size() >= 2) {
                transits.add(transit);
            }

            int index = 0;
            while (stopsBeingKickedOut != null && stopsBeingKickedOut.size() > 1) {
                Transit kickedOut = new Transit(Transit.TRAIN, transit.getName() + "_" + index++);
                kickedOut.setDuplexTransit(true);
                // Assume the train can run at a speed of 270 km/h
                kickedOut.setSpeed(TRAIN_SPEED);
                kickedOut.getStops().addAll(stopsBeingKickedOut);

                stopsBeingKickedOut = kickedOut.neoRearrangeStops();

                if (kickedOut.getStops().size() >= 2) {
                    transits.add(kickedOut);
                }
            }
        }

        transits.forEach(this::buildLinks);

    }

    private void buildLinks(Transit transit) {
        // create links and add them to the network
        TransitStation lastStation = null;
        for (TransitStop stop : transit.getStops()) {
            TransitStation transitStation = stop.getStation();
            transitStation.getPassThroughTransitMap().put(transit, stop);
            Set<String> allowedMode = trainMode;
            if (lastStation != null) {

                double distance = lastStation.getDistanceFrom(transitStation);
                if (distance > Constants.WGS_DISTANCE_1KM * LONG_DISTANCE_FACTOR) {
                    logger.warn(String.format("Found a super long distance (>%dKM) %.2fKM " +
                            "in line %s between stations %s -> %s", LONG_DISTANCE_FACTOR,
                            distance / Constants.WGS_DISTANCE_1KM, transit.getName(), lastStation.getName(),
                            transitStation.getName()));
                    allowedMode = trainAndCarMode;
                }
                // since all data from NLNI are actual duplex trains
                // add both links from station A to station B and station B to station A
                Link link1 = network.getFactory().createLink(
                        new IdImpl(getNextId()), lastStation.getNode(), transitStation.getNode()
                );
                Link link2 = network.getFactory().createLink(
                        new IdImpl(getNextId()), transitStation.getNode(), lastStation.getNode()
                );
                link1.setAllowedModes(allowedMode);
                link2.setAllowedModes(allowedMode);
                synchronized (AbstractNLNITransitAdapter.class) {
                    network.addLink(link1);
                    network.addLink(link2);
                }
            } else {
                // This is the first stop of this Transit
                // It seems we have to give a link that goes from the node of the first stop
                // to the node of the first stop (the same node) to make the simulation work
                Link linkToSelf = network.getFactory().createLink(
                        new IdImpl(getNextId()), stop.getStation().getNode(), stop.getStation().getNode()
                );
                linkToSelf.setAllowedModes(allowedMode);
                synchronized (AbstractNLNITransitAdapter.class) {
                    network.addLink(linkToSelf);
                }
            }
            lastStation = transitStation;
        }
        assert lastStation != null;
        // This is the last stop of this Transit
        // It seems we have to give a link that goes from the node of the last stop
        // to the node of the last stop (the same node) to make the simulation work
        Link linkToSelf = network.getFactory().createLink(
                new IdImpl(getNextId()), lastStation.getNode(), lastStation.getNode()
        );
        linkToSelf.setAllowedModes(trainMode);
        synchronized (AbstractNLNITransitAdapter.class) {
            network.addLink(linkToSelf);
        }
    }

    @Override
    public void startTag(String name, Attributes atts, Stack<String> context) {
        if ("Curve".equals(name)) {
            currentCurve = new NLNICurve(atts.getValue("gml:id"));
        } else if ("Station".equals(name)) {
            String id = atts.getValue("gml:id");
            currentStation = new NLNIRailwayStation(id);
        } else if ("location".equals(name) && "Station".equals(context.peek())) {
            if (currentStation == null) return;
            String refId = atts.getValue("xlink:href").substring(1);
            NLNICurve ref = curves.get(refId);
            // The x and y are actually reversed for longitude and latitude
            currentStation.setCoord(transformation.transform(new CoordImpl(ref.getYCenter(), ref.getXCenter())));
        }
    }

    @Override
    public void endTag(String name, String content, Stack<String> context) {
        if ("Curve".equals(name)) {
            // end tag for a curve
            curves.put(currentCurve.getId(), currentCurve);
        } else if ("posList".equals(name) && context.contains("Curve")) {
            // curve's position list
            for (String posStr : content.split("\\s+")) {
                if (!"".equals(posStr)) {
                    currentCurve.addNumber(Double.valueOf(posStr));
                }
            }
        }

        if (currentStation == null) {
            return;
        }

        if ("railwayLineName".equals(name) && "Station".equals(context.peek())) {
            NLNIRailwayLine line = lines.get(content);
            if (line == null) {
                line = new NLNIRailwayLine(content);
                lines.put(content, line);
            }
            line.getStations().add(currentStation);
        } else if ("railwayType".equals(name) && "Station".equals(context.peek())) {
            currentStation.setRailwayType(content);
        } else if ("stationName".equals(name) && "Station".equals(context.peek())) {
            currentStation.setName(content);
        } else if ("Station".equals(name) && "Dataset".equals(context.peek())) {
            stations.put(currentStation.getId(), currentStation);
        }
    }
}
