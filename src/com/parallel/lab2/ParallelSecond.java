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
    public static final int N = 5000;
    public static final double epsilon = 0.000001;
    public static final double t = 0.00001;
    public static final int NumberOfThreads = 2;
    public static List<Double> x = new ArrayList<>();
    public static List<Double> A = new ArrayList<>();
    public static List<Double> mulAxSubb = new ArrayList<>();
    public static List<Boolean> isWait = new ArrayList<>();
    public static List<Double> b = new ArrayList<>();
    public static boolean flagThreadsWorks;
    public static boolean AnswerNotFounded;
    public static ExecutorService MatrixThreads;
    public static boolean Over;


    public static void main(String[] args) throws InterruptedException {
        double min_time=1000;
        for(int i=0;i<4;i++) {
            Over=false;
            AnswerNotFounded = true;
            flagThreadsWorks = true;
            System.out.println("start loop " + i);
            InitA(A);
            InitB(b);
            InitX(x);
            InitZero(mulAxSubb);
            long startTime = System.nanoTime();

            MatrixThreads = Executors.newFixedThreadPool(NumberOfThreads);
            ExecutorService Checker = Executors.newSingleThreadExecutor();

            for(int j=0;j<NumberOfThreads;j++){
                isWait.add(false);
            }
            for (int thread = 0; thread < NumberOfThreads; thread++) {
                MatrixThreads.execute(new MatrixThread(thread));
            }
            Checker.execute(new CheckThread());

            while(AnswerNotFounded){
                //System.out.println("Main Thread is waiting...");
                Thread.sleep(200); //0.2 sec
            }
            System.out.println("End");
            long elapsedNanos = System.nanoTime() - startTime;
            if (elapsedNanos / 1000000000.0 < min_time) {
                min_time = elapsedNanos / 1000000000.0;
            }
            Over=true;
            MatrixThreads.shutdown();
            Checker.shutdown();
            MatrixThreads.awaitTermination(5, TimeUnit.SECONDS);
            Checker.awaitTermination(5, TimeUnit.SECONDS);
        }
        System.out.println(min_time);
    }

    private static class CheckThread implements Runnable{


        CheckThread(){}
        public void run() {
            do {
                if (!flagThreadsWorks) {
                    //System.out.println("Try to notify");

                    for (int i=0;i<NumberOfThreads;i++){
                        isWait.set(i, false);
                    }
                    //System.out.println("Threads notify");
                    flagThreadsWorks = true;
                }
                //System.out.println("Checker waited");
                while (flagThreadsWorks) {
                    for (int thread = 0, count = 0; thread < NumberOfThreads; thread++) {
                        if (isWait.get(thread)) {
                            count++;
                        }
                        if (count == NumberOfThreads) {
                            flagThreadsWorks = false;
                        }
                        if(Over){
                            break;
                        }
                    }
                }
                //System.out.println("Threads done");
                subVectorOnVector(mulAxSubb, b); //N 0.1%
                nextStep(mulAxSubb, x);//2N 0.2%
                checkAnswer(mulAxSubb, b); //2N 0.2%
                //System.out.println(x);
                //System.out.println(AnswerNotFounded);
            } while (AnswerNotFounded && !Over);
        }
    }
    private static class MatrixThread implements Runnable{
        private final int currThread;
        MatrixThread(int currThread){
            this.currThread = currThread;
        }
        public void run() {
            try{
                do{
                    //System.out.println("Thread " + (currThread+1) + " start");
                    mulMatrixOnVectorParallel(A, x, currThread, mulAxSubb); //N^2 99.5%
                    isWait.set(currThread, true);
                    //System.out.println("Thread " + (currThread+1) + " set wait");
                    while(isWait.get(currThread)){
                        Thread.sleep(1);
                        if(Over){
                            break;
                        }
                    }
                }while(AnswerNotFounded && !Over);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    private static void checkAnswer(List<Double> mulAx, List<Double> b) {
        //takenormX
        //takenormB
        // X/B
        double xDouble = takeNorm(mulAx);
        double bDouble = takeNorm(b);
        if (xDouble / bDouble < epsilon) AnswerNotFounded = false;
        else AnswerNotFounded = true;
    }
    private static double takeNorm(List<Double> vector) {
        double sum=0;
        for(int i=0;i<N;i++){
            sum+=vector.get(i);
        }
        return sqrt(abs(sum));
    }
    private static void nextStep(List<Double> mulAxSubb, List<Double> x) {
        //*t
        //x-
        List<Double> tempPer;
        tempPer = mulVectorOnConst(mulAxSubb, t);
        subVectorOnVector(x, tempPer);
    }
    private static List<Double> mulVectorOnConst(List<Double> x, double t) {
        List<Double> new_X = new ArrayList<>(N);
        for(int row=0;row<N;row++){
            new_X.add(row,x.get(row)*t);
        }
        return new_X;
    }
    private static void subVectorOnVector(List<Double> x, List<Double> b) {
        for(int row=0;row<N;row++){
            x.set(row,x.get(row)-b.get(row));
        }
    }
    private static void mulMatrixOnVectorParallel(List<Double> A, List<Double> x,int currenThread,List<Double> output) {

        //long startTime = System.nanoTime();
        int currentRow = currenThread*N/NumberOfThreads;
        for (int i = 0; i < (N / NumberOfThreads); currentRow++, i++) {
            double sumOfElements = 0;
            for (int column = 0; column < N; column++) {
                sumOfElements += A.get(currentRow * N + column);
            }
            sumOfElements *= x.get(currentRow);
            output.set(currentRow, sumOfElements);
        }
        //long elapsedNanos = System.nanoTime() - startTime;
        //System.out.println(currenThread + " time " + elapsedNanos);
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
    public static void InitZero(List<Double> x) {
        for(int i=0;i<N;i++){
            x.add((double) 0);
        }
    }
}


