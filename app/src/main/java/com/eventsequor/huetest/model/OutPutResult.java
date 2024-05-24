package com.eventsequor.huetest.model;

import java.util.List;

public class OutPutResult {
    private int totalScore;
    private List<BoxModel> boxModel;

    public OutPutResult(int totalScore, List<BoxModel> boxModel) {
        this.totalScore = totalScore;
        this.boxModel = boxModel;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public List<BoxModel> getBoxModel() {
        return boxModel;
    }

    public void setBoxModel(List<BoxModel> boxModel) {
        this.boxModel = boxModel;
    }
}
