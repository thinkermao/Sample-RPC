package net.hashcoding.samplerpc.base;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import net.hashcoding.samplerpc.Registry;
import net.hashcoding.samplerpc.common.entity.Host;
import net.hashcoding.samplerpc.common.handle.ConnectionWatchdog;
import net.hashcoding.samplerpc.common.handle.HeartBeatReceiveTrigger;
import net.hashcoding.samplerpc.common.handle.MessageDecoder;
import net.hashcoding.samplerpc.common.handle.MessageEncoder;
import net.hashcoding.samplerpc.common.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by MaoChuan on 2017/5/13.
 */
public class DefaultRegistry implements Registry, DefaultRegistryHandler.Callback {
    private static final String TAG = "DefaultRegistry";

    private final ServerBootstrap server;
    private final EventLoopGroup loopGroupBoss;
    private final EventLoopGroup loopGroupWorkers;

    private final Host host;
    // 存放对外提供的服务对象 <interface, services>
    private final ConcurrentHashMap<String, List<Host>> services;

    public DefaultRegistry(Host server) {
        this.host = server;

        this.services = new ConcurrentHashMap<>();
        this.server = new ServerBootstrap();

        int availableProcessors = Runtime.getRuntime().availableProcessors();

        LogUtils.i(TAG, "processors:" + String.valueOf(availableProcessors));
        this.loopGroupBoss = new NioEventLoopGroup(1);
        this.loopGroupWorkers = new NioEventLoopGroup(availableProcessors - 1);
    }

    @Override
    public void start() {
        LogUtils.d(TAG, "start register at: " + host.toString());
        server.group(loopGroupBoss, loopGroupWorkers)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024) // 链接队列个数
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, false)
                .localAddress(host.toAddress())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipe = ch.pipeline();
                        pipe.addLast(new IdleStateHandler(6, 0, 0));
                        pipe.addLast(new HeartBeatReceiveTrigger(
                                DefaultRegistry.this::connectionInterrupted));
                        pipe.addLast(new ConnectionWatchdog(
                                DefaultRegistry.this::connectionInterrupted));
                        pipe.addLast(new MessageDecoder());
                        pipe.addLast(new MessageEncoder());
                        pipe.addLast(new DefaultRegistryHandler(DefaultRegistry.this));
                    }
                });

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

    @Override
    public void shutdown() {
        LogUtils.d(TAG, "register shutdown ");
        loopGroupWorkers.shutdownGracefully();
        loopGroupBoss.shutdownGracefully();
    }

    private void connectionInterrupted(ChannelHandlerContext handler) {
        // TODO:
    }

    private List<Host> getProvidersByServiceName(String name) {
        return services.computeIfAbsent(name, k -> new ArrayList<>());
    }

    public List<Host> requireServiceList(String serviceName) {
        return getProvidersByServiceName(serviceName);
    }

    public void register(String serviceName, Host provider) {
        LogUtils.d(TAG, "register：" + serviceName + " with " + provider.toString());
        List<Host> providers = getProvidersByServiceName(serviceName);
        providers.add(provider);
    }

    public void unregister(String serviceName, Host provider) {
        LogUtils.d(TAG, "unregister：" + serviceName + " with " + provider.toString());
        List<Host> providers = getProvidersByServiceName(serviceName);
        providers.remove(provider);
    }

    @Override
    public void unregisterAll(Host provider) {
        LogUtils.d(TAG, "unregister all services with: " + provider.toString());
        services.forEach((services, hosts) -> hosts.remove(provider));
    }
}
