package net.hashcoding.simplerpc.base;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.ConcurrentSet;
import net.hashcoding.simplerpc.Registry;
import net.hashcoding.simplerpc.common.Promise;
import net.hashcoding.simplerpc.common.entity.Host;
import net.hashcoding.simplerpc.common.handle.*;
import net.hashcoding.simplerpc.common.message.RegisterRequest;
import net.hashcoding.simplerpc.common.utils.LogUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Created by MaoChuan on 2017/5/13.
 */
public class DefaultRegistry implements Registry,
        DefaultRegistryHandler.Callback {
    private static final String TAG = "DefaultRegistry";

    private final ServerBootstrap server;
    private final EventLoopGroup loopGroupBoss;
    private final EventLoopGroup loopGroupWorkers;

    private final Host host;
    private final ConcurrentHashMap<String, ConcurrentSet<Host>> provideServices;
    private final ConcurrentHashMap<Host, Host> providers;

    public DefaultRegistry(Host server) {
        this.host = server;

        this.providers = new ConcurrentHashMap<>();
        this.provideServices = new ConcurrentHashMap<>();
        this.server = new ServerBootstrap();

        int availableProcessors = Runtime.getRuntime().availableProcessors();

        LogUtils.i(TAG, "processors:"
                + String.valueOf(availableProcessors));
        this.loopGroupBoss = new NioEventLoopGroup(1);
        this.loopGroupWorkers = new NioEventLoopGroup(
                availableProcessors - 1);
    }

    @Override
    public void start() {
        prepareAcceptor();

        try {
            ChannelFuture future = server.bind().sync();
            if (!future.isSuccess()) {
                future.channel().close();
                throw new RuntimeException("bind failed");
            }
        } catch (InterruptedException e) {
            LogUtils.e(TAG, e);
        }
    }

    public Future<Boolean> startAsync() {
        prepareAcceptor();
        Promise<Boolean> promise = new Promise<>();
        server.bind().addListener((ChannelFuture future) -> {
            if (!future.isSuccess()) {
                future.channel().close();
            }
            promise.setValue(future.isSuccess());
        });
        return promise.getFuture();
    }

    @Override
    public void shutdown() {
        LogUtils.d(TAG, "register shutdown ");
        loopGroupWorkers.shutdownGracefully();
        loopGroupBoss.shutdownGracefully();
    }

    public List<Host> requireServiceList(String serviceName) {
        ConcurrentSet<Host> hosts = getProvidersByServiceName(serviceName);
        return new ArrayList<>(hosts);
    }

    @Override
    public void register(Host remote, RegisterRequest request) {
        providers.put(remote, request.getHost());
        for (String s : request.getServices()) {
            register(s, request.getHost());
        }
    }

    @Override
    public void unregister(Host remote) {
        Host host = providers.get(remote);
        if (host == null)
            return;
        LogUtils.d(TAG, "unregister all services" +
                " of remote:" + host.toString());
        provideServices.forEach((service, hostSet) -> hostSet.remove(host));
        providers.remove(remote);
    }

    private void prepareAcceptor() {
        LogUtils.d(TAG, "start register at: " + host.toString());
        server.group(loopGroupBoss, loopGroupWorkers)
                .localAddress(host.toAddress())
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024) // 链接队列个数
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, false);

        server.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipe = ch.pipeline();
                pipe.addLast(new IdleStateHandler(10, 0, 0));
                pipe.addLast(new HeartBeatReceiveTrigger());
                pipe.addLast(new ConnectionWatchdog(
                        DefaultRegistry.this::connectionInterrupted));
                pipe.addLast(new MessageDecoder());
                pipe.addLast(new MessageEncoder());
                pipe.addLast(new DefaultRegistryHandler(DefaultRegistry.this));
                pipe.addLast(new DefaultExceptionCaught());
            }
        });
    }

    private void register(String service, Host host) {
        LogUtils.d(TAG, "remote:" + host.toString()
                + " register " + service);
        ConcurrentSet<Host> hosts = getProvidersByServiceName(service);
        hosts.add(host);
    }

    private void connectionInterrupted(ChannelHandlerContext handler) {
        SocketAddress remoteAddress = handler.channel().remoteAddress();
        Host remote = Host.factory((InetSocketAddress) remoteAddress);
        LogUtils.d(TAG, "remote:" + remote + " disconnected");

        unregister(remote);
    }

    private ConcurrentSet<Host> getProvidersByServiceName(String name) {
        return provideServices.computeIfAbsent(
                name, k -> new ConcurrentSet<>());
    }
}
