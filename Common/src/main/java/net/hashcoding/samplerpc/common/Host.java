package net.hashcoding.samplerpc.common;

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

//    public static List<Host> factoryArray(byte [] bytes) {
//        String data = null;
//        try {
//            data = new String(bytes, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//            // TODO:
//        }
//        return JSON.parseArray(data, Host.class);
//    }

//    public static String toJsonString(List<Host> hosts) {
//        return JSON.toJSONString(hosts);
//    }
//
//    public static byte [] toJsonBytes(List<Host> hosts) {
//        return JSON.toJSONBytes(hosts);
//    }

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
