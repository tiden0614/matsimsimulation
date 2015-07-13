package com.mitsubishi.simulation.controllers.parsers;

import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.xml.sax.Attributes;

import java.util.Stack;

/**
 * Created by tiden on 7/13/2015.
 */
public class OSMBoundingBoxParser extends MatsimXmlParser {

    private QuadTree.Rect boundingBox;

    public OSMBoundingBoxParser() {
        this.setValidating(false);
    }

    public OSMBoundingBoxParser parseFluent(String filename) {
        this.parse(filename);
        return this;
    }

    @Override
    public void startTag(String name, Attributes atts, Stack<String> context) {
        if ("bounds".equals(name)) {
            // I assume that x represents longitude and y represents latitude
            double minlat = Double.valueOf(atts.getValue("minlat"));
            double minlon = Double.valueOf(atts.getValue("minlon"));
            double maxlat = Double.valueOf(atts.getValue("maxlat"));
            double maxlon = Double.valueOf(atts.getValue("maxlon"));
            boundingBox = new QuadTree.Rect(minlon, minlat, maxlon, maxlat);
        }
    }

    @Override
    public void endTag(String name, String content, Stack<String> context) {

    }

    public QuadTree.Rect getBoundingBox() {
        return boundingBox;
    }
}
