package com.parallel.lab2;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.*;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class Main {
    private static final int N = 10000;
    public static final double epsilon = 0.1;
    private static final double t = 0.00001;
    public static final int NumberOfThreads = 2;
    public static void main(String[] args) throws InterruptedException {
        for(int i=0;i<1;i++) {
            List<Double> A = new ArrayList<>();
            List<Double> b = new ArrayList<>();
            List<Double> x = new ArrayList<>();
            InitA(A);
            InitB(b);
            InitX(x);
            boolean flag;
            long startTime = System.nanoTime();
            do {

                //flag = checkAnswer(A, b, x);
                flag = checkAnswerParallel(A, b, x);
                //x = nextStep(A, b, x);
                x = nextStepParallel(A, b, x);

                //System.out.println(x);
            } while (flag);
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


    private static List<Double> nextStepParallel(List<Double> A, List<Double> b, List<Double> x) throws InterruptedException {
        //Ax
        //-b
        //*t
        //x-
        List<Double> new_X;
        new_X = mulMatrixOnVectorParallel(A,x);
        new_X =subVectorOnVector(new_X,b);
        new_X= mulVectorOnConst(new_X,t);
        new_X=subVectorOnVector(x,new_X);
        return new_X;
    }

    private static List<Double> mulMatrixOnVectorParallel(List<Double> A, List<Double> x) throws InterruptedException {
        List<Double> new_X = new ArrayList<>(N);
        for(int i=0;i<N;i++){
            new_X.add((double) 0);
        }

        ExecutorService service = Executors.newFixedThreadPool(NumberOfThreads); // Thread

        for(int thread=0;thread<NumberOfThreads;thread+=1){
            int row=thread*N/NumberOfThreads;
            service.execute(() -> {
                int currentRow=row;
                for(int i=0;i<N/NumberOfThreads;currentRow++,i++) {
                    double sumOfElements = 0;
                    for (int column = 0; column < N; column++) {
                        sumOfElements += A.get(currentRow * N + column);
                    }
                    sumOfElements *= x.get(currentRow);
                    new_X.set(currentRow, sumOfElements);
                }
            });
        }
        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);
        return new_X;
    }

    private static boolean checkAnswerParallel(List<Double> A, List<Double> b, List<Double> x) throws InterruptedException {
        //Ax
        //-b
        //takenormX
        //takenormB
        // X/B
        List<Double> new_X;
        new_X = mulMatrixOnVectorParallel(A, x);
        new_X = subVectorOnVector(new_X, b);
        double xDouble = takeNorm(new_X);
        double bDouble = takeNorm(b);
        if (xDouble / bDouble < epsilon) return false;
        else return true;
    }
}
