package com.pranav.streakly.models;

public class Habit {
    private String name;
    private String id;
    private String goal;
    private int streakCount;
    private int bestStreak;
    private long lastLoggedDate;

    public Habit(String name, String id, String goal, int streakCount, int bestStreak, long lastLoggedDate) {
        this.name = name;
        this.id = id;
        this.goal = goal;
        this.streakCount = 0;
        this.bestStreak = 0;
        this.lastLoggedDate = 0;
    }

    public Habit() {} // Required for Firestore

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


    public String getId() {
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public int getStreakCount() {
        return streakCount;
    }

    public void setStreakCount(int streakCount) {
        this.streakCount = streakCount;
    }

    public int getBestStreak() {
        return bestStreak;
    }

    public void setBestStreak(int bestStreak) {
        this.bestStreak = bestStreak;
    }

    public long getLastLoggedDate() {
        return lastLoggedDate;
    }

    public void setLastLoggedDate(long lastLoggedDate) {
        this.lastLoggedDate = lastLoggedDate;
    }


}
