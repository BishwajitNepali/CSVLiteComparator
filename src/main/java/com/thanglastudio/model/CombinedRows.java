package com.thanglastudio.model;

public class CombinedRows {
    private String[] rowFromTable1;
    private String[] rowFromTable2;

    public CombinedRows(String[] rowFromTable1, String[] rowFromTable2) {
        this.rowFromTable1 = rowFromTable1;
        this.rowFromTable2 = rowFromTable2;
    }

    public String[] getRowFromTable1() {
        return rowFromTable1;
    }

    public void setRowFromTable1(String[] rowFromTable1) {
        this.rowFromTable1 = rowFromTable1;
    }

    public String[] getRowFromTable2() {
        return rowFromTable2;
    }

    public void setRowFromTable2(String[] rowFromTable2) {
        this.rowFromTable2 = rowFromTable2;
    }
}
