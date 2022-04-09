package com.cs.quzzie.model;

public class ResultQnClass {
    int marked;
    String qid;
    boolean result;

    public ResultQnClass() {
    }

    public ResultQnClass(int marked, String qid, boolean result) {
        this.marked = marked;
        this.qid = qid;
        this.result = result;
    }

    public int getMarked() {
        return marked;
    }

    public void setMarked(int marked) {
        this.marked = marked;
    }

    public String getQid() {
        return qid;
    }

    public void setQid(String qid) {
        this.qid = qid;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}
