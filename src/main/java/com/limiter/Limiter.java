package com.limiter;

public interface Limiter {

    int TIME_IN_NANO_SECOND = 1_000_000_000;

    void acquire();

}
