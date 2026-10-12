package com.tech365.inventory;
import java.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
@RestControllerAdvice
public class ApiErrors {
 private ResponseEntity<?> error(int status,String code,String message,Object details){return ResponseEntity.status(status).body(Map.of("error",Map.of("code",code,"message",message,"details",details)));}
 @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<?> validation(MethodArgumentNotValidException e){var details=new TreeMap<String,String>();e.getBindingResult().getFieldErrors().forEach(f->details.put(f.getField(),f.getDefaultMessage()));return error(400,"VALIDATION_ERROR","Check the submitted fields.",details);}
 @ExceptionHandler(DataIntegrityViolationException.class) public ResponseEntity<?> conflict(){return error(409,"DATA_CONFLICT","SKU already exists or database constraint failed.",Map.of());}
 @ExceptionHandler({HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class}) public ResponseEntity<?> badInput(){return error(400,"INVALID_INPUT","Invalid JSON, field type or ID.",Map.of());}
 @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<?> status(ResponseStatusException e){return error(e.getStatusCode().value(),"REQUEST_ERROR",Objects.toString(e.getReason(),"Request failed."),Map.of());}
 @ExceptionHandler(HttpMediaTypeNotSupportedException.class) public ResponseEntity<?> media(){return error(415,"UNSUPPORTED_MEDIA_TYPE","Send application/json.",Map.of());}
 @ExceptionHandler(HttpRequestMethodNotSupportedException.class) public ResponseEntity<?> method(){return error(405,"METHOD_NOT_ALLOWED","Method not supported.",Map.of());}
}
