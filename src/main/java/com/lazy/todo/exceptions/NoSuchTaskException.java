package com.lazy.todo.exceptions;
//return this exception if a user tries to access a task that doesn't exist
public class NoSuchTaskException extends Exception{

    public NoSuchTaskException(String message) {super(message);}

    public NoSuchTaskException(){

    }
}
