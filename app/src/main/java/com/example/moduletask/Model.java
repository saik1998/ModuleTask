package com.example.moduletask;

import java.util.ArrayList;

public class Model {
    private String batch;
    private String quantity;

    private ArrayList spinnerListData;

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public ArrayList getSpinnerListData() {
        return spinnerListData;
    }

    public void setSpinnerListData(ArrayList spinnerListData) {
        this.spinnerListData = spinnerListData;
    }
}
