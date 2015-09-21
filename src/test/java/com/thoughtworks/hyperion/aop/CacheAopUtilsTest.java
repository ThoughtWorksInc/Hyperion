package com.thoughtworks.hyperion.aop;

import static org.junit.Assert.assertEquals;


import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheAopUtilsTest {

    @Before
    public void setUp() {
    }

    @Test
    public void testExtractParameterKeys() throws Exception {
        Method method = this.getClass().getMethod("setUp");
        Object[] parameters = {"113123", 20};
        List<String> keys = new ArrayList<>();
        keys.add("0-113123");
        keys.add("1-20");
        List<String> results = CacheAopUtils.extractParameterKeys(method, parameters);

        assertEquals(2, results.size());
        for (int i=0; i < results.size(); i++) {
            assertEquals(keys.get(i), results.get(i));
        }
    }

    @Test
    public void testConvertSubKeyToList() throws Exception {
        Map<Integer, Object> keys = new HashMap<Integer, Object>() {{
            put(0, "23");
            put(14, "aaa");
            put(1, 40);
        }};
        List<String> results = CacheAopUtils.convertSubKeyToList(keys);

        assertEquals(3, results.size());
        assertEquals("0-23", results.get(0));
        assertEquals("1-40", results.get(1));
        assertEquals("14-aaa", results.get(2));
    }
}