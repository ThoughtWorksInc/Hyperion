package com.thoughtworks.hyperion.aop;

import static com.thoughtworks.hyperion.aop.CacheAopUtils.convertSubKeys;
import static com.thoughtworks.hyperion.aop.CacheAopUtils.extractParameterKeys;
import static com.thoughtworks.hyperion.aop.CacheAopUtils.getScopeKeyPrefix;


import com.thoughtworks.hyperion.annotation.ClearCached;
import com.thoughtworks.hyperion.enumeration.CacheScope;
import com.thoughtworks.hyperion.service.CacheService;
import com.thoughtworks.hyperion.utils.SerializeUtil;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.util.Set;

@Aspect
public class ClearCacheAnnotationAspect {

    @Autowired
    private CacheService cacheService;

    @AfterReturning("@annotation(com.thoughtworks.hyperion.annotation.ClearCached)")
    public void clearCachedMethodAfterReturningAdvice(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        ClearCached annotation = method.getAnnotation(ClearCached.class);

        CacheScope scope = annotation.scope();
        String scopeKeyPrefix = getScopeKeyPrefix(scope, method);

        String[] keys = annotation.keys();
        if (keys == null || keys.length == 0) {
            clearWithKeyPrefix(scopeKeyPrefix);
        } else {
            String subKeys = convertSubKeys(extractParameterKeys(method, joinPoint.getArgs()));
            for (String key : keys) {
                cacheService.clearWithKey(SerializeUtil.stringToBytes(scopeKeyPrefix + key + subKeys));
            }
        }
    }

    private void clearWithKeyPrefix(final String keyPrefix) {
        Set<byte[]> keys = cacheService.getKeys(SerializeUtil.stringToBytes(keyPrefix + "*"));
        if (!keys.isEmpty()) {
            cacheService.clearWithKey(keys.toArray(new byte[][]{}));
        }
    }

}
