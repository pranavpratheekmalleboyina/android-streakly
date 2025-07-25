package com.pranav.streakly.models;

public class Habit {
    private String name;
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

    public String getGoal() {
        return goal;
    }

    public int getProgress() {
        return progress;
    }
}
