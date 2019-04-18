package com.example.sudokuapp;

public class SudokuSolver {


    public int[][] getBoadrIntToSolve() {
        return boadrIntToSolve;
    }

    private final int[][] boadrIntToSolve;
    public static final int SIZE_SUDOKU_BOARD = 9;


    public SudokuSolver(String[][] boardStringToSolve) {
        this.boadrIntToSolve = new int[SIZE_SUDOKU_BOARD][SIZE_SUDOKU_BOARD];
        for (int i = 0; i < SIZE_SUDOKU_BOARD; i++) {
            for (int j = 0; j < SIZE_SUDOKU_BOARD; j++) {
                this.boadrIntToSolve[i][j] = Integer.parseInt(boardStringToSolve[i][j]);
            }
        }
    }


    private boolean checkNumberInRow(int row, int number) {
        for (int i = 0; i < SIZE_SUDOKU_BOARD; i++) {
            if (boadrIntToSolve[row][i] == number) {
                return true;
            }
        }
        return false;
    }

    private boolean checkNumberInColumn(int col, int number) {
        for (int i = 0; i < SIZE_SUDOKU_BOARD; i++) {
            if (boadrIntToSolve[i][col] == number) {
                return true;
            }
        }
        return false;
    }

    private boolean checkNumberInBox(int row, int col, int number) {
        int boxRow = row - row % 3;
        int boxCol = col - col % 3;

        for (int i = boxRow; i < boxRow + 3; i++) {
            for (int j = boxCol; j < boxCol + 3; j++) {
                if (boadrIntToSolve[i][j] == number) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkNumberInRowColumnBox(int row, int col, int number) {
        return !checkNumberInRow(row, number) && !checkNumberInColumn(col, number) && !checkNumberInBox(row, col, number);
    }

    public boolean solveSudokuRecursive() {
        for (int row = 0; row < SIZE_SUDOKU_BOARD; row++) {
            for (int col = 0; col < SIZE_SUDOKU_BOARD; col++) {
                if (boadrIntToSolve[row][col] == 0) {
                    for (int number = 1; number <= SIZE_SUDOKU_BOARD; number++) {
                        if (checkNumberInRowColumnBox(row, col, number)) {
                            boadrIntToSolve[row][col] = number;
                            if (solveSudokuRecursive()) {
                                return true;
                            } else {
                                boadrIntToSolve[row][col] = 0;
                            }

                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder tableToString = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                tableToString.append(boadrIntToSolve[i][j]).append(" ");
            }
            tableToString.append("\n");
        }

        return tableToString.toString();
    }

}
