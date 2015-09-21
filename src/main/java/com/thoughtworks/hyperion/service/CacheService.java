package com.thoughtworks.hyperion.service;

import java.util.Set;

public interface CacheService {
    void cache(byte[] key, byte[] value, Integer expireMinutes);

    byte[] getCache(byte[] key);

    void clearWithKey(byte[]... key);

    Set<byte[]> getKeys(byte[] scopeKeyPrefix);
}
