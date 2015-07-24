package com.mitsubishi.simulation.utils;

import com.mitsubishi.simulation.input.transit.Transit;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

/**
 * Created by tiden on 7/9/2015.
 */
public class Constants {
    public static final String OSM_TEST_INPUT = "testinput/test-osm-basic.xml";
    public static final double WGS_DISTANCE_VERY_SMALL = 0.00005;
    public static final double WGS_DISTANCE_1M = 0.000009043716;
    public static final double WGS_DISTANCE_5M = WGS_DISTANCE_1M * 5;
    public static final double WGS_DISTANCE_500M = WGS_DISTANCE_5M * 100;
    public static final double WGS_DISTANCE_5KM = WGS_DISTANCE_500M * 10;
    public static final double WGS_DISTANCE_1KM = WGS_DISTANCE_500M * 2;

    public static double get1MForCoordSystem(String targetCoordSystem) {
        Coord wgs84_1m = new CoordImpl(WGS_DISTANCE_1M, 0);
        Coord wgs84_ori = new CoordImpl(0, 0);
        CoordinateTransformation transformation = TransformationFactory
                .getCoordinateTransformation(TransformationFactory.WGS84, targetCoordSystem);
        Coord target_ori = transformation.transform(wgs84_ori);
        Coord target1m = transformation.transform(wgs84_1m);
        return Math.sqrt(
                (target1m.getX() - target_ori.getX()) * (target1m.getX() - target_ori.getX()) +
                (target1m.getY() - target_ori.getY()) * (target1m.getY() - target_ori.getY())
        );
    }

    public static QuadTree.Rect convertBoundryToCoordSystem(QuadTree.Rect boundary, String targetCoordSystem) {
        CoordinateTransformation transformation = TransformationFactory.getCoordinateTransformation(
                TransformationFactory.WGS84, targetCoordSystem
        );
        Coord leftTop = transformation.transform(new CoordImpl(boundary.minX, boundary.minY));
        Coord rightBottom = transformation.transform(new CoordImpl(boundary.maxX, boundary.maxY));

        return new QuadTree.Rect(leftTop.getX(), leftTop.getY(), rightBottom.getX(), rightBottom.getY());
    }
}
