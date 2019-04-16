package com.github.evgeniymelnikov.piro.controller;

import com.github.evgeniymelnikov.piro.exception.ResourceIllegalArgumentException;
import com.github.evgeniymelnikov.piro.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler(ResourceIllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponseEntity handleResourceIllegalArgumentException(ResourceIllegalArgumentException e) {
        return createErrorResponseEntity(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponseEntity handleResourceNotFoundException(ResourceNotFoundException e) {
        return createErrorResponseEntity(e, HttpStatus.BAD_REQUEST);
    }

    /**
     * перехватываемый эксцепшн падает в конструкторе WidgetFilter (когда jackson пытается сконструировать объект
     * в качестве параметра для метода контроллера, но не может из-за того, что внутри конструктора вызывается
     * ResourceIllegalArgumentException)
     */
    @ExceptionHandler(HttpMessageConversionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponseEntity handleResourceIllegalArgumentException(HttpMessageConversionException e) {
        log.error("Ошибка при конвертации json-object", e);
        return createErrorResponseEntity(findCause(e, ResourceIllegalArgumentException.class),
                HttpStatus.BAD_REQUEST);
    }

    private static ErrorResponseEntity createErrorResponseEntity(Exception e, HttpStatus httpStatus) {
        return new ErrorResponseEntity(e.getMessage(), httpStatus.getReasonPhrase(), httpStatus.value());
    }

    private static Exception findCause(Exception exception, Class<?> classOfSearchedCause) {
        if (classOfSearchedCause.isInstance(exception) || exception.getCause() == null) {
            return exception;
        }

        return findCause((Exception) exception.getCause(), classOfSearchedCause);
    }
}

