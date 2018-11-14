package com.thanglastudio.model;

public class Row {
    String id;
    String[] rowdata;

    public Row(String id, String[] rowdata) {
        this.id = id;
        this.rowdata = rowdata;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getRowdata() {
        return rowdata;
    }

    public void setRowdata(String[] rowdata) {
        this.rowdata = rowdata;
    }
}
