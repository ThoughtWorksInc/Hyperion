package com.thoughtworks.hyperion.aop;

import com.thoughtworks.hyperion.annotation.CacheKey;
import com.thoughtworks.hyperion.annotation.NoCacheIf;
import com.thoughtworks.hyperion.annotation.NoCacheKey;
import com.thoughtworks.hyperion.enumeration.CacheScope;
import com.thoughtworks.hyperion.enumeration.NoCacheCondition;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CacheAopUtils {
    private static final Logger log = Logger.getLogger(CacheAopUtils.class);

    private static final String nullStr = "nil";

    static String getScopeKeyPrefix(CacheScope scope, Method method) {
        switch (scope) {
            case CLASS: {
                return scope.name() + ":" + method.getDeclaringClass().getName() + ":";
            }
            case GLOBAL: {
                return scope.name() + ":";
            }
            default: {
                log.fatal("Unsupported cache scope type: " + scope);
                throw new RuntimeException("Unsupported cache scope type: " + scope);
            }
        }
    }

    static List<String> extractParameterKeys(Method method, Object[] arguments) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Map<Integer, Object> subKeys = new HashMap<>();
        for (int i = 0; i < arguments.length; i++) {
            Object argument = arguments[i];
            if (argument == null) {
                subKeys.put(i, nullStr);
            } else {
                subKeys.put(i, argument);
            }
        }
        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation.annotationType().equals(NoCacheKey.class)) {
                    subKeys.remove(i);
                } else if (annotation.annotationType().equals(CacheKey.class)) {
                    CacheKey cacheKey = CacheKey.class.cast(annotation);
                    Object argument = arguments[i];
                    if (argument == null) {
                        continue;
                    }
                    if (cacheKey.field() == null || cacheKey.field().isEmpty()) {
                        subKeys.put(i, argument.toString());
                    } else {
                        String fieldValue;
                        try {
                            fieldValue = BeanUtils.getProperty(argument, cacheKey.field());
                        } catch (Exception e) {
                            log.error("Failed to get field value of cache key: " + cacheKey, e);
                            throw new RuntimeException("Failed to get field value of cache key: " + cacheKey);
                        }
                        subKeys.put(i, fieldValue);
                    }
                }
            }
        }
        return convertSubKeyToList(subKeys);
    }

    static int compare(String string1, String sting2) {
        return Integer.parseInt(string1.substring(0, string1.indexOf("-")))
            - Integer.parseInt(sting2.substring(0, sting2.indexOf("-")));
    }

    static List<String> convertSubKeyToList(Map<Integer, Object> subKeys) {
        List<String> result = subKeys.entrySet()
            .stream()
            .map(input -> input.getKey() + "-" + input.getValue())
            .collect(Collectors.toList());
        Collections.sort(result, (string1, sting2) -> compare(string1, sting2));

        return result;
    }

    static String convertSubKeys(List<String> subKeys) {
        StringBuffer sb = new StringBuffer();
        for (String subKey : subKeys) {
            sb.append(":");
            sb.append(subKey);
        }
        return sb.toString();
    }

    static boolean checkIfCacheDisabled(Method method, Object[] arguments) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation.annotationType().equals(NoCacheIf.class)) {
                    NoCacheIf noCacheIf = NoCacheIf.class.cast(annotation);
                    Object argument = arguments[i];
                    NoCacheCondition condition = noCacheIf.condition();
                    switch (condition) {
                        case NOT_EMPTY_STRING: {
                            if (argument != null) {
                                if (String.class.isInstance(argument)) {
                                    String value = String.class.cast(argument);
                                    if (!(value == null || value.isEmpty())) {
                                        return true;
                                    }
                                }
                            }
                            break;
                        }
                        case NOT_EMPTY_MAP: {
                            if (argument != null) {
                                if (Map.class.isInstance(argument)) {
                                    Map value = Map.class.cast(argument);
                                    if (!value.isEmpty()) {
                                        return true;
                                    }
                                }
                            }
                            break;
                        }
                        case NOT_EMPTY_ITERATOR: {
                            if (argument != null) {
                                if (Iterable.class.isInstance(argument)) {
                                    Iterable value = Iterable.class.cast(argument);
                                    if (value.iterator().hasNext()) {
                                        return true;
                                    }
                                }
                            }
                        }
                        default: {
                            throw new RuntimeException("Unsupported cache condition type: " + condition);
                        }
                    }
                }
            }
        }
        return false;
    }
}
