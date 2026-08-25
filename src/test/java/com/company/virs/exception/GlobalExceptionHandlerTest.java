package com.company.virs.exception;

import com.company.virs.dto.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void handleValidation() {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/api/reconcile");

        ResponseEntity<ErrorResponse> response =
                handler.handleValidation(
                        new ValidationException("Invalid file"),
                        request
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );
    }

    @Test
    void handleNotFound() {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/api/result");

        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(
                        new ResourceNotFoundException("Not found"),
                        request
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );
    }

    @Test
    void handleMissingHeader() throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/api/test");

        MissingRequestHeaderException exception =
                new MissingRequestHeaderException(
                        "Batch-Id",
                        null
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleMissingHeader(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );
    }

    @Test
    void handleNotAcceptable() {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/api/export");

        ResponseEntity<ErrorResponse> response =
                handler.handleNotAcceptable(
                        new HttpMediaTypeNotAcceptableException(
                                "Unsupported"
                        ),
                        request
                );

        assertEquals(
                HttpStatus.NOT_ACCEPTABLE,
                response.getStatusCode()
        );
    }

    @Test
    void handleDataIntegrity() {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/api/retrigger");

        ResponseEntity<ErrorResponse> response =
                handler.handleDataIntegrity(
                        new DataIntegrityViolationException(
                                "duplicate"
                        ),
                        request
                );

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );
    }

    @Test
    void handleUnexpected() {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/api/error");

        ResponseEntity<ErrorResponse> response =
                handler.handleUnexpected(
                        new RuntimeException("Error"),
                        request
                );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );
    }
}