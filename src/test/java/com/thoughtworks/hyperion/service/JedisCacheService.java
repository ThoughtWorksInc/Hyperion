package com.thoughtworks.hyperion.service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.function.Function;

public class JedisCacheService implements CacheService {

    @Autowired
    private JedisPool jedisPool;

    private void writeCache(Function<Jedis, Void> function) {
        Jedis jedis = jedisPool.getResource();
        function.apply(jedis);
        jedis.close();
    }

    private <T> T readCache(Function<Jedis, T> function) {
        Jedis jedis = jedisPool.getResource();
        T result = function.apply(jedis);
        jedis.close();

        return result;
    }

    @Override
    public void cache(byte[] key, byte[] value, Integer expireMinutes) {
        writeCache(jedis -> {
            jedis.set(key, value);
            if (expireMinutes != null) {
                jedis.expire(key, expireMinutes * 60);
            }
            return null;
        });
    }

    @Override
    public byte[] getCache(byte[] key) {
        return readCache(jedis -> {
            byte[] bytes = jedis.get(key);

            if (bytes == null || bytes.length == 0) {
                return null;
            }
            return bytes;
        });
    }

    @Override
    public void clearWithKey(byte[]... key) {
        writeCache(jedis -> {
            jedis.del(key);
            return null;
        });
    }

    @Override
    public Set<byte[]> getKeys(byte[] keyPrefix) {
        return readCache(jedis -> jedis.keys(keyPrefix));
    }
}
