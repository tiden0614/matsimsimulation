package com.mitsubishi.simulation.input.route;

import com.mitsubishi.simulation.input.transit.TransitStop;

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
    private TransitStop fromStop;
    private TransitStop toStop;
    private List<TransitTrip> trips;

    public TransitRoute(double walkingDistance1, double walkingDistance2, List<TransitTrip> trips) {
        this.walkingDistance1 = walkingDistance1;
        this.walkingDistance2 = walkingDistance2;
        this.totalWalkingDistance = walkingDistance1 + walkingDistance2;
        this.trips = trips;
        if (trips.size() > 0) {
            this.fromStop = trips.get(0).getFromStop();
            this.toStop = trips.get(trips.size() - 1).getToStop();
            // calculate total walking distance
            int size = trips.size();
            for (int i = 0; i < size - 1; i++) {
                TransitTrip trip1 = trips.get(i);
                TransitTrip trip2 = trips.get(i + 1);
                totalWalkingDistance += trip1.getToStop().getDistanceFrom(trip2.getFromStop());
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

    public TransitStop getFromStop() {
        return fromStop;
    }

    public TransitStop getToStop() {
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
