package com.mitsubishi.simulation.controllers;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;

/**
 * Created by tiden on 6/19/2015.
 */
public class MyFirstController {
    public static void main(String[] args) {
        Config config = ConfigUtils.loadConfig("input/config.xml");
        Controler controler = new Controler(config);
//        controler.setOverwriteFiles(true);
        controler.run();
    }
}
