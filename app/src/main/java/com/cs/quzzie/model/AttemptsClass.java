package com.cs.quzzie.model;

public class AttemptsClass {
    String id;
    int score;
    int total;
    String uid;

    public AttemptsClass() {
    }

    public AttemptsClass(String id, int score, int total, String uid) {
        this.id = id;
        this.score = score;
        this.total = total;
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
