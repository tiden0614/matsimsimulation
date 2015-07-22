package com.mitsubishi.simulation.input.matsimtransit;

/**
 * Created by tiden on 7/22/2015.
 */
public class TransitLineRouteDeparture {
    private String id;
    private String departureTime;
    private String vehicleRefId;

    public TransitLineRouteDeparture(String id, String departureTime, String vehicleRefId) {
        this.id = id;
        this.departureTime = departureTime;
        this.vehicleRefId = vehicleRefId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getVehicleRefId() {
        return vehicleRefId;
    }

    public void setVehicleRefId(String vehicleRefId) {
        this.vehicleRefId = vehicleRefId;
    }
}
