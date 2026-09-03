package com.spring.myapp.exceptions;

public class SensorAlreadyExistsException extends RuntimeException {
    private static final String MESSAGE = "name - There's already a sensor by that name!;";

    public SensorAlreadyExistsException() {
        super(MESSAGE);
    }

    public SensorAlreadyExistsException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
