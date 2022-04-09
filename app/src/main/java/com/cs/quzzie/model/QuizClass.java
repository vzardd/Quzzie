package com.cs.quzzie.model;

public class QuizClass {
    private String name;
    private String id;
    private Object timestamp;
    private boolean accepting;

    public QuizClass() {
    }

    public QuizClass(String name, String id, Object timestamp, boolean accepting) {
        this.name = name;
        this.id = id;
        this.timestamp = timestamp;
        this.accepting = accepting;
    }

    public boolean isAccepting() {
        return accepting;
    }

    public void setAccepting(boolean accepting) {
        this.accepting = accepting;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}
