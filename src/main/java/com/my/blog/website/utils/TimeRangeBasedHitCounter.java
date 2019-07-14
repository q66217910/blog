package com.my.blog.website.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @Author: Vince
 */
public class TimeRangeBasedHitCounter<T> {

    Queue<TimeRangedBased> timeRangedQueue = new ConcurrentLinkedQueue<>();

    volatile Map<T, Integer> countCached = new HashMap<>();

    final private Object sync = new Object();

    private static final long DEFAULT_FLUSH_ON_TIME_MILLIS = 60_000L;
    private static final int DEFAULT_FLUSH_THRESHOLD = 2500;

    private long flushOnTimeMillis = DEFAULT_FLUSH_ON_TIME_MILLIS;
    private int flushThreshold = DEFAULT_FLUSH_THRESHOLD;

    public void addCount(T t) {
        TimeRangedBased timeRangedBased = new TimeRangedBased();
        timeRangedBased.t = t;
        timeRangedBased.timestamp = System.currentTimeMillis();
        timeRangedQueue.add(timeRangedBased);

        if (timeRangedQueue.size() > 2 * flushThreshold) {
            flushCount();
        }
    }

    public void flushCount() {
        long currentTimeMillis = System.currentTimeMillis();
        Map<T, Integer> count = new HashMap<>();
        Map<T, Long> time = new HashMap<>();

        synchronized (sync) {
            while (true) {
                TimeRangedBased peek = timeRangedQueue.peek();
                if (peek == null) break;

                if (!(peek.timestamp < currentTimeMillis - flushOnTimeMillis || timeRangedQueue.size() > flushThreshold)) {
                    break;
                }

                timeRangedQueue.poll();
                Long aLong = time.get(peek.t);
                if (aLong == null) {
                    count.put(peek.t, 1);
                    time.put(peek.t, peek.timestamp);
                } else {
                    if (peek.timestamp < aLong + flushOnTimeMillis) {
                        continue;
                    }
                    Integer c = count.get(peek.t);
                    count.put(peek.t, (c != null ? c : 0) + 1);
                    time.put(peek.t, peek.timestamp);
                }
            }

            count.forEach((key, value) -> {
                Integer c = countCached.get(key);
                if (c == null) {
                    c = 0;
                }
                countCached.put(key, c + value);
            });

        }
    }

    public Map<T, Integer> getAndCleanCountCached() {
        synchronized (sync) {
            Map<T, Integer> cached = this.countCached;
            this.countCached = new HashMap<>();
            return cached;
        }
    }

    public long getFlushOnTimeMillis() {
        return flushOnTimeMillis;
    }

    public void setFlushOnTimeMillis(long flushOnTimeMillis) {
        this.flushOnTimeMillis = flushOnTimeMillis < 20 ? 20 : flushOnTimeMillis;
    }

    public int getFlushThreshold() {
        return flushThreshold;
    }

    public void setFlushThreshold(int flushThreshold) {
        this.flushThreshold = flushThreshold < 100 ? 100 : flushThreshold;
    }

    public class TimeRangedBased {
        T t;
        long timestamp;
    }
}
