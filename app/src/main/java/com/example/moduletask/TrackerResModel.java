package com.example.moduletask;

import java.io.Serializable;

public class TrackerResModel implements Serializable {
    private String tracking_id;
    private String barcode;
    private String variety;
    private String lot;
    private String qty;
    private String from_location;
    private String cust_code;
    private String sale_order_number;
    private String dispatch_date;
    private String added_by;
    private String created_date;

    public String getTracking_id() {
        return tracking_id;
    }

    public void setTracking_id(String tracking_id) {
        this.tracking_id = tracking_id;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getVariety() {
        return variety;
    }

    public void setVariety(String variety) {
        this.variety = variety;
    }

    public String getLot() {
        return lot;
    }

    public void setLot(String lot) {
        this.lot = lot;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getFrom_location() {
        return from_location;
    }

    public void setFrom_location(String from_location) {
        this.from_location = from_location;
    }

    public String getCust_code() {
        return cust_code;
    }

    public void setCust_code(String cust_code) {
        this.cust_code = cust_code;
    }

    public String getSale_order_number() {
        return sale_order_number;
    }

    public void setSale_order_number(String sale_order_number) {
        this.sale_order_number = sale_order_number;
    }

    public String getDispatch_date() {
        return dispatch_date;
    }

    public void setDispatch_date(String dispatch_date) {
        this.dispatch_date = dispatch_date;
    }

    public String getAdded_by() {
        return added_by;
    }

    public void setAdded_by(String added_by) {
        this.added_by = added_by;
    }

    public String getCreated_date() {
        return created_date;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }
}
