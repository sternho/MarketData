package com.market.data;

import com.market.data.dto.MarketData;
import com.market.data.processor.AbstractMarketDataProcessor;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.limiter.Limiter.TIME_IN_NANO_SECOND;

public class AbstractMarketDataProcessorTest {

    @Test
    public void test_message_order() throws InterruptedException {
        List<MarketData> result = new ArrayList<>();
        AbstractMarketDataProcessor processor = new AbstractMarketDataProcessor() {
            @Override
            public void publishAggregatedMarketData(MarketData data) {
                result.add(data);
            }
        };
        processor.onMessage(MarketData.builder().symbol("stk1").ask(1.1f).bid(2.1f).updatedAt(LocalDateTime.now()).build());
        processor.onMessage(MarketData.builder().symbol("stk2").ask(2.1f).bid(3.1f).updatedAt(LocalDateTime.now()).build());
        processor.onMessage(MarketData.builder().symbol("stk3").ask(3.1f).bid(4.1f).updatedAt(LocalDateTime.now()).build());
        processor.onMessage(MarketData.builder().symbol("stk1").ask(1.1f).bid(2.1f).updatedAt(LocalDateTime.now()).build());

        Thread.sleep(1_000);
        Assert.assertEquals("stk1", result.get(0).getSymbol());
        Assert.assertEquals(1.1f, result.get(0).getAsk().floatValue(), 0.0f);
        Assert.assertEquals(2.1f, result.get(0).getBid().floatValue(), 0.0f);
        Assert.assertEquals("stk2", result.get(1).getSymbol());
        Assert.assertEquals("stk3", result.get(2).getSymbol());
    }

    @Test
    public void test_latest_price() throws InterruptedException {
        List<MarketData> result = new ArrayList<>();
        AbstractMarketDataProcessor processor = new AbstractMarketDataProcessor(10) {
            @Override
            public void publishAggregatedMarketData(MarketData data) {
                result.add(data);
            }
        };
        for(int i=0; i<=9; i++) {
            processor.onMessage(MarketData.builder()
                    .symbol(i+"stk0").ask(0.0f).bid(0.0f)
                    .updatedAt(LocalDateTime.now())
                    .build());
        }
        processor.onMessage(MarketData.builder().symbol("stk1").ask(1.1f).bid(1.1f).updatedAt(LocalDateTime.now().plusSeconds(2)).build());
        processor.onMessage(MarketData.builder().symbol("stk1").ask(2.1f).bid(2.2f).updatedAt(LocalDateTime.now().plusSeconds(2)).build());

        TimeUnit.SECONDS.sleep(2);
        Assert.assertEquals("stk1", result.get(10).getSymbol());
        Assert.assertEquals(2.1f, result.get(10).getAsk().floatValue(), 0.0f);
        Assert.assertEquals(2.2f, result.get(10).getBid().floatValue(), 0.0f);
    }

    @Test
    public void test_process_100_per_second() {
        AtomicInteger resultCount = new AtomicInteger(0);
        AbstractMarketDataProcessor processor = new AbstractMarketDataProcessor() {
            @Override
            public void publishAggregatedMarketData(MarketData data) {
                resultCount.getAndIncrement();
            }
        };
        long beforeLimit = System.nanoTime();
        for(int i=0; i<101; i++) {
            processor.onMessage(MarketData.builder()
                    .symbol("stk"+i).ask(0.0f).bid(0.0f)
                    .updatedAt(LocalDateTime.now())
                    .build());
        }
        while(true) {
            if(resultCount.get()==101) {
                break;
            }
        }
        long timeSpent = System.nanoTime()-beforeLimit;
        Assert.assertTrue(timeSpent>TIME_IN_NANO_SECOND);
    }

}
