package com.mitsubishi.simulation.test;

import com.mitsubishi.simulation.controllers.parsers.OSMBoundingBoxParser;
import com.mitsubishi.simulation.input.matsimtransit.TransitWriter;
import com.mitsubishi.simulation.input.network.NetworkUtils;
import com.mitsubishi.simulation.input.nlni.NLNIRailwayTransitAdapter;
import com.mitsubishi.simulation.input.population.PersonGenerator;
import com.mitsubishi.simulation.input.population.RandomWithinBoundaryPersonGenerator;
import org.junit.Before;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.api.experimental.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.openstreetmap.osmosis.core.Osmosis;

import java.io.File;

/**
 * Created by tiden on 7/22/2015.
 */
public class TestCombinedFunctions {
    private static final String COMBINED_TEST_OUTPUT = "combinedTestOutput";
    private static final String COMBINED_TEST_INPUT = "combinedTestInput";

    private Scenario scenario;

//    @Before
    public void setup() {
        Config config = ConfigUtils.createConfig();
        this.scenario = ScenarioUtils.createScenario(config);
    }


    public Network getTransformedNetwork() {
        String[] wayExtractorArgs = new String[] {
                "--rx", "file=" + COMBINED_TEST_INPUT + File.separator + "tokyo.osm",
                "--tf", "accept-ways", "highway=motorway,motorway_link,trunk,trunk_link,primary,primary_link",
                "--tf", "reject-relations",
                "--used-node",
                "--wx", "file=" + COMBINED_TEST_OUTPUT + File.separator + "wayTempOutput.osm"
        };
        Osmosis.run(wayExtractorArgs);

        return NetworkUtils.convertOSMToNetwork(
                scenario.getNetwork(),
                COMBINED_TEST_OUTPUT + File.separator + "wayTempOutput.osm",
                TransformationFactory.WGS84,
                TransformationFactory.CH1903_LV03,
                true
        );
    }

    public Network getNewNetwork() {
        return org.matsim.core.network.NetworkUtils.createNetwork();
    }

//    @Test
    public void testCombinedFunction() {
        Network someNetwork = getTransformedNetwork();
//        Network someNetwork = scenario.getNetwork();

        QuadTree.Rect boundary = new OSMBoundingBoxParser(COMBINED_TEST_INPUT + File.separator + "tokyo.osm")
                .getBoundingBox();

        // insert public transportation network
        NLNIRailwayTransitAdapter adapter = new NLNIRailwayTransitAdapter(
                COMBINED_TEST_INPUT + File.separator + "N02-13.xml", someNetwork, boundary);

        new NetworkWriter(someNetwork).write(COMBINED_TEST_OUTPUT + File.separator + "network.xml");


        TransitWriter writer = new TransitWriter(COMBINED_TEST_OUTPUT, adapter.getTransits());
        writer.writeTransitVehicles();
        writer.writeTransitSchedule();

        PersonGenerator generator = new RandomWithinBoundaryPersonGenerator(
                scenario,
                boundary,
                10000);

        for (Person person : generator.getPersons()) {
            scenario.getPopulation().addPerson(person);
        }

        PopulationWriter populationWriter = new PopulationWriter(scenario.getPopulation(), someNetwork);
        populationWriter.write(COMBINED_TEST_OUTPUT + File.separator + "population.xml");
    }

//    @Test
    public void testRunScenario() {
        Config config = ConfigUtils.loadConfig(COMBINED_TEST_OUTPUT + File.separator + "config.xml");
        Controler controler = new Controler(config);
        controler.run();
    }
}
