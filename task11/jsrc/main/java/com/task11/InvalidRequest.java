package com.task11;

public class InvalidRequest extends RuntimeException {
    public InvalidRequest() {
    }

    public InvalidRequest(String message) {
        super(message);
    }
}
