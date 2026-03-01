package com.rev.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.exception.InsufficientStockException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleResourceNotFound(ResourceNotFoundException e) {
        logger.error("Resource not found: {}", e.getMessage());
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", e.getMessage());
        mav.setViewName("error");
        return mav;
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ModelAndView handleInsufficientStock(InsufficientStockException e) {
        logger.error("Insufficient stock: {}", e.getMessage());
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", e.getMessage());
        mav.setViewName("error");
        return mav;
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ModelAndView handleDataIntegrityViolation(org.springframework.dao.DataIntegrityViolationException e) {
        logger.error("Data integrity violation: {}", e.getMessage());
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage",
                "This action cannot be completed because the record is referenced elsewhere or violates a database constraint.");
        mav.setViewName("error");
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception e) {
        logger.error("An unexpected error occurred: ", e);

        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", e);
        // Provide the specific message if it exists, otherwise a generic one
        String msg = (e.getMessage() != null && !e.getMessage().isEmpty()) ? e.getMessage()
                : "An unexpected error occurred. Please try again later.";
        mav.addObject("errorMessage", msg);
        mav.setViewName("error");
        return mav;
    }
}
