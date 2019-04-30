package com.example.sudokuapp;

public class Sudoku {
    private String ocrString;

    public Sudoku(String ocrString) {
        this.ocrString = ocrString;
    }

    public String getOcrString() {
        return ocrString;
    }

    public void setOcrString(String ocrString) {
        this.ocrString = ocrString;
    }
}
