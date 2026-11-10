package za.ac.mycput.interfaces.rest.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.exception.DuplicateEntityException;
import za.ac.mycput.domain.exception.EntityNotFoundException;
import za.ac.mycput.domain.exception.OrganisationAccessException;
import za.ac.mycput.domain.exception.PermissionDeniedException;
import za.ac.mycput.interfaces.rest.dto.ApiError;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    ResponseEntity<ApiError> missing(EntityNotFoundException exception) {
        String code = exception.entity().toUpperCase().replace(' ', '_') + "_NOT_FOUND";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(code, exception.getMessage()));
    }

    @ExceptionHandler(DuplicateEntityException.class)
    ResponseEntity<ApiError> duplicate(DuplicateEntityException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError("DUPLICATE", exception.getMessage()));
    }

    @ExceptionHandler(OrganisationAccessException.class)
    ResponseEntity<ApiError> organisation(OrganisationAccessException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiError("ORGANISATION_ACCESS", exception.getMessage()));
    }

    @ExceptionHandler(PermissionDeniedException.class)
    ResponseEntity<ApiError> forbidden(PermissionDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiError("FORBIDDEN", exception.getMessage()));
    }

    @ExceptionHandler(DomainRuleException.class)
    ResponseEntity<ApiError> rule(DomainRuleException exception) {
        return ResponseEntity.badRequest().body(new ApiError("DOMAIN_RULE", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> invalid(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .reduce((left, right) -> left + "; " + right)
                .orElse("Request validation failed");
        return ResponseEntity.badRequest().body(new ApiError("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> unreadable(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body(new ApiError("MALFORMED_JSON", "The request body could not be read"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> conflict(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("CONFLICT", "The change conflicts with related portfolio records"));
    }
}
