package net.hashcoding.simplerpc;

import net.hashcoding.simplerpc.common.entity.Host;

import java.util.List;

/**
 * Created by MaoChuan on 2017/5/13.
 */
public interface RegistryClient {
    void start(int timeout, ConnectCallback callback);

    void shutdown();

    void addTargetService(String name);

    void removeTargetService(String name);

    List<Host> getTargetServices(String name);

    void setPathChangeListener(PathChangeListener listener);

    interface PathChangeListener {
        void call(String service, List<Host> hosts);
    }

    interface ConnectCallback {
        void call(boolean status);
    }
}
