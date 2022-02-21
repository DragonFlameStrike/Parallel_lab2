package com.parallel.lab2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class ParallelSecond {
    public static ArrayList<Double> time;
    private static final int N = 5000;
    public static final double epsilon = 0.000001;
    private static final double t = 0.00001;
    public static final int NumberOfThreads = 4;
    public static void main(String[] args) throws InterruptedException {
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
            ExecutorService service = Executors.newCachedThreadPool();
            do {
                mulAxSubb = mulMatrixOnVectorParallel(A, x, service); //N^2 99.5%
                mulAxSubb = subVectorOnVector(mulAxSubb,b); //N 0.1%
                flag = checkAnswer(mulAxSubb, b); //2N 0.2%
                x = nextStep(mulAxSubb, x);//2N 0.2%
                //System.out.println(x);
            } while (flag);
            long elapsedNanos = System.nanoTime() - startTime;
            if(elapsedNanos / 1000000000.0<min_time){
                min_time=elapsedNanos / 1000000000.0;
            }
            service.shutdown();
            service.awaitTermination(1, TimeUnit.MINUTES);
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

    private static List<Double> mulMatrixOnVectorParallel(List<Double> A, List<Double> x,ExecutorService service) {

        List<Double> output = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            output.add((double) 0);
        }
        List<Future> arrFutures = new ArrayList<>();
        long startTime1 = System.nanoTime();
        double[] arr={0,0,0,0,0,0,0,0,0};
        long startTime = System.nanoTime();
        for(int currentThread = 0; currentThread < NumberOfThreads; currentThread += 1) {
            int row = currentThread * N / NumberOfThreads;
            int finalCurrentThread = currentThread;
            arrFutures.add(
                    service.submit(() -> {
                        int counter=0;
                        arr[finalCurrentThread] = row;
                        int currentRow = row;
                        for (int i = 0; i < (N / NumberOfThreads); currentRow++, i++) {
                            double sumOfElements = 0;
                            for (int column = 0; column < N; column++) {
                                counter++;
                                sumOfElements += A.get(currentRow * N + column);
                            }
                            sumOfElements *= x.get(currentRow);
                            output.set(currentRow, sumOfElements);
                        }
                        long elapsedNanos = System.nanoTime() - startTime;
                        arr[row * NumberOfThreads / N]=elapsedNanos / 1000000000.0;
                        arr[(row * NumberOfThreads / N)+4]=counter;
                    })
            );
        }
        double elapsedNanos1 = (System.nanoTime() - startTime1)/ 1000000000.0;
        do {
            arrFutures.removeIf(Future::isDone);
        }while(!arrFutures.isEmpty());
        double elapsedNanos2 = (System.nanoTime() - startTime1)/ 1000000000.0;
        System.out.println(Arrays.toString(arr));
        System.out.println("time1 " + elapsedNanos1);
        System.out.println("time2 " + elapsedNanos2);
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


