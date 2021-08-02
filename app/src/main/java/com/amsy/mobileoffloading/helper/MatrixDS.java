package com.amsy.mobileoffloading.helper;

public class MatrixDS {
    public static int[][] createMatrix(int rowCount, int colCount){
        int [][] matrix = new int[rowCount][colCount];

        for(int i = 0; i < rowCount; i++){
            for(int j = 0 ; j < colCount; j++){
                matrix[i][j] = (i + j) % 128;
                //TODO: Not really sure why this but okay
            }
        }
        return matrix;
    }


    public static int getDotProduct(int[] vector1, int[] vector2){
        int result = 0;
        for(int i = 0 ; i < vector1.length; i++){
            result = result +  (vector1[i] * vector2[i]);
        }
        return result;
    }


    public static int[][] getTranspose(int[][]matrix){
        int[][] result = new int[matrix[0].length][matrix.length];
        int rowSize = matrix[0].length;
        for(int i =0 ; i < matrix.length; i++){//row
            for(int j = 0 ; j < rowSize; j++){//col
                result[j][i] = matrix[i][j];  //transpose
            }
        }
        return result;
    }
}
