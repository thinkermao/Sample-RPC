package net.hashcoding.samplerpc.base;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.hashcoding.samplerpc.RegistryClient;
import net.hashcoding.samplerpc.common.Command;
import net.hashcoding.samplerpc.common.Host;
import net.hashcoding.samplerpc.common.handle.MessageDecoder;
import net.hashcoding.samplerpc.common.handle.MessageEncoder;
import net.hashcoding.samplerpc.common.utils.LogUtils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by MaoChuan on 2017/5/13.
 */
public class RegisterCurator implements RegistryClient, RegisterCuratorHandler.Callback {
    private static final String TAG = "RegisterCurator";

    private final Bootstrap register;
    private final EventLoopGroup loopGroup;
    private final Host remote;
    private final ConcurrentHashMap<Long, String> requireServices;
    private final ConcurrentHashMap<String, List<Host>> services;
    private Channel channel;
    private ScheduledFuture scheduledFuture;
    private ConnectCallback connectCallback;
    private PathChangeListener listener;

    public RegisterCurator(Host remote) {
        this.remote = remote;

        register = new Bootstrap();
        loopGroup = new NioEventLoopGroup(1);
        services = new ConcurrentHashMap<>();
        requireServices = new ConcurrentHashMap<>();
    }

    @Override
    public void addTargetService(String name) {
        services.computeIfAbsent(name, k -> new ArrayList<>());
    }

    @Override
    public void removeTargetService(String name) {
        services.remove(name);
        if (listener != null) {
            listener.call(name, new ArrayList<>());
        }
    }

    @Override
    public List<Host> getTargetServices(String name) {
        List<Host> targets = services.get(name);
        if (targets == null) {
            return new ArrayList<>();
        }
        return targets;
    }

    @Override
    public void setPathChangeListener(PathChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void start(int timeout, ConnectCallback callback) {
        LogUtils.d(TAG, "start register client");
        this.connectCallback = callback;
        register.group(this.loopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipe = ch.pipeline();
                        //pipe.addLast(new IdleStateHandler(0, 0, 60));
                        pipe.addLast(new MessageDecoder());
                        pipe.addLast(new MessageEncoder());
                        pipe.addLast(new RegisterCuratorHandler(RegisterCurator.this));
                    }
                });

        doConnect();
    }

    // 每10s更新一次服务列表
    public void updatePathCache() {
        if (channel == null)
            return;

        LogUtils.t(TAG, "try update service list");

        requireServices.clear();
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }

        Enumeration<String> keys = services.keys();
        while (keys.hasMoreElements()) {
            String name = keys.nextElement();
            Command command = new Command(Command.GET_SERVER_LIST, name);
            requireServices.put(command.getRequestId(), name);
            channel.writeAndFlush(command);
        }
        scheduledFuture = channel.eventLoop()
                .schedule(this::updatePathCache, 10, TimeUnit.SECONDS);
    }

    private void doSuccess(Channel channel) {
        this.channel = channel;
        updatePathCache();
    }

    private void doConnect() {
        this.channel = null;
        ChannelFuture future = register.connect(remote.toAddress());
        future.addListener((ChannelFuture f) -> {
            if (f.isSuccess()) {
                doSuccess(f.channel());
            } else {
                this.channel = null;
            }
            if (connectCallback != null)
                connectCallback.call(f.isSuccess());
        });
    }

    @Override
    public void shutdown() {
        LogUtils.d(TAG, "register client shutdown");
        loopGroup.shutdownGracefully();
    }

    @Override
    public void inactive(Channel channel) {
        LogUtils.d(TAG, "connect close, try connect to after 1 second");
        channel.eventLoop().schedule(this::doConnect, 1, TimeUnit.SECONDS);
    }

    @Override
    public void serviceResponse(long requestId, List<Host> hosts) {
        String name = requireServices.get(requestId);
        if (name == null)
            return;

        requireServices.remove(requestId);
        services.put(name, hosts);
        if (listener != null) {
            listener.call(name, hosts);
        }
    }
}
