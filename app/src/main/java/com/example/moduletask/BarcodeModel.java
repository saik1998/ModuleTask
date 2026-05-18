package com.example.moduletask;

import java.util.ArrayList;

public class BarcodeModel {
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<TrackerResModel> getBarcode_data() {
        return barcode_data;
    }

    public void setBarcode_data(ArrayList<TrackerResModel> barcode_data) {
        this.barcode_data = barcode_data;
    }

    private String status;
    private ArrayList<TrackerResModel> barcode_data;
}
