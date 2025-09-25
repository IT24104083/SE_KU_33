package com.example.test002.config;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import java.util.HashMap;
import java.util.Map;

@Component
@SessionScope
public class SessionManager {
    private Map<String, Object> sessionData = new HashMap<>();

    public void setAttribute(String key, Object value) {
        sessionData.put(key, value);
    }

    public Object getAttribute(String key) {
        return sessionData.get(key);
    }

    public void removeAttribute(String key) {
        sessionData.remove(key);
    }

    public void invalidate() {
        sessionData.clear();
    }
}