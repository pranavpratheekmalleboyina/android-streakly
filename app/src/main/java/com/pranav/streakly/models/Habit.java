package com.pranav.streakly.models;

public class Habit {

    private String name;
    private String id;
    private String goal;
    private int progress;

    public Habit() {} // Required for Firestore

    public Habit(String name, String goal) {
        this.name = name;
        this.goal = goal;
        this.progress = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public int getProgress() {
        return progress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id){
        this.id = id;
    }
}
