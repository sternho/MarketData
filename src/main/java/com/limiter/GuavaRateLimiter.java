package com.limiter;

import com.google.common.util.concurrent.RateLimiter;

public class GuavaRateLimiter implements Limiter {

    private RateLimiter rateLimiter;

    public GuavaRateLimiter(int seq) {
        rateLimiter = RateLimiter.create(seq);
    }

    @Override
    public void acquire() {
        rateLimiter.acquire();
    }

}
