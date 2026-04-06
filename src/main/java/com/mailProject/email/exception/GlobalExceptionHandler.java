package com.mailProject.email.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClickupConfigNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleConfigNotFound(ClickupConfigNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.NOT_FOUND.value());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidConfigException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidConfig(InvalidConfigException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "Something went wrong");
//        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
//
//        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
//    }

    @ExceptionHandler(ClickupSpaceNotSelectedException.class)
    public ResponseEntity<Map<String, Object>> handleSpaceNotSelected(ClickupSpaceNotSelectedException ex) {
        Map<String, Object> res = new HashMap<>();
        res.put("message", ex.getMessage());
        res.put("status", 400);
        return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ClickupListNotSelectedException.class)
    public ResponseEntity<Map<String, Object>> handleListNotSelected(ClickupListNotSelectedException ex) {
        Map<String, Object> res = new HashMap<>();
        res.put("message", ex.getMessage());
        res.put("status", 400);
        return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ClickupListDeletedException.class)
    public ResponseEntity<Map<String, Object>> handleListDeleted(ClickupListDeletedException ex) {
        Map<String, Object> res = new HashMap<>();
        res.put("message", ex.getMessage());
        res.put("status", 410);
        return new ResponseEntity<>(res, HttpStatus.GONE);
    }
}
