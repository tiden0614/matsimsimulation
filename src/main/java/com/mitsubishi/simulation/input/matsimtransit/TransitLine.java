package com.mitsubishi.simulation.input.matsimtransit;

import com.mitsubishi.simulation.input.transit.Transit;

/**
 * Created by tiden on 7/22/2015.
 */
public class TransitLine {
    private String id;
    private TransitLineRoute forwardRoute;
    private TransitLineRoute backwardRoute;
    private int seats;
    private int standingRoom;
    private int length;

    public TransitLine(Transit transit) {
        seats = 50;
        standingRoom = 50;
        length = 50;
        init(transit);
    }

    private void init(Transit transit) {
        setId(transit.getName());

        forwardRoute = new TransitLineRoute(transit, false);

        if (transit.isDuplexTransit()) {
            // generate backward transit
            backwardRoute = new TransitLineRoute(transit, true);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TransitLineRoute getForwardRoute() {
        return forwardRoute;
    }

    public void setForwardRoute(TransitLineRoute forwardRoute) {
        this.forwardRoute = forwardRoute;
    }

    public TransitLineRoute getBackwardRoute() {
        return backwardRoute;
    }

    public void setBackwardRoute(TransitLineRoute backwardRoute) {
        this.backwardRoute = backwardRoute;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public int getStandingRoom() {
        return standingRoom;
    }

    public void setStandingRoom(int standingRoom) {
        this.standingRoom = standingRoom;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
