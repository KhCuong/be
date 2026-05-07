//package com.dev.demo.validator;
//
//import jakarta.validation.ConstraintValidator;
//import jakarta.validation.ConstraintValidatorContext;
//import java.time.LocalDate;
//import java.time.Period;
//import java.util.Objects;
//
//public class DobValidator implements ConstraintValidator<DobContraint, LocalDate> {
//    private int min;
//    @Override
//    public void initialize(DobContraint constraintAnnotation) {
//        ConstraintValidator.super.initialize(constraintAnnotation);
//        min = constraintAnnotation.min();
//    }
//
//    @Override
//    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
//        if(Objects.isNull(localDate)) {
//            return true;
//        }
//        LocalDate now = LocalDate.now();
//
//        long years = Period.between(localDate, now).getYears();
//        return years >= min;
//    }
//}
