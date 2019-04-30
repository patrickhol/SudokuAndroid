package com.example.sudokuapp;

import org.junit.Assert;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

public class SudokuSolverTest {





    private final String[][] sudokuInputGrid = {
            {"0", "3", "0", "0", "8", "9", "6", "0", "1"},
            {"7", "4", "8", "3", "0", "1", "0", "0", "0"},
            {"1", "9", "6", "2", "0", "0", "0", "0", "0"},
            {"0", "5", "0", "0", "0", "8", "0", "0", "4"},
            {"0", "0", "7", "0", "9", "0", "8", "0", "0"},
            {"4", "0", "0", "5", "0", "0", "0", "9", "0"},
            {"0", "0", "0", "0", "0", "3", "2", "8", "5"},
            {"0", "0", "0", "6", "0", "2", "4", "3", "7"},
            {"3", "0", "5", "8", "4", "0", "0", "1", "0"}

    };
    private final int[][] sudokuExpectedOutputGrid = {
            {5, 3, 2, 4, 8, 9, 6, 7, 1},
            {7, 4, 8, 3, 6, 1, 5, 2, 9},
            {1, 9, 6, 2, 7, 5, 3, 4, 8},
            {9, 5, 3, 7, 2, 8, 1, 6, 4},
            {2, 6, 7, 1, 9, 4, 8, 5, 3},
            {4, 8, 1, 5, 3, 6, 7, 9, 2},
            {6, 7, 4, 9, 1, 3, 2, 8, 5},
            {8, 1, 9, 6, 5, 2, 4, 3, 7},
            {3, 2, 5, 8, 4, 7, 9, 1, 6},

    };
    private final int[][] sudokuBadOutputGrid = {
            {5, 0, 2, 4, 8, 9, 6, 7, 1},
            {7, 4, 8, 3, 6, 1, 5, 2, 9},
            {1, 9, 6, 2, 7, 5, 3, 4, 8},
            {9, 5, 3, 7, 2, 8, 1, 6, 4},
            {2, 6, 7, 1, 9, 4, 8, 5, 3},
            {4, 8, 1, 5, 3, 6, 7, 9, 2},
            {6, 7, 4, 9, 1, 3, 2, 8, 5},
            {8, 1, 9, 6, 5, 2, 4, 3, 7},
            {3, 2, 5, 8, 4, 7, 9, 1, 6},

    };



    @RepeatedTest(100)
    public void givenNewSudokuInputWhenSolveSudokuRecursiveThenArraysEqualsTrue() {
        SudokuSolver newSudokuTab = new SudokuSolver(sudokuInputGrid);
        newSudokuTab.solveSudokuRecursive();
        int[][] solvedIntArray = newSudokuTab.getBoadrIntToSolve();
        Assert.assertArrayEquals("Solver result check need true ",sudokuExpectedOutputGrid,solvedIntArray );

    }


    @Test
    public void givenNewSudokuInputWhenSolveSudokuRecursiveThenArraysEqualsFalse() {
        SudokuSolver newSudokuTab = new SudokuSolver(sudokuInputGrid);
        newSudokuTab.solveSudokuRecursive();
        int[][] solvedIntArray = newSudokuTab.getBoadrIntToSolve();
        Assert.assertArrayEquals("Arrays first differed at element [0][1] Expected : 0", sudokuBadOutputGrid,solvedIntArray );

    }
}