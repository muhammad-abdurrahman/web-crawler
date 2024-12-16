package com.github.muhammadabdurrahman.webcrawler.presentation.advice;

import com.github.muhammadabdurrahman.webcrawler.presentation.exception.ConciseProblem;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Problem> handleValidationExceptions(MethodArgumentNotValidException ex) {
    final Set<String> validationErrors = new HashSet<>();
    ex.getBindingResult().getFieldErrors()
        .forEach(error -> validationErrors.add("[field: %s, error: %s]".formatted(error.getField(), error.getDefaultMessage())));

    var problem = ConciseProblem.builder()
        .title("Validation errors occurred")
        .detail(String.join(", ", validationErrors))
        .status(Status.BAD_REQUEST)
        .message("Validation failed for one or more fields")
        .build();

    return ResponseEntity.badRequest().body(problem);
  }

  @ExceptionHandler(RejectedExecutionException.class)
  public ResponseEntity<Problem> handleRejectedExecutionException() {
    var problem = ConciseProblem.builder()
        .title("Service Unavailable")
        .detail("The server is currently busy. Please try again later.")
        .status(Status.SERVICE_UNAVAILABLE)
        .message("Server is busy")
        .build();
    return ResponseEntity.status(Status.SERVICE_UNAVAILABLE.getStatusCode()).body(problem);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Problem> handleGenericException(Exception ex) {
    log.error("An unexpected error occurred", ex);
    var problem = ConciseProblem.builder()
        .title("Internal Server Error")
        .detail("An unexpected error occurred: %s".formatted(ex.getMessage()))
        .status(Status.INTERNAL_SERVER_ERROR)
        .message("An unexpected error occurred")
        .build();
    return ResponseEntity.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).body(problem);
  }
}