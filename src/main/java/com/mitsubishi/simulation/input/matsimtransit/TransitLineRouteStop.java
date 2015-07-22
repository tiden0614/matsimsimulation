package com.mitsubishi.simulation.input.matsimtransit;

/**
 * Created by tiden on 7/22/2015.
 */
public class TransitLineRouteStop {
    private String refId;
    private int arrivalOffset = -1;
    private int departureOffset = -1;

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public void setArrivalOffset(int arrivalOffset) {
        this.arrivalOffset = arrivalOffset;
    }

    public void setDepartureOffset(int departureOffset) {
        this.departureOffset = departureOffset;
    }

    public String getRefId() {
        return refId;
    }

    public String getArrivalOffset() {
        if (arrivalOffset > 0) {
            return String.format("%02d:%02d:%02d", arrivalOffset / 3600, arrivalOffset / 60, arrivalOffset % 60);
        }
        return null;
    }

    public String getDepartureOffset() {
        if (departureOffset > 0) {
            return String.format("%02d:%02d:%02d", departureOffset / 3600, departureOffset / 60, departureOffset % 60);
        }
        return null;
    }
}
