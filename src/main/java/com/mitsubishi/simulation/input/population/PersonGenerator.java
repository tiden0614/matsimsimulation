package com.mitsubishi.simulation.input.population;

import org.matsim.api.core.v01.population.Person;

import java.util.List;

/**
 * Created by tiden on 7/22/2015.
 */
public interface PersonGenerator {
    List<Person> getPersons();
}
