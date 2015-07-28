package com.mitsubishi.simulation.input.population;

import com.mitsubishi.simulation.input.transit.Transit;
import com.mitsubishi.simulation.utils.Constants;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.LegImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.geometry.CoordImpl;

import java.util.*;

/**
 * Created by tiden on 7/22/2015.
 */
public class RandomWithinBoundaryPersonGenerator implements PersonGenerator {
    private static final double DISTANCE_1M = Constants.get1MForCoordSystem(Transit.ACCEPT_COORD_SYSTEM);
    private static final double[] ranges = {
            DISTANCE_1M * 10000,
            DISTANCE_1M * 5000,
            DISTANCE_1M * 1000,
            DISTANCE_1M * 500,
            DISTANCE_1M * 100,
            DISTANCE_1M * 50,
    };

    private static final int angleSearchInterval = 15;

    private static int idGen = 0;

    private QuadTree.Rect boundary;
    private Scenario scenario;
    private List<Person> persons;
    private int numPersons;
    private Random rand;

    public RandomWithinBoundaryPersonGenerator(Scenario scenario, QuadTree.Rect boundary, int numPersons) {
        this.scenario = scenario;
//        this.boundary = boundary;
        this.boundary = Constants.convertBoundryToCoordSystem(boundary, Transit.ACCEPT_COORD_SYSTEM);
        this.numPersons = numPersons;
        this.persons = new ArrayList<>();
        this.rand = new Random();
        generate();
    }

    private void generate() {
        NetworkImpl network = (NetworkImpl) scenario.getNetwork();
        // A person possibly leaves his home from 5 AM to 11 AM
        int depHomeEarliest = 3600 * 5;
        int depHomeLatest = 3600 * 11;
        // A person possibly leaves his work place from 6 PM to 11 PM
        int depWorkEarliest = 3600 * 18;
        int depWorkLatest = 3600 * 23;

        for (int i = 0; i < numPersons; i++) {
            // generate the home location
            Coord homeLocation = new CoordImpl(generateX(), generateY());
            Coord workLocation = generateDestCoord(homeLocation);

            if (workLocation == null) {
                i--;
                continue;
            }

            Id homeLinkId = network.getNearestLink(homeLocation).getId();
            Id workLinkId = network.getNearestLink(workLocation).getId();

            Activity home1Act = new ActivityImpl("h", homeLocation, homeLinkId);
            home1Act.setEndTime(depHomeEarliest + rand.nextInt(depHomeLatest - depHomeEarliest));

            Activity workAct = new ActivityImpl("w", workLocation, workLinkId);
            workAct.setEndTime(depWorkEarliest + rand.nextInt(depWorkLatest - depWorkEarliest));

            Activity home2Act = new ActivityImpl("h", homeLocation, homeLinkId);

            Person p = new PersonImpl(new IdImpl(idGen++));
            Plan plan = new PlanImpl();
            plan.addActivity(home1Act);
            plan.addLeg(new LegImpl(TransportMode.pt));
            plan.addActivity(workAct);
            plan.addLeg(new LegImpl(TransportMode.pt));
            plan.addActivity(home2Act);
            p.addPlan(plan);

            persons.add(p);
        }
    }

    private Coord generateDestCoord(Coord homeLocation) {
        int size = (int) Math.ceil(360.0 / angleSearchInterval);
        List<Double> shuffledAngleArray = new ArrayList<>();
        double realInterval = angleSearchInterval * Math.PI / 180;
        double curAngle = realInterval;
        for (int i = 0; i < size; i++) {
            shuffledAngleArray.add(curAngle);
            curAngle += realInterval;
        }
        Collections.shuffle(shuffledAngleArray);

        for (double range : ranges) {
            for (double angle : shuffledAngleArray) {
                double rangex = homeLocation.getX() + range * Math.cos(angle);
                double rangey = homeLocation.getY() + range * Math.sin(angle);
                if (isWithinBoundary(rangex, rangey)) {
                    return new CoordImpl(rangex, rangey);
                }
            }
        }
        return null;
    }

    private boolean isWithinBoundary(double x, double y) {
        return boundary.minX <= x && x <= boundary.maxX && boundary.minY <= y && y <= boundary.maxY;
    }

    private double generateY() {
        double dif = boundary.maxY - boundary.minY;
        double r = dif * rand.nextDouble();
        return r + boundary.minY;
    }

    private double generateX() {
        double dif = boundary.maxX - boundary.minX;
        double r = dif * rand.nextDouble();
        return r + boundary.minX;
    }

    public List<Person> getPersons() {
        return persons;
    }

    public Scenario getScenario() {
        return scenario;
    }
}
