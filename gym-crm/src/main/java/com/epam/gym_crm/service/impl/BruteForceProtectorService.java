package com.epam.gym_crm.service.impl;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BruteForceProtectorService {

    private static final int MAX_ATTEMPT = 3;
    private static final long BLOCK_TIME = 5 * 60 * 1000; // 5 minutes

    private final Map<String, Integer> attempts = new ConcurrentHashMap<>();
    private final Map<String, Long> blockTime = new ConcurrentHashMap<>();

    public void loginSucceeded(String username) {
        attempts.remove(username);
        blockTime.remove(username);
    }

    public void loginFailed(String username) {
        int currentAttempts = attempts.getOrDefault(username, 0);
        currentAttempts++;
        attempts.put(username, currentAttempts);

        if (currentAttempts >= MAX_ATTEMPT) {
            blockTime.put(username, System.currentTimeMillis() + BLOCK_TIME);
        }
    }

    public boolean isBlocked(String username) {
        Long blockedUntil = blockTime.get(username);
        if (blockedUntil == null) {
            return false;
        }

        if (System.currentTimeMillis() > blockedUntil) {
            // Unblock after 5 minute
            attempts.remove(username);
            blockTime.remove(username);
            return false;
        }

        return true;
    }
}
