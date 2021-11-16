package com.limiter;

import java.util.concurrent.TimeUnit;

public class SlidingWindow implements Limiter {

    private long[] executeArray;
    private int seq;
    private int curIndex;

    public SlidingWindow(int seq) {
        this.seq = seq;
        executeArray = new long[seq];
        curIndex = 0;
    }

    @Override
    public synchronized void acquire() {
        long lastTime = executeArray[curIndex];
        if(lastTime!=0) {
            long sleep = TIME_IN_NANO_SECOND-(System.nanoTime()-lastTime);
            if(sleep>0) {
                try {
                    TimeUnit.NANOSECONDS.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        executeArray[curIndex] = System.nanoTime();
        if(++curIndex>=seq) {
            curIndex = 0;
        }
    }
}
