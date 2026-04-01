package com.cda.cdajava.exception;

// Exception reservee aux erreurs fonctionnelles attendues par le metier.
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
