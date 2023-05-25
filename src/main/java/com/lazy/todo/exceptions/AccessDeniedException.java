package com.lazy.todo.exceptions;
//return this exception if a user tries to access something they don't have permission for
public class AccessDeniedException extends Exception {

    public AccessDeniedException(String message) {super(message);}

    public AccessDeniedException(){

    }
}
