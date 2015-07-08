package com.mitsubishi.simulation.input.osm;

import org.apache.log4j.Logger;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.xml.sax.Attributes;

import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by tiden on 7/3/2015.
 * This class is used to parse relations in an OSM file into OSMRelation objects.
 */
public class OSMRelationXMLParser extends MatsimXmlParser {

    private static final Logger logger = Logger.getLogger(OSMRelationXMLParser.class);

    private OSMRelation curRelation;
    private List<OSMRelation> relations;
    private long relationNum;
    // use this map to filter relations according to their tags
    private Map<String, Object> filter;

    public OSMRelationXMLParser(final List<OSMRelation> relations) throws InvalidFilterValueException {
        this(relations, null);
    }

    public OSMRelationXMLParser(final List<OSMRelation> relations, Map<String, Object> filter)
            throws InvalidFilterValueException {
        this.relations = relations;
        this.filter = filter;
        this.relationNum = 0L;
        setValidating(false);
        if (filter != null) {
            for (Object o : filter.values()) {
                if (!(o instanceof String || o instanceof String[])) {
                    throw new InvalidFilterValueException(
                            "Only String or String[] can be passed to the filter");
                }
            }
        }
    }

    @Override
    public void startTag(String name, Attributes atts, Stack<String> context) {
        if ("relation".equals(name)) {
            curRelation = new OSMRelation(Long.valueOf(atts.getValue("id")));
        } else if ("member".equals(name) && "relation".equals(context.peek())) {
            curRelation.getMembers().add(new OSMRelationMember(
                    atts.getValue("type"),
                    Long.valueOf(atts.getValue("ref")),
                    atts.getValue("role")
            ));
        } else if ("tag".equals(name) && "relation".equals(context.peek())) {
            curRelation.getTags().put(atts.getValue("k"), atts.getValue("v"));
        }
    }

    @Override
    public void endTag(String name, String content, Stack<String> context) {
        if (curRelation != null && "relation".equals(name)) {
            // filter out relations that either don't contain a tag name
            // in the filter map or the value of the tag does not equal to
            // that defined in the filter
            if (filter != null) {
                for (Map.Entry<String, Object> e : filter.entrySet()) {
                    String k = e.getKey();
                    String testV = curRelation.getTags().get(k);
                    if (testV == null) {
                        curRelation = null;
                        return;
                    }
                    Object o = e.getValue();
                    if (o instanceof String) {
                        if (!testV.equals(o)) {
                            curRelation = null;
                            return;
                        }
                    } else if (o instanceof String[]) {
                        // if the value of the filter is an array of string
                        // the parser will accept this relation when any of
                        // the strings in the array equals to the corresponding tag
                        String[] arr = (String[]) o;
                        boolean skip = true;
                        for (String s : arr) {
                            if (testV.equals(s)) {
                                skip = false;
                            }
                        }
                        if (skip) {
                            curRelation = null;
                            return;
                        }
                    }
                }
            }
            relations.add(curRelation);
            curRelation = null;
            if (++relationNum % 1000 == 0) {
                logger.info(relationNum + " relations extracted.");
            }
        }
    }
}
