package com.sistema_de_inventarios_v02.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class TokenBlacklistService {

    private final ConcurrentMap<String, Boolean> blacklistedTokens = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> activeTokensByUser = new ConcurrentHashMap<>();

    public void addToBlacklist(String token) {
        blacklistedTokens.put(token, true);
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }

    public void setActiveTokenForUser(String username, String token) {
        String oldToken = activeTokensByUser.get(username);
        if (oldToken != null) {
            addToBlacklist(oldToken);
        }
        activeTokensByUser.put(username, token);
    }

    public void removeActiveTokenForUser(String username) {
        String token = activeTokensByUser.remove(username);
        if (token != null) {
            addToBlacklist(token);
        }
    }

    public String getActiveTokenForUser(String username) {
        return activeTokensByUser.get(username);
    }
}
