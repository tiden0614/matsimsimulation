package com.mitsubishi.simulation.input.osm;

import org.apache.log4j.Logger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tiden on 7/3/2015.
 * Represents a relation in OSM files
 * Provides a static helper method to get all relations defined in an OSM file
 */
public class OSMRelation {
    private static final Logger logger = Logger.getLogger(OSMRelation.class);

    public static List<OSMRelation> extractRelationsFromOSMFile(
            InputStream inputStream, Map<String, Object> filter) throws Exception {
        logger.info("Extracting relations from OSM network file.");
        List<OSMRelation> relations = new ArrayList<OSMRelation>();
        OSMRelationXMLParser parser = new OSMRelationXMLParser(relations, filter);
        parser.parse(inputStream);
        logger.info("Relation extraction from OSM network file is done. " + relations.size()
            + " relations have been extracted.");
        return relations;
    }

    private long id;
    private Map<String, String> tags;
    private List<OSMRelationMember> members;

    public OSMRelation(long id) {
        this.id = id;
        this.tags = new HashMap<String, String>();
        this.members = new ArrayList<OSMRelationMember>();
    }

    public long getId() {
        return id;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public List<OSMRelationMember> getMembers() {
        return members;
    }
}
