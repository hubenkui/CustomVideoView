package com.example.videoview.customevideoview;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Session {

    private Map<String, Object> mAttributes = new ConcurrentHashMap<String, Object>();

    public static Session getSessionInstance() {

        return SingleInstanceHolder.instance;
    }

    private static class SingleInstanceHolder {

        /**
         * 静态初始化器，由JVM来保证线程安全
         */
        private static final Session instance = new Session();

    }

    public Object getAttribute(String key) {
        if (!mAttributes.containsKey(key)) {
            return null;
        }
        return this.mAttributes.get(key);
    }

    public void putAttribute(String key, Object paramObject) {
        if (paramObject == null) {
            return;
        }
        this.mAttributes.put(key, paramObject);
    }

    public Object removeAttribute(String key) {
        if (!this.mAttributes.containsKey(key)) {
            return null;
        }
        return this.mAttributes.remove(key);
    }

    public boolean hasAttribute(String key) {
        return this.mAttributes.containsKey(key);
    }
}