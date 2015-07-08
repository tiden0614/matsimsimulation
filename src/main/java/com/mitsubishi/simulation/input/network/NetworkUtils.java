package com.mitsubishi.simulation.input.network;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.OsmNetworkReader;

/**
 * Created by tiden on 7/2/2015.
 */
public class NetworkUtils {

    private static final Logger logger = Logger.getLogger(NetworkUtils.class);

    /**
     * Convert an OSM file into MATSim's network
     * @param osm the path of the OSM file
     * @param osmCoord the type of the coordinate used by the osm file
     *                 should be a value in TransformationFactory
     *                 e.g. <code>TransformationFactory.WGS84</code>
     * @param networkCoord the type of the coordinate used by the network file
     *                     should be a value in TransformationFactory
     *                     e.g. <code>TransformationFactory.CH1903_LV3</code>
     * @return the generated network object
     */
    public static Network convertOSMToNetwork(String osm, String osmCoord, String networkCoord) {
        logger.info(String.format("Converting OSM file [%s] into MATSim's network. " +
                "Source coordinate system [%s]; target coordinate system [%s]",
                osm, osmCoord, networkCoord));
        Config config = ConfigUtils.createConfig();
        Scenario sc = ScenarioUtils.createScenario(config);
        Network net = sc.getNetwork();
        CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(osmCoord, networkCoord);
        OsmNetworkReader onr = new OsmNetworkReader(net, ct);
        onr.parse(osm);
//        new NetworkCleaner().run(net);
        logger.info("Successfully converted OSM file into MATSim's network.");
        return net;
    }

    /**
     * Write matsim's network into a file
     * @param network matsim's network
     * @param path the file path expected to be written
     */
    public static void writeNetworkToFile(Network network, String path) {
        logger.info("Writing network into file " + path + "...");
        new NetworkWriter(network).write(path);
        logger.info(path + " written");
    }
}
