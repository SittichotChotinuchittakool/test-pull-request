package com.example.wongnai.exceptions;

public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(Class c){
        super(c.getName() + " not found");
    }
}
