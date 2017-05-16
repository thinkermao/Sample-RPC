package net.hashcoding.samplerpc.common.entity;

/**
 * Created by MaoChuan on 2017/5/13.
 */
public class Provider {
    private String name;
    private Host host;

    public Provider() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

}
