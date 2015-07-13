package com.mitsubishi.simulation.controllers;

import com.mitsubishi.simulation.input.network.NetworkUtils;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

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

        Network network = NetworkUtils.convertOSMToNetwork(args[0], TransformationFactory.WGS84, targetCoordSys, true);

        NetworkUtils.writeNetworkToFile(network, args[1]);
    }
}
