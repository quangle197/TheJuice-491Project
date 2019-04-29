package com.example.quangle.myapplication;

public class User {
    private String name;
    private String color;

    public User(String name, String color){
        this.name = name;
        this.color = color;
    }

    public User(){}

    public String getName(){return name;}
    public String getColor(){return color;}
}
