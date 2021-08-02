package com.amsy.mobileoffloading.helper;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import needle.Needle;

public class FlushToFile {
    //TODO: Append is not needed since we are flushing the entire stream to file
    public static void writeTextToFile(Context context, String filename, boolean append, String str) {
        Needle.onBackgroundThread().execute(() -> {
            File path = context.getFilesDir();
            File file = new File(path, filename);
            String text = str;
            try {
                text = text + "\n";
                java.io.FileWriter fileWriter = new java.io.FileWriter(file, append);
                fileWriter.write(text);
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("FLUSHTOFILE", "Writing to file failed.");
            }
        });
    }

    public static void writeMatrixToFile(Context context, String filename, int[][] matrix) {
        StringBuilder stringBuilder = new StringBuilder();


        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                stringBuilder.append(matrix[i][j] + "\t");
            }
            stringBuilder.append("\n");
        }

        FlushToFile.writeTextToFile(context, filename, false, stringBuilder.toString());
    }


}
