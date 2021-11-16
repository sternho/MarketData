package com.market.data;

import com.market.data.dto.MarketData;
import com.market.data.processor.AbstractMarketDataProcessor;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        long timer = System.currentTimeMillis();
        AbstractMarketDataProcessor processor = new AbstractMarketDataProcessor() {
            @Override
            public void publishAggregatedMarketData(MarketData data) {
                System.out.println(data.getSymbol()+" : "+data.getAsk());
            }
        };

        for(int i=0; i<=100; i++) {
            processor.onMessage(MarketData.builder()
                    .symbol("stk"+i)
                    .ask(1.0f)
                    .updatedAt(LocalDateTime.now())
                    .build()
            );
        }
        System.out.println((System.currentTimeMillis()-timer) + "ms toke");
        TimeUnit.SECONDS.sleep(2);
        System.out.println((System.currentTimeMillis()-timer) + "ms toke");
    }

}
