package com.example.mainapp.ui.main;

import android.content.Context;
import android.util.Log;

import com.example.mainapp.FixDTW;
import com.example.mainapp.KNNModel;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;

import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    private static final int MAX_Points_TEMPLATE = 100;
    private static final int MAX_Points_TEST = 1;
    private double[][] matrix = new double[3][132];
    private ArrayList<WatchPoint> mPoints_temp = new ArrayList<>(MAX_Points_TEMPLATE);
    private ArrayList<WatchPoint> mPoints_test = new ArrayList<>(MAX_Points_TEST);
    private ArrayList<Double> mDistanceDTW = new ArrayList<Double>(MAX_Points_TEMPLATE);

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public void initTempData(String tempfilePath) {
        mPoints_temp.clear();
        JsonArray jsonArray = Utils.getJsonArrayFromFile(tempfilePath);
        Iterator<JsonElement> iterator = jsonArray.iterator();
        while (iterator.hasNext()) {
            JsonElement element = iterator.next();
            JsonArray jsonArray1 = element.getAsJsonArray();
            JsonArray jsonArray30 = jsonArray1.get(0).getAsJsonArray();
            JsonArray jsonArray31 = jsonArray1.get(1).getAsJsonArray();
            JsonArray jsonArray32 = jsonArray1.get(2).getAsJsonArray();

            WatchPoint watchPoint = new WatchPoint();
            for (int i = 0; i < WatchPoint.Sample_size; i++) {
                double n1 = Utils.getdoubleFromStr(jsonArray30.get(i).getAsString());
                matrix[0][i] = n1;
                double n2 = Utils.getdoubleFromStr(jsonArray31.get(i).getAsString());
                matrix[1][i] = n2;
                double n3 = Utils.getdoubleFromStr(jsonArray32.get(i).getAsString());
                matrix[2][i] = n3;
            }
            watchPoint.initData(matrix[0], matrix[1], matrix[2]);
            mPoints_temp.add(watchPoint);

        }
    }

        // test.json
        public void initTestData (String testfilePath){
            mPoints_test.clear();
            JsonArray jsonArray = Utils.getJsonArrayFromFile(testfilePath);
            JsonArray jsonArray30 = jsonArray.get(0).getAsJsonArray();
            JsonArray jsonArray31 = jsonArray.get(1).getAsJsonArray();
            JsonArray jsonArray32 = jsonArray.get(2).getAsJsonArray();

            WatchPoint watchPoint = new WatchPoint();
            for (int i = 0; i < 132; i++) {
                double n1 = Utils.getdoubleFromStr(jsonArray30.get(i).getAsString());
                matrix[0][i] = n1;
                double n2 = Utils.getdoubleFromStr(jsonArray31.get(i).getAsString());
                matrix[1][i] = n2;
                double n3 = Utils.getdoubleFromStr(jsonArray32.get(i).getAsString());
                matrix[2][i] = n3;
            }
            watchPoint.initData(matrix[0], matrix[1], matrix[2]);
            mPoints_test.add(watchPoint);
        }

        public long dodtw (String tsetDTWFile){
            long costTime = 0 ;
            mDistanceDTW.clear();
            long startTime = System.currentTimeMillis();
            for (WatchPoint watchPointTemp : mPoints_temp) {
                for (WatchPoint watchPointTest : mPoints_test) {
                    double distanDTW = watchPointTemp.getDistanceByDTW(watchPointTest);
                    mDistanceDTW.add(distanDTW);
                }
            }
            long endTime = System.currentTimeMillis();
            costTime = endTime - startTime;

            JsonArray jsonArray = new JsonArray();
            for (Double d : mDistanceDTW) {
                jsonArray.add(d);
            }

            Utils.writeJsonArrayFromFile(tsetDTWFile, jsonArray);
            return costTime;
        }

    public long doPredictionDTW(String tempfilePath, String testfilePath, String resultDTWJson) {
        Gson gson = new Gson();

        try {
            double[][][] a = gson.fromJson(new FileReader(new File(new URI(tempfilePath))), double[][][].class);
            double[][] b = gson.fromJson(new FileReader(new File(new URI(testfilePath))), double[][].class);

            long st = System.currentTimeMillis();
            double[] re = FixDTW.calcMultiDTW(a, b);
            long end = System.currentTimeMillis();
            PrintWriter writer = new PrintWriter(resultDTWJson);
            writer.println(new Gson().toJson(re).replaceAll(",", ",\n    ")
                    .replaceAll("\\[", "\\[\n    ")
                    .replaceAll("]", "    \n]"));
            writer.flush();
            return end - st;
        } catch (FileNotFoundException | URISyntaxException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public long doPredictionOptimizeDTW(String tempfilePath, String testfilePath, String resultDTWJson) {
        Gson gson = new Gson();

        try {
            double[][][] a = gson.fromJson(new FileReader(new File(new URI(tempfilePath))), double[][][].class);
            double[][] b = gson.fromJson(new FileReader(new File(new URI(testfilePath))), double[][].class);

            // todo ..... change if u want to try other window size, but for b = 3, only w=0,1,2(eq dtw) is meaningful
            int w = b.length / 2;
            w = Math.max(1, w);
            long st = System.currentTimeMillis();
            double[] re = FixDTW.calcOptimizeMultiDTW(w, a, b);
            long end = System.currentTimeMillis();
            PrintWriter writer = new PrintWriter(resultDTWJson);
            writer.println(new Gson().toJson(re).replaceAll(",", ",\n    ")
                    .replaceAll("\\[", "\\[\n    ")
                    .replaceAll("]", "    \n]"));
            writer.flush();
            return end - st;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public long doPredictionNDDTW(String tempfilePath, String testfilePath, String resultDTWJson) {
        Gson gson = new Gson();

        try {
            double[][][] a = gson.fromJson(new FileReader(new File(new URI(tempfilePath))), double[][][].class);
            double[][] b = gson.fromJson(new FileReader(new File(new URI(testfilePath))), double[][].class);

            int w = 8;
            long st = System.currentTimeMillis();
            double[] re = FixDTW.calcMultiNDDTW(w, a, b);
            long end = System.currentTimeMillis();
            PrintWriter writer = new PrintWriter(resultDTWJson);
            writer.println(new Gson().toJson(re).replaceAll(",", ",\n    ")
                    .replaceAll("\\[", "\\[\n    ")
                    .replaceAll("]", "    \n]"));
            writer.flush();
            return end - st;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public long doPredictModel(String tempfilePath, String testfilePath, String resultDTWJson, Context context) {
        Gson gson = new Gson();

        try {
            float[][][] a = gson.fromJson(new FileReader(new File(new URI(testfilePath))), float[][][].class);
            long st = System.currentTimeMillis();
            int[] re = FixDTW.calDTWModel(a, context);
            long end = System.currentTimeMillis();
            PrintWriter writer = new PrintWriter(resultDTWJson);
            writer.println(new Gson().toJson(re).replaceAll(",", ",\n    ")
                    .replaceAll("\\[", "\\[\n    ")
                    .replaceAll("]", "    \n]"));
            writer.flush();
            return end - st;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return -1;
    }


    public long doPredictRfModel(String tempfilePath, String testfilePath, String resultDTWJson, Context context) {
        Gson gson = new Gson();

        try {
            double[][][] a = gson.fromJson(new FileReader(new File(new URI(testfilePath))), double[][][].class);
            long st = System.currentTimeMillis();
            int[] re = FixDTW.calRFModel(a);
            long end = System.currentTimeMillis();
            PrintWriter writer = new PrintWriter(resultDTWJson);
            writer.println(new Gson().toJson(re).replaceAll(",", ",\n    ")
                    .replaceAll("\\[", "\\[\n    ")
                    .replaceAll("]", "    \n]"));
            writer.flush();
            return end - st;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public long doPredictSVCModel(String tempfilePath, String testfilePath, String resultDTWJson, Context context) {
        Gson gson = new Gson();

        try {
            double[][][] a = gson.fromJson(new FileReader(new File(new URI(testfilePath))), double[][][].class);
            long st = System.currentTimeMillis();
            int[] re = FixDTW.calSVCModel(a);
            long end = System.currentTimeMillis();
            PrintWriter writer = new PrintWriter(resultDTWJson);
            writer.println(new Gson().toJson(re).replaceAll(",", ",\n    ")
                    .replaceAll("\\[", "\\[\n    ")
                    .replaceAll("]", "    \n]"));
            writer.flush();
            return end - st;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public long doPredictKNNModel(String tempfilePath, String testfilePath, String resultDTWJson, Context context) {
        Gson gson = new Gson();

        try {
            KNNModel model = gson.fromJson(new FileReader(new File(new URI(tempfilePath))), KNNModel.class);
            double[][][] a = gson.fromJson(new FileReader(new File(new URI(testfilePath))), double[][][].class);
            long st = System.currentTimeMillis();
            int[] re = FixDTW.calKNNModel(a, model.getX(), model.getY());
            long end = System.currentTimeMillis();
            PrintWriter writer = new PrintWriter(resultDTWJson);
            writer.println(new Gson().toJson(re).replaceAll(",", ",\n    ")
                    .replaceAll("\\[", "\\[\n    ")
                    .replaceAll("]", "    \n]"));
            writer.flush();
            return end - st;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return -1;
    }


    public long doPredictGSModel(String tempfilePath, String testfilePath, String resultDTWJson, Context context) {
        Gson gson = new Gson();

        try {
            double[][][] a = gson.fromJson(new FileReader(new File(new URI(testfilePath))), double[][][].class);
            long st = System.currentTimeMillis();
            int[] re = FixDTW.calGSModel(a);
            long end = System.currentTimeMillis();
            PrintWriter writer = new PrintWriter(resultDTWJson);
            writer.println(new Gson().toJson(re).replaceAll(",", ",\n    ")
                    .replaceAll("\\[", "\\[\n    ")
                    .replaceAll("]", "    \n]"));
            writer.flush();
            return end - st;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return -1;
    }
}