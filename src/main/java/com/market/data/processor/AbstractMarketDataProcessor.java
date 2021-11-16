package com.market.data.processor;

import com.limiter.Limiter;
import com.limiter.SlidingWindow;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.market.data.dto.MarketData;
import com.market.data.dto.ValueEvent;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractMarketDataProcessor {
    private static final int DEFAULT_THROUGH_PUT = 100;
    private static final int DEFAULT_BUFFER_SIZE = 256;

    private final Map<String, MarketData> marketDataMap;
    private final Map<String, LocalDateTime> sentTime;
    private final Map<String, LocalDateTime> sentDataUpdatedTime;

    private final Limiter rateLimiter;
    private final RingBuffer<ValueEvent> ringBuffer;

    public AbstractMarketDataProcessor() {
        this(DEFAULT_THROUGH_PUT);
    }

    public AbstractMarketDataProcessor(int throughput) {
        marketDataMap = new ConcurrentHashMap<>();

        sentTime = new ConcurrentHashMap<>();
        sentDataUpdatedTime = new ConcurrentHashMap<>();

        rateLimiter = new SlidingWindow(throughput);
        Disruptor<ValueEvent> disruptor = new Disruptor<>(
                ValueEvent::new,DEFAULT_BUFFER_SIZE,
                DaemonThreadFactory.INSTANCE, ProducerType.SINGLE,
                new BusySpinWaitStrategy());
        disruptor.handleEventsWith(getEventHandler());
        ringBuffer = disruptor.start();
    }

    public EventHandler<ValueEvent>[] getEventHandler() {
        EventHandler<ValueEvent> eventHandler = (event, sequence, endOfBatch)
                -> processMessage(event.getValue());
        return new EventHandler[] { eventHandler };
    }

    /*
     * Receive data and pushed into the queue.
     */
    public void onMessage(MarketData data) {
        this.marketDataMap.put(data.getSymbol(), data);

        long sequenceId = ringBuffer.next();
        ValueEvent valueEvent = ringBuffer.get(sequenceId);
        valueEvent.setValue(data.getSymbol());
        ringBuffer.publish(sequenceId);
    }

    /*
     * consumer, process the logic and update necessary data structure
     */
    public void processMessage(String symbol) {
        MarketData marketData = marketDataMap.get(symbol);
        LocalDateTime now = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(System.currentTimeMillis()),
                TimeZone.getDefault().toZoneId());
        if(isSymbolSentWithinThreshold(marketData, now)) {
            rateLimiter.acquire();
            publishAggregatedMarketData(marketData);
            sentDataUpdatedTime.put(symbol, marketData.getUpdatedAt());
            sentTime.put(symbol, now);
        }
    }

    /*
     * Check market has been sent within 1 second.
     * And check the market date didn't send before.
     */
    public boolean isSymbolSentWithinThreshold(MarketData marketData, LocalDateTime now) {
        LocalDateTime lastSendTime = this.sentTime.get(marketData.getSymbol());
        LocalDateTime updatedTime = sentDataUpdatedTime.get(marketData.getSymbol());
        return (lastSendTime==null || Duration.between(lastSendTime, now).getSeconds()>1) &&
                (updatedTime==null || !updatedTime.equals(marketData.getUpdatedAt()));
    }

    /*
     * public aggregated and throttled market data.
     * assume implemented by the subscriptor
     *
     */
    public abstract void publishAggregatedMarketData(MarketData data);
}
