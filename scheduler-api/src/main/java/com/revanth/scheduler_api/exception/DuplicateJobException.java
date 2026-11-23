package com.revanth.scheduler_api.exception;

public class DuplicateJobException extends RuntimeException {

    public DuplicateJobException(String message) {
        super(message);
    }
}