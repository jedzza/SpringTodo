package com.lazy.todo.exceptions;
//return this exception if a user tries to access a project that doesn't exist
public class NoSuchProjectException extends Exception{

    public NoSuchProjectException(String message) {super(message);}

    public NoSuchProjectException(){

    }
}
