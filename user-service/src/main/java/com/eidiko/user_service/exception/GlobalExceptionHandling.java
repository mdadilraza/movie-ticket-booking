package com.eidiko.user_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandling {

    // Handle UserAlreadyExistException (409 - Conflict)
    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ProblemDetail> handleUserAlreadyExistException(UserAlreadyExistException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problemDetail.setType(URI.create("https://example.com/errors/user-already-exists"));
        problemDetail.setTitle("User Already Exists");
        problemDetail.setDetail("User is already registered, please try with a different username or email.");
        return new ResponseEntity<>(problemDetail, HttpStatus.CONFLICT);
    }

    // Handle UserNotFoundException (404 - Not Found)
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUserNotFoundException(UserNotFoundException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problemDetail.setType(URI.create("https://example.com/errors/user-not-found"));
        problemDetail.setTitle("User Not Found");
        problemDetail.setDetail("User with the provided username or email could not be found.");
        return new ResponseEntity<>(problemDetail, HttpStatus.NOT_FOUND);
    }

    // Handle generic Exception (500 - Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception e) {
        e.printStackTrace();
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
        problemDetail.setType(URI.create("https://example.com/errors/internal-server-error"));
        problemDetail.setTitle("Internal Server Error");
        return new ResponseEntity<>(problemDetail, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
