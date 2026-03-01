package com.rev.app.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.exception.InsufficientStockException;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleResourceNotFound(ResourceNotFoundException e) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", e.getMessage());
        mav.setViewName("error");
        return mav;
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ModelAndView handleInsufficientStock(InsufficientStockException e) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", e.getMessage());
        mav.setViewName("error");
        return mav;
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ModelAndView handleDataIntegrityViolation(org.springframework.dao.DataIntegrityViolationException e) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage",
                "This action cannot be completed because the record is referenced elsewhere or violates a database constraint.");
        mav.setViewName("error");
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception e) {
        // Log to file for developer debugging
        try (FileWriter fw = new FileWriter("debug_error.txt", true);
                PrintWriter pw = new PrintWriter(fw)) {
            pw.println("--- NEW ERROR --- [" + java.time.LocalDateTime.now() + "]");
            e.printStackTrace(pw);
            pw.println("-----------------");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

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
