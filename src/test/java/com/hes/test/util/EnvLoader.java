package com.hes.test.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight env loader for tests.
 * Reads key=value pairs from a project-root file named `test.env` (if present)
 * and exposes a get(key, default) method. Priority order:
 * 1) System.getenv(), 2) System.getProperty(), 3) test.env values, 4) default
 */
public class EnvLoader {
    private static final Map<String, String> OVERRIDES = new HashMap<>();

    static {
        Path p = Paths.get("test.env");
        if (Files.exists(p)) {
            try {
                List<String> lines = Files.readAllLines(p);
                for (String line : lines) {
                    String l = line.trim();
                    if (l.isEmpty() || l.startsWith("#")) continue;
                    int idx = l.indexOf('=');
                    if (idx <= 0) continue;
                    String k = l.substring(0, idx).trim();
                    String v = l.substring(idx + 1).trim();
                    OVERRIDES.put(k, v);
                }
            } catch (IOException ignored) {
                // swallow — tests will fallback to defaults
            }
        }
    }

    public static String get(String key, String defaultVal) {
        String v = System.getenv(key);
        if (v != null && !v.isEmpty()) return v;
        v = System.getProperty(key);
        if (v != null && !v.isEmpty()) return v;
        v = OVERRIDES.get(key);
        if (v != null && !v.isEmpty()) return v;
        return defaultVal;
    }
}
