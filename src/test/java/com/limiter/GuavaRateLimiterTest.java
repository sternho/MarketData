package com.limiter;

import org.junit.Assert;
import org.junit.Test;

import static com.limiter.Limiter.TIME_IN_NANO_SECOND;

public class GuavaRateLimiterTest {

    @Test
    public void testAcquire_withinLimit() {
        Limiter limiter = new GuavaRateLimiter(10);
        long beforeLimit = System.nanoTime();
        for(int i=0; i<10; i++) {
            limiter.acquire();
        }
        long timeSpent = System.nanoTime()-beforeLimit;
        Assert.assertTrue(timeSpent<TIME_IN_NANO_SECOND);
    }

    @Test
    public void testAcquire_overLimit() {
        Limiter limiter = new GuavaRateLimiter(10);
        long beforeLimit = System.nanoTime();
        for(int i=0; i<11; i++) {
            limiter.acquire();
        }
        long timeSpent = System.nanoTime()-beforeLimit;
        Assert.assertTrue(timeSpent>TIME_IN_NANO_SECOND);
    }

}
