package com.example.tagmod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TagTimerManager {

    public static final Map<UUID, Long> survivalTimes = new HashMap<>();

    public static void start(UUID id) {
        survivalTimes.put(id, System.currentTimeMillis());
    }

    public static long getSurvival(UUID id) {

        if (!survivalTimes.containsKey(id)) return 0;

        return (System.currentTimeMillis() - survivalTimes.get(id)) / 1000;
    }

    public static void clear() {
        survivalTimes.clear();
    }
}