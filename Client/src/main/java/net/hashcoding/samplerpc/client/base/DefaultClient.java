package net.hashcoding.samplerpc.client.base;

import io.netty.channel.Channel;
import net.hashcoding.samplerpc.base.RegisterCurator;
import net.hashcoding.samplerpc.client.Client;
import net.hashcoding.samplerpc.client.Proxy;
import net.hashcoding.samplerpc.client.ResponseMapHelper;
import net.hashcoding.samplerpc.common.*;
import net.hashcoding.samplerpc.common.utils.LogUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by MaoChuan on 2017/5/13.
 */
public class DefaultClient implements Client {

    private static final String TAG = "DefaultClient";

    private final Class<? extends Proxy> proxyClass;

    private final Host remote;

    private final RegisterCurator curator;
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<ChannelConf>> services;
    private final ConcurrentHashMap<String, Promise<Boolean>> isServiceAvailable;
    private long requestTimeoutMillis;

    public DefaultClient(Host remote, long requestTimeoutMillis) {
        this(remote, requestTimeoutMillis, CGLibProxy.class);
    }

    public DefaultClient(Host remote, long requestTimeoutMilli, Class<? extends Proxy> proxyClass) {
        this.remote = remote;
        this.proxyClass = proxyClass;
        this.requestTimeoutMillis = requestTimeoutMilli;

        curator = new RegisterCurator(remote);
        curator.setPathChangeListener(this::onPathCacheChange);

        services = new ConcurrentHashMap<>();
        isServiceAvailable = new ConcurrentHashMap<>();
    }

    public Future<Boolean> start(int timeout) {
        LogUtils.d(TAG, "start client, connect to: " + remote.toString());
        Promise<Boolean> promise = new Promise<>();
        curator.start(timeout, promise::setValue);
        return promise.getFuture();
    }

    @Override
    public Future<Boolean> addTargetService(Class<?> clazz) {
        LogUtils.d(TAG, "add target service: " + clazz.getCanonicalName());

        String name = clazz.getCanonicalName();
        Promise<Boolean> promise = isServiceAvailable
                .computeIfAbsent(name, k -> new Promise<>());
        List<ChannelConf> channels = services
                .computeIfAbsent(name, k -> new CopyOnWriteArrayList<>());
        if (!channels.isEmpty()) {
            isServiceAvailable.remove(name);
            promise.setValue(true);
        } else {
            curator.addTargetService(name);
            curator.updatePathCache();
        }
        return promise.getFuture();
    }

    public Response sendMessage(Class<?> clazz, Method method, Object[] args) {
        LogUtils.d(TAG, "try send message: " + clazz.getCanonicalName());

        Request invoke = new Request();
        invoke.setInterfaceName(clazz.getCanonicalName());
        invoke.setMethodName(method.getName());
        invoke.setArguments(args);

        String[] types = new String[method.getParameterTypes().length];
        int index = 0;
        for (Class type : method.getParameterTypes()) {
            types[index] = type.getCanonicalName();
            index++;
        }
        invoke.setParameterTypes(types);

        ChannelConf channelWrapper = selectChannel(clazz.getCanonicalName());
        if (channelWrapper == null) {
            Response response = new Response();
            response.setThrowReason("Channel is not active");
            return response;
        }

        Channel channel = null;
        try {
            channel = channelWrapper.getObjectPool().borrowObject();
        } catch (Exception e) {
            LogUtils.e(TAG, e);
        }
        if (channel == null) {
            Response response = new Response();
            response.setThrowReason("Channel is not available");
            return response;
        }

        Command command = new Command(Command.INVOKE_REQUEST, invoke);
        Channel finalChannel = channel;

        Promise<Response> promise = new Promise<>();

        ResponseMapHelper.responses.put(command.getRequestId(), promise);

        channel.writeAndFlush(command);
        Future<Response> future = promise.getFuture();
        try {
            return future.get(requestTimeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Response response = new Response();
            response.setThrowReason("require timeout");
            return response;
        } finally {
            try {
                channelWrapper.getObjectPool().returnObject(finalChannel);
            } catch (Exception e) {
                LogUtils.e(TAG, e);
            }
            ResponseMapHelper.responses.remove(command.getRequestId());
        }
    }

    public <T> T getInterface(Class<T> serviceInterface) {
        try {
            Proxy proxy = proxyClass.newInstance();
            return proxy.getInterface(this, serviceInterface);
        } catch (InstantiationException | IllegalAccessException e) {
            LogUtils.e(TAG, e);
            // e.printStackTrace();
        }
        return null;
    }

    public void close() {
        LogUtils.d(TAG, "close client");
        curator.shutdown();
        services.forEach((services, hosts) -> {
            for (ChannelConf channel : hosts) {
                channel.close();
            }
        });
    }

    private ChannelConf selectChannel(String service) {
        List<ChannelConf> channels = services.computeIfAbsent(service, k -> new CopyOnWriteArrayList<>());
        // TODO: distribution
        Random random = new Random();
        int size = channels.size();
        if (size < 1) {
            return null;
        }
        return channels.get(random.nextInt(size));
    }

    private void onPathCacheChange(String service, List<Host> hosts) {
        CopyOnWriteArrayList<ChannelConf> channels = services
                .computeIfAbsent(service, k -> new CopyOnWriteArrayList<>());

        ArrayList<ChannelConf> waitForRemove = new ArrayList<>();

        for (ChannelConf channel : channels) {
            Host remote = channel.getRemote();
            if (!hosts.contains(remote)) {
                LogUtils.d(TAG, "remove channel: " + channel.toString());
                waitForRemove.add(channel);
                channel.close();
            }
        }

        channels.removeAll(waitForRemove);

        for (Host host : hosts) {
            boolean contain = false;
            for (ChannelConf channel : channels) {
                if (channel.getRemote().equals(host)) {
                    contain = true;
                    break;
                }
            }
            if (!contain) {
                LogUtils.d(TAG, "add channel " + host.toString());
                channels.add(new ChannelConf(host));
            }
        }

        Promise<Boolean> promise = isServiceAvailable.get(service);
        if (!channels.isEmpty() && promise != null) {
            promise.setValue(true);
            isServiceAvailable.remove(service);
        }
        services.put(service, channels);
    }
}
