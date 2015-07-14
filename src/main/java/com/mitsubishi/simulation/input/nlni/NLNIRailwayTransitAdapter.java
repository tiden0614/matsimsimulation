package com.mitsubishi.simulation.input.nlni;

import com.mitsubishi.simulation.input.transit.Transit;
import com.mitsubishi.simulation.input.transit.TransitAdapter;
import com.mitsubishi.simulation.input.transit.TransitStation;
import com.mitsubishi.simulation.input.transit.TransitStop;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.NodeImpl;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.xml.sax.Attributes;

import java.util.*;

/**
 * Created by tiden on 7/14/2015.
 * This adapter transforms National Land Numerical Information's railway data
 * into Transit and TransitStation.
 */
public class NLNIRailwayTransitAdapter extends MatsimXmlParser implements TransitAdapter {

    private List<Transit> transits;
    private Map<String, TransitStation> transitStations;

    private NLNICurve currentCurve;
    private Map<String, NLNICurve> curves;

    private NLNIRailwayStation currentStation;
    private Map<String, NLNIRailwayStation> stations;

    private Map<String, NLNIRailwayLine> lines;

    private Network network;
    private long idGen;

    public NLNIRailwayTransitAdapter(String inputFile, Network network) {
        this.network = network;
        this.idGen = Long.MAX_VALUE;

        transits = new ArrayList<Transit>();
        transitStations = new HashMap<String, TransitStation>();
        curves = new HashMap<String, NLNICurve>();
        stations = new HashMap<String, NLNIRailwayStation>();
        lines = new HashMap<String, NLNIRailwayLine>();
        setValidating(false);

        // get raw types from the xml file
        parse(inputFile);

        // convert the raw types into Transits and TransitStations
        convert();
    }

    public List<Transit> getTransits() {
        return transits;
    }

    public List<TransitStation> getTransitStations() {
        List<TransitStation> retList = new ArrayList<TransitStation>();
        retList.addAll(transitStations.values());
        return retList;
    }

    private void convert() {
        for (NLNIRailwayLine line : lines.values()) {
            Transit transit = new Transit(Transit.TRAIN, line.getName());
            TransitStation lastStation = null;
            int index = 0;
            for (NLNIRailwayStation s : line.getStations()) {
                TransitStation transitStation = transitStations.get(s.getName());
                if (transitStation == null) {
                    double x = s.getX();
                    double y = s.getY();
                    NodeImpl node = new NodeImpl(new IdImpl(s.getId()));
                    node.setCoord(new CoordImpl(x, y));
                    network.addNode(node);
                    transitStation = new TransitStation(s.getName(), node);
                    transitStations.put(transitStation.getName(), transitStation);
                }
                if (lastStation != null) {
                    Link link1 = network.getFactory().createLink(
                            new IdImpl(idGen--), lastStation.getNode(), transitStation.getNode()
                    );
                    Link link2 = network.getFactory().createLink(
                            new IdImpl(idGen--), transitStation.getNode(), lastStation.getNode()
                    );
                    network.addLink(link1);
                    network.addLink(link2);
                }
                TransitStop stop = new TransitStop(transitStation, index++);
                transit.getStops().add(stop);
                transitStation.getPassThroughTransitMap().put(transit, stop);
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
    }
}
