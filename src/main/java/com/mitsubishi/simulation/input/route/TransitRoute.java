package com.mitsubishi.simulation.input.route;

import com.mitsubishi.simulation.input.transit.TransitStation;

import java.util.List;

/**
 * Created by tiden on 7/8/2015.
 * This class represents a route on public transportation
 */
public class TransitRoute {
    private double score;
    private double walkingDistance1;
    private double walkingDistance2;
    private double totalWalkingDistance;
    private TransitStation fromStop;
    private TransitStation toStop;
    private List<TransitTrip> trips;

    public TransitRoute(double walkingDistance1, double walkingDistance2, List<TransitTrip> trips) {
        this.walkingDistance1 = walkingDistance1;
        this.walkingDistance2 = walkingDistance2;
        this.totalWalkingDistance = walkingDistance1 + walkingDistance2;
        this.trips = trips;
        if (trips.size() > 0) {
            this.fromStop = trips.get(0).getFromStation();
            this.toStop = trips.get(trips.size() - 1).getToStation();
            // calculate total walking distance
            int size = trips.size();
            for (int i = 0; i < size - 1; i++) {
                TransitTrip trip1 = trips.get(i);
                TransitTrip trip2 = trips.get(i + 1);
                totalWalkingDistance += trip1.getToStation().getDistanceFrom(trip2.getFromStation());
            }
        }
        // score this route
        score();
    }

    public double getScore() {
        return score;
    }

    public double getWalkingDistance1() {
        return walkingDistance1;
    }

    public double getWalkingDistance2() {
        return walkingDistance2;
    }

    public TransitStation getFromStop() {
        return fromStop;
    }

    public TransitStation getToStop() {
        return toStop;
    }

    public List<TransitTrip> getTrips() {
        return trips;
    }

    public double getTotalWalkingDistance() {
        return totalWalkingDistance;
    }

    private void score() {
        // TODO implement this method
    }
}
