package com.mitsubishi.simulation.controllers;

/**
 * Created by tiden on 6/19/2015.
 * This is the entry for the public transport example simulation
 */
public class PublicTransportSimulation {
    public static void main(String[] args) {
        org.matsim.run.Controler.main(new String[]{"examples/pt-tutorial/config.xml"});
    }
}
