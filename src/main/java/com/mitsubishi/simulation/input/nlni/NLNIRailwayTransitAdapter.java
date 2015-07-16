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
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.NodeImpl;
import org.matsim.core.utils.geometry.CoordImpl;
import org.xml.sax.Attributes;

import java.util.*;

/**
 * Created by tiden on 7/14/2015.
 * This adapter transforms National Land Numerical Information's railway data
 * into Transit and TransitStation.
 */
public class NLNIRailwayTransitAdapter extends AbstractNLNITransitAdapter {

    private static final Logger logger = Logger.getLogger(NLNIRailwayTransitAdapter.class);

    private List<Transit> transits;
    private Map<String, TransitStation> transitStations;

    private NLNICurve currentCurve;
    private Map<String, NLNICurve> curves;

    private NLNIRailwayStation currentStation;
    private Map<String, NLNIRailwayStation> stations;

    private Map<String, NLNIRailwayLine> lines;

    private Network network;

    public NLNIRailwayTransitAdapter(String inputFile, Network network) {
        this.network = network;

        transits = new ArrayList<Transit>();
        transitStations = new HashMap<String, TransitStation>();
        curves = new HashMap<String, NLNICurve>();
        stations = new HashMap<String, NLNIRailwayStation>();
        lines = new HashMap<String, NLNIRailwayLine>();
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

        StringBuilder b = new StringBuilder("The following stops are discarded due to malformed data:");
        b.append(" (");
        b.append(Transit.getNumDiscardedStops());
        b.append(")\n");
        for (Transit t : Transit.getDiscardedStops().keySet()) {
            b.append(t.getName());
            b.append("\n");
            for (TransitStop stop : Transit.getDiscardedStops().get(t)) {
                b.append("\t");
                b.append(stop.getStation().getName());
                b.append("\n");
            }
        }

        logger.warn(b.toString());
    }

    public List<Transit> getTransits() {
        return transits;
    }

    public List<TransitStation> getTransitStations() {
        List<TransitStation> retList = new ArrayList<TransitStation>();
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
        Iterator<Map.Entry<String, NLNIRailwayLine>> iterator = lines.entrySet().iterator();
        Map<String, NLNIRailwayLine> toBeAdded = new HashMap<String, NLNIRailwayLine>();
        while (iterator.hasNext()) {
            Map.Entry<String, NLNIRailwayLine> entry = iterator.next();
            Map<String, List<NLNIRailwayStation>> stationCategories = new HashMap<String, List<NLNIRailwayStation>>();
            NLNIRailwayLine l = entry.getValue();
            for (NLNIRailwayStation station : l.getStations()) {
                String stationType = station.getRailwayType();
                List<NLNIRailwayStation> slist = stationCategories.get(stationType);
                if (slist == null) {
                    slist = new LinkedList<NLNIRailwayStation>();
                    stationCategories.put(stationType, slist);
                }
                slist.add(station);
            }
            if (stationCategories.size() > 1) {
                iterator.remove();
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

        // for debug use
        int loop = 0;
        Set<Link> unusualLinks = new HashSet<Link>();
        Set<String> carMode = new HashSet<String>();
        carMode.add(TransportMode.car);

        // Set the links of transits to allow public transport
        Set<String> trainMode = new HashSet<String>();
        trainMode.add(TransportMode.pt);

        for (NLNIRailwayLine line : lines.values()) {

            // for debug use
//            if (loop++ > 2) return;

            Transit transit = new Transit(Transit.TRAIN, line.getName());
            transit.setDuplexTransit(true);

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
            transit.rearrangeStops();

            // after rearrangement, there might be some transits that we don't need anymore
            if (transit.getStops().size() < 2) {
                continue;
            }

            // create links and add them to the network
            List<Link> forwardLinks = transit.getForwardLinks();
            List<Link> backwardLinks = transit.getBackwardLinks();
            TransitStation lastStation = null;
            for (TransitStop stop : transit.getStops()) {
                TransitStation transitStation = stop.getStation();
                transitStation.getPassThroughTransitMap().put(transit, stop);
                Set<String> allowedMode = trainMode;
                if (lastStation != null) {

                    // unfortunately we have to do this cleaning for links
                    // I have no idea where those extremely long links come from
                    double distance = lastStation.getDistanceFrom(transitStation);
                    if (distance > Constants.WGS_DISTANCE_5KM * 1600) {
                        logger.warn("Found a super long distance " + distance + " in line " +
                            transit.getName() + " between stations " +
                            lastStation.getName() + " -> " + transitStation.getName());
                        allowedMode = carMode;
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
                    forwardLinks.add(link1);
                    backwardLinks.add(link2);
                    synchronized (AbstractNLNITransitAdapter.class) {
                        network.addLink(link1);
                        network.addLink(link2);
                    }
                }
                lastStation = transitStation;
            }
            transits.add(transit);
        }
    }

    @Override
    public void startTag(String name, Attributes atts, Stack<String> context) {
        if ("Curve".equals(name)) {
            currentCurve = new NLNICurve(atts.getValue("gml:id"));
        } else if ("Station".equals(name)) {
            currentStation = new NLNIRailwayStation(atts.getValue("gml:id"));
        } else if ("location".equals(name) && "Station".equals(context.peek())) {
            String refId = atts.getValue("xlink:href").substring(1);
            NLNICurve ref = curves.get(refId);
            currentStation.setX(ref.getXCenter());
            currentStation.setY(ref.getYCenter());
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
        } else if ("railwayLineName".equals(name) && "Station".equals(context.peek())) {
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

    public static void main(String[] args) {
        Network network = NetworkUtils.createNetwork();
        NLNIRailwayTransitAdapter adapter = new NLNIRailwayTransitAdapter("NLNIInput/N02-13.xml", network);
        System.out.println(adapter.curves.size());
        com.mitsubishi.simulation.input.network.NetworkUtils.writeNetworkToFile(network, "NLNIInput/n.xml");
    }
}
