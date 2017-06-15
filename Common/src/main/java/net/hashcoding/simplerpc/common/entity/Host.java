package net.hashcoding.simplerpc.common.entity;

import java.net.InetSocketAddress;

/**
 * Created by MaoChuan on 2017/5/12.
 */
public class Host {
    private String ip;
    private int port;

    public Host() {
        ip = "";
        port = 0;
    }

    public Host(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public static Host factory(InetSocketAddress address) {
        return new Host(address.getHostString(), address.getPort());
    }

    @Override
    public String toString() {
        return "Host{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Host host = (Host) o;

        if (port != host.port) return false;
        return ip != null ? ip.equals(host.ip) : host.ip == null;
    }

    public InetSocketAddress toAddress() {
        return new InetSocketAddress(ip, port);
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
