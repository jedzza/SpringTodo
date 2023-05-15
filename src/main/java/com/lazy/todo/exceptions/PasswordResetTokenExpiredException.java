package com.lazy.todo.exceptions;

public class PasswordResetTokenExpiredException extends Exception{

    public PasswordResetTokenExpiredException(String message) {
        super(message);
    }

    public PasswordResetTokenExpiredException() {
    }
}
