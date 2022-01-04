package com.transcodegroup.jtt1078.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by matrixy on 2019/4/10.
 */
public final class Session {
    Map<String, Object> attributes;
    /**
     * 下发流水号
     */
    private int serial;

    /**
     * 第一个数据包的时间戳
     */
    private long firstTime;

    public Session() {
        this.attributes = new HashMap<>();
        this.serial = 0;
        this.firstTime = 0;
    }

    public int getSerial() {
        this.serial += 1;
        return this.serial % 65535;
    }

    public void setSerial(int serial) {
        this.serial = serial;
    }

    public long getFirstTime() {
        if (firstTime == 0) {
            firstTime = System.currentTimeMillis();
        }
        return firstTime;
    }

    public void setFirstTime(long firstTime) {
        this.firstTime = firstTime;
    }

    public Session set(String key, Object value) {
        this.attributes.put(key, value);
        return this;
    }

    public boolean has(String key) {
        return this.attributes.containsKey(key);
    }

    public Set<String> keys() {
        return this.attributes.keySet();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T)this.attributes.get(key);
    }

}
