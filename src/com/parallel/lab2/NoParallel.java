package com.parallel.lab2;

import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class NoParallel {
    public static ArrayList<Double> time;
    private static final int N = 5000;
    public static final double epsilon = 0.000001;
    private static final double t = 0.00001;
    public static void main(String[] args) {
        double min_time=1000;
        for(int i=0;i<4;i++) {
            List<Double> A = new ArrayList<>();
            List<Double> b = new ArrayList<>();
            List<Double> x = new ArrayList<>();
            List<Double> mulAxSubb;
            time=new ArrayList<>();
            InitA(A);
            InitB(b);
            InitX(x);
            boolean flag;
            long startTime = System.nanoTime();
            do {
                mulAxSubb = mulMatrixOnVector(A, x); //N^2
                mulAxSubb = subVectorOnVector(mulAxSubb,b); //N
                flag = checkAnswer(mulAxSubb, b); //2N
                x = nextStep(mulAxSubb, x);//2N
                //System.out.println(x);
            } while (flag);
            long elapsedNanos = System.nanoTime() - startTime;
            if(elapsedNanos / 1000000000.0<min_time){
                min_time=elapsedNanos / 1000000000.0;
            }
        }
        System.out.println(min_time);
    }

    private static boolean checkAnswer(List<Double> mulAx, List<Double> b) {
        //takenormX
        //takenormB
        // X/B
        double xDouble = takeNorm(mulAx);
        double bDouble = takeNorm(b);
        if (xDouble / bDouble < epsilon) return false;
        else return true;
    }

    private static double takeNorm(List<Double> vector) {
        double sum=0;
        for(int i=0;i<N;i++){
            sum+=vector.get(i);
        }
        return sqrt(abs(sum));
    }

    private static List<Double> nextStep(List<Double> mulAxSubb, List<Double> x) {
        //*t
        //x-
        List<Double> new_X;
        new_X = mulVectorOnConst(mulAxSubb, t);
        new_X = subVectorOnVector(x, new_X);
        return new_X;
    }

    private static List<Double> mulVectorOnConst(List<Double> x, double t) {
        List<Double> new_X = new ArrayList<>(N);
        for(int row=0;row<N;row++){
            new_X.add(row,x.get(row)*t);
        }
        return new_X;
    }

    private static List<Double> subVectorOnVector(List<Double> x, List<Double> b) {
        List<Double> new_X = new ArrayList<>(N);
        for(int row=0;row<N;row++){
            new_X.add(row,x.get(row)-b.get(row));
        }
        return new_X;
    }

    private static List<Double> mulMatrixOnVector(List<Double> A, List<Double> x) {
        List<Double> output = new ArrayList<>(N);
        for(int row=0;row<N;row++){
            double sumOfElements=0;
            for(int column=0;column<N;column++){
                sumOfElements+=A.get(row * N+column);
            }
            sumOfElements*=x.get(row);
            output.add(row,sumOfElements);
        }
        return output;
    }

    public static void InitA(List<Double> A) {
        for(int i=0;i<N;i++){
            for(int j=0;j<N;j++){
                if(i==j){
                    A.add(2.0);
                }
                else{
                    A.add(1.0);
                }
            }
        }
    }
    public static void InitB(List<Double> b) {
        for(int i=0;i<N;i++){
            b.add(N+1.0);
        }
    }
    public static void InitX(List<Double> x) {
        for(int i=0;i<N;i++){
            x.add((double) 10);
        }
    }
}


