package com.lazy.todo.exceptions;
//return this exception if a user tries to reset their password with a token that has expired
public class PasswordResetTokenExpiredException extends Exception{

    public PasswordResetTokenExpiredException(String message) {
        super(message);
    }

    public PasswordResetTokenExpiredException() {
    }
}
