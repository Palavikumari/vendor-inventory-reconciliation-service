package com.company.virs.exception;

public class DuplicateBatchException extends RuntimeException {

    public DuplicateBatchException(String message) {
        super(message);
    }
}