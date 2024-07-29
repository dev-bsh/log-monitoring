package com.log_monitoring.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.concurrent.ExecutionException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<String> handleExecutionException(ExecutionException e) {
        log.error(e.getLocalizedMessage());
        return new ResponseEntity<>("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InterruptedException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleInterruptedException(InterruptedException e) {
        // 인터럽트 상태 복원
        Thread.currentThread().interrupt();
        log.error(e.getLocalizedMessage());
        return new ResponseEntity<>("Operation interrupted: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 기본 처리기: 모든 예외를 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.error(e.getLocalizedMessage());
        return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}