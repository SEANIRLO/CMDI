package com.tencent.yolov5ncnn.utils;

public class HistoryItem {
    private int ID;
    private String category;
    private String time;
    private float prob;

    public HistoryItem(int ID, String category, String time, float prob)
    {
        this.ID = ID;
        this.category = category;
        this.time = time;
        this.prob = prob;
    }

    public int getID(){ return ID; }
    public String getCategory(){ return category; }
    public String getTime(){ return time; }
    public float getProb(){ return prob; }
}
