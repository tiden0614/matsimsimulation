package com.mitsubishi.simulation.input.osm;

/**
 * Created by tiden on 7/6/2015.
 */
public class InvalidFilterValueException extends Exception {

    public InvalidFilterValueException() {
    }

    public InvalidFilterValueException(String message) {
        super(message);
    }

    public InvalidFilterValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidFilterValueException(Throwable cause) {
        super(cause);
    }

    public InvalidFilterValueException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
