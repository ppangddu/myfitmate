package com.myfitmate.myfitmate.domain.food.exception;

import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.myfitmate.myfitmate.domain.food")
public class FoodExceptionHandler {

    @ExceptionHandler(FoodException.class)
    public ResponseEntity<ErrorResponse> handleFoodException(FoodException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getErrorCode());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(errorResponse);
    }

    @Getter
    private static class ErrorResponse {
        private final String message;
        private final int status;

        public ErrorResponse(FoodErrorCode errorCode) {
            this.message = errorCode.getMessage();
            this.status = errorCode.getStatus().value();
        }
    }
}
