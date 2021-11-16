package com.limiter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TokenBucket implements Limiter {

    private int seq;
    private AtomicInteger token;
    private Thread thread;

    public TokenBucket(int seq) {
        this.seq = seq;
        this.token = new AtomicInteger(seq);
        thread = new Thread(() -> {
            try {
                while(token.get() < seq) {
                    TimeUnit.SECONDS.sleep(1);
                    while(token.get() < seq) {
                        TimeUnit.MILLISECONDS.sleep(1_000 / seq);
                        token.getAndIncrement();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    @Override
    public synchronized void acquire() {
        while(token.get()<1) {
            try {
                TimeUnit.MILLISECONDS.sleep(1_000/seq);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        token.getAndDecrement();
    }

}
