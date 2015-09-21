package com.thoughtworks.hyperion.utils;

import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;

public class SerializeUtil {
    private static final Logger log = Logger.getLogger(SerializeUtil.class);

    private static final String ENCODING = "utf-8";

    public static byte[] objToBytes(Object value) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(value);
        oos.flush();
        byte[] bytes = baos.toByteArray();
        oos.close();
        baos.close();
        return bytes;
    }

    public static Object bytesToObj(byte[] bytes, Class<?> type) throws IOException, ClassNotFoundException {
        if (type == String.class) {
            return new String(bytes, ENCODING);
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object obj = ois.readObject();
        ois.close();
        bais.close();
        return obj;
    }

    public static byte[] stringToBytes(String str) {
        try {
            return str.getBytes(ENCODING);
        } catch (UnsupportedEncodingException e) {
            log.fatal("Failed to encode key string, unsupported encoding: " + ENCODING, e);
            throw new RuntimeException(e);
        }
    }
}
