package net.hashcoding.samplerpc.common.message;

import net.hashcoding.samplerpc.common.entity.Host;

import java.util.List;

/**
 * Created by MaoChuan on 2017/5/16.
 */
public class RegisterRequest {
    private Host host;
    private List<String> services;

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }
}
