package com.spring.myapp.exceptions;

public class SensorNotFoundException extends RuntimeException {
    private static final String MESSAGE = "sensor - There is no registered sensor with that name!;";

    public SensorNotFoundException() {
        super(MESSAGE);
    }
}
