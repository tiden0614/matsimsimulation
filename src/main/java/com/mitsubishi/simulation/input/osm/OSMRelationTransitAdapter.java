package com.mitsubishi.simulation.input.osm;

import com.mitsubishi.simulation.input.transit.Transit;
import com.mitsubishi.simulation.input.transit.TransitStation;
import com.mitsubishi.simulation.input.transit.TransitAdapter;
import com.mitsubishi.simulation.input.transit.TransitStop;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.basic.v01.IdImpl;

import java.io.FileInputStream;
import java.util.*;

/**
 * Created by tiden on 7/6/2015.
 * This class is used to convert OSM relations into more abstract transits
 */
public class OSMRelationTransitAdapter implements TransitAdapter {
    private static final Logger logger = Logger.getLogger(OSMRelationTransitAdapter.class);

    private Network network;
    private List<OSMRelation> relations;
    private List<Transit> transits;
    private Map<Id, TransitStation> transitStations;
    // unfortunately we cannot get the inner ID generator of Matsim network
    // although it is possible to access the ID generator by reflection
    // it is dangerous to do so since the IDs may crush in someway
    // assume that the higher ids are not used, which is *often* true
    // having said that, better solution is expected
    private long unusedId = Long.MAX_VALUE;

    /**
     * This constructor accepts an OSM file and a matsim network to convert OSM relations
     * into transits
     * Note that the network is subject to mutation because links that do not belong to
     * the existing network might be added
     *
     * @param filename OSM file path
     * @param network  matsim network
     */
    public OSMRelationTransitAdapter(String filename, Network network) {
        this.network = network;
        this.transits = new ArrayList<>();
        this.transitStations = new HashMap<>();
        logger.info("Converting OSMRelations into Transits...");
        try {
            // only extract public transport relations
            Map<String, Object> filter = new HashMap<>();
            filter.put("type", "route");
            filter.put("route", new String[]{Transit.BUS, Transit.TRAIN});
            this.relations = OSMRelation.extractRelationsFromOSMFile(new FileInputStream(filename), filter);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        for (OSMRelation relation : relations) {
            Map<String, String> tags = relation.getTags();
            String name = tags.get("name");
            String type = tags.get("type");
            // we do not need transits that do not have a name or type
            if (name == null || type == null) {
                continue;
            }
            List<Node> forwardNodes = new ArrayList<>();
            List<Node> backwardNodes = new ArrayList<>();
            boolean createTwoWayTransits = false;
            for (OSMRelationMember member : relation.getMembers()) {
                String role = member.getRole();
                if ("node".equals(member.getType()) && role != null && role.endsWith("stop")) {
                    Id nodeId = new IdImpl(member.getRef());
                    Node stopNode = network.getNodes().get(nodeId);
                    if (stopNode == null) {
                        // TODO insert the node into the network if it is not already there
                        continue;
                    }
                    if ("stop".equals(role)) {
                        forwardNodes.add(stopNode);
                        backwardNodes.add(stopNode);
                    } else if ("forward_stop".equals(role)) {
                        createTwoWayTransits = true;
                        forwardNodes.add(stopNode);
                    } else if ("backward_stop".equals(role)) {
                        createTwoWayTransits = true;
                        backwardNodes.add(stopNode);
                    }
                }
            }
            Transit forwardTransit = null;
            Transit backwardTransit = null;
            if (createTwoWayTransits) {
                forwardTransit = makeATransitFromNodes(type, name + "_forward", forwardNodes);
                // reverse backward stops
                Collections.reverse(backwardNodes);
                backwardTransit = makeATransitFromNodes(type, name + "_backward", backwardNodes);
            } else {
                forwardTransit = makeATransitFromNodes(type, name, forwardNodes);
            }
            if (forwardTransit != null) {
                transits.add(forwardTransit);
            }
            if (backwardTransit != null) {
                transits.add(backwardTransit);
            }
        }
        logger.info("Conversion from OSMRelations to Transits done.");
    }

    private Transit makeATransitFromNodes(String type, String name, List<Node> nodes) {
        Transit t = new Transit(type, name);
        List<TransitStop> stops = t.getStops();
        // cache the stops in case we decide not to add new stops into the map
        Map<Id, TransitStation> stationsToAdd = new HashMap<>();
        // add stops to the transit
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            Id nodeId = node.getId();
            TransitStation station = transitStations.get(nodeId);
            if (station == null) {
                station = stationsToAdd.get(nodeId);
            }
            if (station == null) {
                station = new TransitStation(node);
                stationsToAdd.put(nodeId, station);
            }
            stops.add(new TransitStop(station, i));
        }
        int size = stops.size();
        // we do not need transits that have only one stop or not at all
        if (size <= 1) {
            return null;
        }
        // let all stops of this transit know that they are passed through by this transit
        for (TransitStop stop : t.getStops()) {
            stop.getStation().getPassThroughTransitMap().put(t, stop);
        }
        // add cached stops to the map
        transitStations.putAll(stationsToAdd);
        // add links to the network
        for (int j = 0; j < size - 1; j++) {
            Node from = stops.get(j).getStation().getNode();
            Node to = stops.get(j + 1).getStation().getNode();
            // find if there is a link connecting stops
            Link connection = null;
            for (Link l : from.getOutLinks().values()) {
                if (l.getToNode().getId().equals(to.getId())) {
                    connection = l;
                    break;
                }
            }
            if (connection == null) {
                // there is no existing link that connects the two stops
                // make one and add it to the network
                logger.info("Modifying the matsim network: adding link #" + unusedId);
                connection = network.getFactory().createLink(new IdImpl(unusedId--), from, to);
                network.addLink(connection);
            }
        }
        return t;
    }

    public List<Transit> getTransits() {
        return transits;
    }

    public List<TransitStation> getTransitStations() {
        List<TransitStation> transitStationList = new ArrayList<>();
        transitStationList.addAll(transitStations.values());
        return transitStationList;
    }
}
