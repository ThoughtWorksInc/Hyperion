package com.thoughtworks.hyperion.aop;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import com.thoughtworks.hyperion.annotation.Cached;
import com.thoughtworks.hyperion.service.CacheService;
import com.thoughtworks.hyperion.utils.SerializeUtil;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;

public class CacheAnnotationAspectTest {

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private CacheAnnotationAspect annotationAspect;

    @Cached(key = "test")
    public static String getValue(String key) {
        return "value";
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void should_success_call_get_cache_if_has_cached_annotation() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature methodSignature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new String[]{"abc"});
        Method method = getClass().getMethod("getValue", String.class);
        when(methodSignature.getMethod()).thenReturn(method);

        when(cacheService.getCache(any())).thenReturn(SerializeUtil.stringToBytes("test"));

        Object result = annotationAspect.cachedMethodAroundAdvice(joinPoint);

        assertEquals(result, "test");
    }

    @Test
    public void should_invoke_origin_method_if_not_get_cache() throws Throwable {

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature methodSignature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new String[]{"abc"});
        when(joinPoint.proceed()).thenReturn("value");
        Method method = getClass().getMethod("getValue", String.class);
        when(methodSignature.getMethod()).thenReturn(method);

        when(cacheService.getCache(any())).thenReturn(null);

        Object result = annotationAspect.cachedMethodAroundAdvice(joinPoint);

        assertEquals(result, "value");
        verify(cacheService).cache(any(), any(), anyInt());
    }
}