package pl.kielce.tu.backend.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;

public class CacheConfigTest {

    @Test
    void cacheManagerBeanIsConfigured() {
        CacheConfig cfg = new CacheConfig();
        CacheManager cm = cfg.cacheManager();

        assertNotNull(cm, "CacheManager should not be null");
        assertTrue(cm.getCacheNames().contains("posterCache"), "cache names should contain 'posterCache'");

        assertNotNull(cm.getCache("posterCache"), "'posterCache' should be available from CacheManager");
    }

    @Test
    void posterCacheEvictsWhenExceedingMaximumSize() {
        CacheConfig cfg = new CacheConfig();
        CacheManager cm = cfg.cacheManager();

        org.springframework.cache.Cache springCache = cm.getCache("posterCache");
        assertNotNull(springCache, "'posterCache' should be available");

        assertTrue(springCache instanceof CaffeineCache, "underlying cache should be a CaffeineCache");
        CaffeineCache caffeineCache = (CaffeineCache) springCache;

        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = (com.github.benmanes.caffeine.cache.Cache<Object, Object>) caffeineCache
                .getNativeCache();
        assertNotNull(nativeCache, "native Caffeine cache should not be null");

        int inserts = 1500;
        for (int i = 0; i < inserts; i++) {
            nativeCache.put("key-" + i, "value-" + i);
        }

        long size = nativeCache.estimatedSize();
        assertTrue(size <= inserts, "estimated size should not exceed number of inserts");
        nativeCache.cleanUp();
        size = nativeCache.estimatedSize();
        assertTrue(size <= 1000, "cache should evict entries and remain near the configured maximum size (1000)");
    }
}
