package com.parallel.lab2;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.*;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class Main2 {
    private static final int N = 10000;
    public static final double epsilon = 0.1;
    private static final double t = 0.00001;
    public static void main(String[] args) throws InterruptedException {
        for(int i=0;i<10;i++) {
            List<Double> A = new ArrayList<>();
            List<Double> b = new ArrayList<>();
            List<Double> x = new ArrayList<>();
            InitA(A);
            InitB(b);
            InitX(x);
            List<Boolean> flag = new ArrayList<>();
            flag.add(true);
            long startTime = System.nanoTime();

            do {
                ExecutorService service = Executors.newSingleThreadExecutor();
                //flag = checkAnswer(A, b, x);
                List<Double> finalX = x;
                service.execute(() -> {
                    if(!checkAnswer(A, b, finalX)){ //checkAnswerParallel(A, b, finalX)
                        flag.set(0,false);
                    }
                });
                //x = nextStep(A, b, x);
                x = nextStep(A, b, x);
                service.shutdown();
                service.awaitTermination(1, TimeUnit.MINUTES);
                //System.out.println(x);
            } while (flag.get(0));
            long elapsedNanos = System.nanoTime() - startTime;
            System.out.println(elapsedNanos / 1000000000.0);
        }
    }

    private static boolean checkAnswer(List<Double> A, List<Double> b, List<Double> x) {
        //Ax
        //-b
        //takenormX
        //takenormB
        // X/B
        List<Double> new_X;
        new_X = mulMatrixOnVector(A, x);
        new_X = subVectorOnVector(new_X, b);
        double xDouble = takeNorm(new_X);
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

    private static List<Double> nextStep(List<Double> A, List<Double> b, List<Double> x) {
        //Ax
        //-b
        //*t
        //x-
        List<Double> new_X;
        new_X = mulMatrixOnVector(A,x);
        new_X =subVectorOnVector(new_X,b);
        new_X= mulVectorOnConst(new_X,t);
        new_X=subVectorOnVector(x,new_X);
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
        List<Double> new_X = new ArrayList<>(N);
        for(int row=0;row<N;row++){
            // here should add Thread
            int currentRow=row;
            double sumOfElements=0;
            for(int column=0;column<N;column++){
                sumOfElements+=A.get(currentRow*N+column);
            }
            sumOfElements*=x.get(currentRow);
            new_X.add(currentRow,sumOfElements);
        }
        return new_X;
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
