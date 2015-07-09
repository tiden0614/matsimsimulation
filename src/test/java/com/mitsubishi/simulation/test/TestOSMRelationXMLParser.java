package com.mitsubishi.simulation.test;

import com.mitsubishi.simulation.input.osm.OSMRelation;
import com.mitsubishi.simulation.input.osm.OSMRelationXMLParser;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Created by tiden on 7/6/2015.
 * Test the function of the XML parser
 */
public class TestOSMRelationXMLParser {
    @Test
    public void testOSMRelationParser() throws Exception {
        List<OSMRelation> relationList = new ArrayList<OSMRelation>();
        Map<String, Object> filter = new HashMap<String, Object>();
        filter.put("type", "route");
        filter.put("route", new String[]{"bus", "train"});
        OSMRelationXMLParser parser = new OSMRelationXMLParser(relationList, filter);
        parser.parse(TestConstants.OSM_TEST_INPUT);
        assertTrue(relationList.size() > 0);
    }
}
