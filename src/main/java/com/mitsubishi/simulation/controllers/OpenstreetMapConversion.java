package com.mitsubishi.simulation.controllers;

import com.mitsubishi.simulation.input.network.NetworkUtils;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.openstreetmap.osmosis.core.Osmosis;

import java.io.File;

/**
 * Created by tiden on 7/13/2015.
 * Entry for the conversion of OSM network
 */
public class OpenstreetMapConversion {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Both input path and output path needed!");
            System.exit(1);
        }

        String targetCoordSys = TransformationFactory.WGS84;
        if (args.length > 2) {
            targetCoordSys = args[2];
        }

        String inputPath = args[0];
        String outputPath = args[1];

        File outputFile = new File(outputPath);
        String wayTempOutputPath = outputFile.getParent() + "/ways.tmp.osm";

        // Extract main ways
        // Filter all relations in the osm file
        String[] wayExtractorArgs = new String[] {
                "--rx", "file=" + inputPath,
                "--tf", "accept-ways", "highway=motorway,motorway_link,trunk,trunk_link,primary,primary_link",
                "--tf", "reject-relations",
                "--used-node",
                "--wx", "file=" + wayTempOutputPath
        };
        Osmosis.run(wayExtractorArgs);

        Network network = NetworkUtils.convertOSMToNetwork(
                wayTempOutputPath, TransformationFactory.WGS84, targetCoordSys);

        NetworkUtils.writeNetworkToFile(network, outputPath);

        File wayTempFile = new File(wayTempOutputPath);
        if (wayTempFile.exists()) {
            wayTempFile.delete();
        }
    }
}
