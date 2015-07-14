package com.mitsubishi.simulation.input.nlni;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiden on 7/14/2015.
 * This class represents a gml:Curve element that is defined in the NLNI's xml file
 * After loaded, every curve should be simplified into a single point
 */
public class NLNICurve {

    private List<Double> xys;
    private String id;

    public NLNICurve(String id) {
        xys = new ArrayList<Double>();
        this.id = id;
    }

    public void addNumber(Double num) {
        xys.add(num);
    }

    public String getId() {
        return id;
    }

    public double getXCenter() {
        return getXORYCenter(0);
    }

    public double getYCenter() {
        return getXORYCenter(1);
    }

    private double getXORYCenter(int start) {
        double total = 0;
        int count = 0;
        for (int i = start; i < xys.size(); i+=2) {
            total += xys.get(i);
            count++;
        }
        if (count == 0) {
            return 0;
        }
        return total / count;
    }

    @Override
    public String toString() {
        return id;
    }
}
