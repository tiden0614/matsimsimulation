package com.mitsubishi.simulation.input.tutrorials;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.api.internal.MatsimWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

/**
 * Created by tiden on 6/26/2015.
 */
public class POnePersonPopulationGenerator {
    public static void main(String[] args) {
        Config config = ConfigUtils.createConfig();
        Scenario sc = ScenarioUtils.createScenario(config);
        Network network = sc.getNetwork();
        Population population = sc.getPopulation();
        PopulationFactory populationFactory = population.getFactory();

        // Create a person by the population factory
        Person person = populationFactory.createPerson(sc.createId("1"));
        population.addPerson(person);

        // Add a plan for our person
        Plan plan = populationFactory.createPlan();
        person.addPlan(plan);

        // Transform the coordination from WGS84 to CH1903_LV03
        CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(
                TransformationFactory.WGS84, TransformationFactory.CH1903_LV03
        );

        // Create a home activity for the person
        Coord homeCoordinates = sc.createCoord(8.55744100, 47.3548407);
        Activity homeActivity = populationFactory.createActivityFromCoord("h6", ct.transform(homeCoordinates));

        // Leave at 6 am
        homeActivity.setEndTime(21600);

        // Add the activity to plan
        plan.addActivity(homeActivity);

        // Create and add a leg. A leg initially does not have many attributes. It just says that a car will
        // be used
        plan.addLeg(populationFactory.createLeg("car"));

        // Create a "work" activity
        Activity workActivity = populationFactory.createActivityFromCoord(
                "w10", ct.transform(sc.createCoord(8.51774679, 47.3893719))
        );

        // Leave at 4 pm
        workActivity.setEndTime(57600);

        // And add to the plan
        plan.addActivity(workActivity);

        // Create and add another car leg
        plan.addLeg(populationFactory.createLeg("car"));

        // End the day with another Activity at home
        // Note that it gets the same coordinates as the first activity
        Activity backHome = populationFactory.createActivityFromCoord("h6", homeCoordinates);
        plan.addActivity(backHome);

        // Write the population to a file
        MatsimWriter popWriter = new PopulationWriter(population, network);
        popWriter.write("./input/plans.HandMade.xml");
    }
}
