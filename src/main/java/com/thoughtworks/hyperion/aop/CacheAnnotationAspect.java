package com.thoughtworks.hyperion.aop;


import static com.thoughtworks.hyperion.aop.CacheAopUtils.checkIfCacheDisabled;
import static com.thoughtworks.hyperion.aop.CacheAopUtils.convertSubKeys;
import static com.thoughtworks.hyperion.aop.CacheAopUtils.extractParameterKeys;
import static com.thoughtworks.hyperion.aop.CacheAopUtils.getScopeKeyPrefix;


import com.thoughtworks.hyperion.annotation.Cached;
import com.thoughtworks.hyperion.enumeration.CacheScope;
import com.thoughtworks.hyperion.service.CacheService;
import com.thoughtworks.hyperion.utils.SerializeUtil;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

@Aspect
public class CacheAnnotationAspect {
    private static final Logger LOG = Logger.getLogger(CacheAnnotationAspect.class);

    @Autowired
    private CacheService cacheService;

    @Around("@annotation(com.thoughtworks.hyperion.annotation.Cached)")
    public Object cachedMethodAroundAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();

        CacheConfig cacheConfig = extractCacheConfig(proceedingJoinPoint);
        byte[] key = createKey(cacheConfig);

        if (!cacheConfig.isCachedDisabled()) {
            Object cached = getCacheObject(key, method.getReturnType());

            if (cached != null) {
                return cached;
            }
        }

        final Object value;
        try {
            value = proceedingJoinPoint.proceed();
        } catch (Throwable t) {
            LOG.warn("Failed to execute cached method: " + proceedingJoinPoint.getSignature(), t);
            throw t;
        }

        if (!cacheConfig.isCachedDisabled()) {
            cacheObject(key, value, cacheConfig.getExpireMinutes());
        }

        return value;
    }

    private void cacheObject(byte[] key, Object value, Integer expireMinutes) {
        try {
            cacheService.cache(key, SerializeUtil.objToBytes(value), expireMinutes);
        } catch (IOException e) {
            LOG.warn("Failed to serialize cached object: " + value, e);
        }
    }

    private Object getCacheObject(byte[] key, Class<?> type) {
        try {
            byte[] result = cacheService.getCache(key);
            return result == null ? result : SerializeUtil.bytesToObj(result, type);
        } catch (IOException e) {
            LOG.warn("Failed to deserialize cached object: " + type, e);
            return null;
        } catch (ClassNotFoundException e) {
            LOG.warn("Failed to deserialize cached object: " + type, e);
            return null;
        }
    }

    private CacheConfig extractCacheConfig(ProceedingJoinPoint proceedingJoinPoint) {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();

        Cached cachedAnnotation = method.getAnnotation(Cached.class);
        CacheConfig cacheConfig = new CacheConfig(method, cachedAnnotation.scope(), cachedAnnotation.key());

        cacheConfig.setExpireMinutes(cachedAnnotation.expireMinutes());

        Object[] arguments = proceedingJoinPoint.getArgs();
        cacheConfig.setSubKeys(extractParameterKeys(method, arguments));

        cacheConfig.setCachedDisabled(checkIfCacheDisabled(method, arguments));

        return cacheConfig;
    }

    private byte[] createKey(CacheConfig cacheConfig) {
        StringBuilder sb = new StringBuilder();
        sb.append(getScopeKeyPrefix(cacheConfig.getScope(), cacheConfig.getMethod()));
        sb.append(cacheConfig.getKey());
        sb.append(convertSubKeys(cacheConfig.getSubKeys()));
        return SerializeUtil.stringToBytes(sb.toString());
    }

    private static class CacheConfig {
        private Method method;
        private CacheScope scope;
        private String key;
        private Integer expireMinutes;
        private List<String> subKeys;
        private boolean cachedDisabled;

        public CacheConfig(Method method, CacheScope scope, String key) {
            this.method = method;
            this.scope = scope;
            this.key = key;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public CacheScope getScope() {
            return scope;
        }

        public void setScope(CacheScope scope) {
            this.scope = scope;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Integer getExpireMinutes() {
            return expireMinutes;
        }

        public void setExpireMinutes(Integer expireMinutes) {
            this.expireMinutes = expireMinutes;
        }

        public List<String> getSubKeys() {
            return subKeys;
        }

        public void setSubKeys(List<String> subKeys) {
            this.subKeys = subKeys;
        }

        public boolean isCachedDisabled() {
            return cachedDisabled;
        }

        public void setCachedDisabled(boolean cachedDisabled) {
            this.cachedDisabled = cachedDisabled;
        }
    }
}
