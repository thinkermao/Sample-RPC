package net.hashcoding.samplerpc.server.base;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import net.hashcoding.samplerpc.common.entity.Host;
import net.hashcoding.samplerpc.common.handle.*;
import net.hashcoding.samplerpc.common.message.Command;
import net.hashcoding.samplerpc.common.message.InvokeRequest;
import net.hashcoding.samplerpc.common.message.InvokeResponse;
import net.hashcoding.samplerpc.common.message.RegisterRequest;
import net.hashcoding.samplerpc.common.utils.ConditionUtils;
import net.hashcoding.samplerpc.common.utils.LogUtils;
import net.hashcoding.samplerpc.common.utils.ReflectUtils;
import net.hashcoding.samplerpc.server.Server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by MaoChuan on 2017/5/12.
 */
public class DefaultServer implements Server {
    private static final String TAG = "DefaultServer";

    private final Host remote;
    private final Host local;
    private final Bootstrap register;
    private final ServerBootstrap server;
    private final EventLoopGroup loopGroupBoss;
    private final EventLoopGroup loopGroupWorkers;
    private final ConcurrentHashMap<String, Object> services;

    private DefaultServer(Host remote, Host local,
                          ConcurrentHashMap<String, Object> services) {
        this.remote = remote;
        this.local = local;
        this.services = services;

        register = new Bootstrap();
        server = new ServerBootstrap();

        int availableProcessors = Runtime.getRuntime().availableProcessors();

        LogUtils.i(TAG, "processors:" + String.valueOf(availableProcessors));
        loopGroupBoss = new NioEventLoopGroup(1);
        loopGroupWorkers = new NioEventLoopGroup(availableProcessors - 1);
    }

    @Override
    public void start() {
        setupServiceProvider();
        setupRegisterClient();
    }

    private void setupServiceProvider() {
        LogUtils.d(TAG, "Begin listen invoke request: " + local.toString());
        server.group(loopGroupBoss, loopGroupWorkers)
                .channel(NioServerSocketChannel.class)
                .localAddress(local.toAddress())
                .option(ChannelOption.SO_BACKLOG, 1024) // 链接队列个数
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, false) //设置心跳参数 FALSE为不启用参数
                .childOption(ChannelOption.TCP_NODELAY, true);
        server.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) throws Exception {
                ChannelPipeline pipe = channel.pipeline();
                pipe.addLast(new IdleStateHandler(10, 0, 0));
                pipe.addLast(new HeartBeatReceiveTrigger());
                pipe.addLast(new ConnectionWatchdog(/* ignore */));
                pipe.addLast(new MessageDecoder());
                pipe.addLast(new MessageEncoder());
                pipe.addLast(new DefaultServerHandler(
                        DefaultServer.this::handleInvokeRequest));
                pipe.addLast(new DefaultExceptionCaught());
            }
        });

        try {
            server.bind().sync();
        } catch (InterruptedException e) {
            LogUtils.d(TAG, e);
            // TODO: bind 失败后不能提供服务，需要重启
            throw new RuntimeException(e);
        }
    }

    private void setupRegisterClient() {
        register.group(new NioEventLoopGroup(1))
                .channel(NioSocketChannel.class)
                .remoteAddress(remote.toAddress())
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, false); //设置心跳参数 FALSE为不启用参数
        register.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) throws Exception {
                ChannelPipeline pipe = channel.pipeline();
                pipe.addLast(new IdleStateHandler(0, 5, 0));
                pipe.addLast(new HeartBeatSendTrigger());
                pipe.addLast(new ConnectionWatchdog(
                        DefaultServer.this::reconnect));
                pipe.addLast(new MessageEncoder());
                pipe.addLast(new MessageDecoder());
                pipe.addLast(new DefaultExceptionCaught());
            }
        });
        doConnect();
    }

    private void doSuccess(Channel channel) {
        LogUtils.d(TAG, "Registry self to register");
        Enumeration<String> keys = services.keys();
        RegisterRequest request = new RegisterRequest();
        List<String> waitForRegister = new ArrayList<>();
        while (keys.hasMoreElements()) {
            waitForRegister.add(keys.nextElement());
        }
        request.setServices(waitForRegister);
        request.setHost(local);
        channel.writeAndFlush(new Command(Command.REGISTER_SERVER, request));
    }

    private void reconnect(ChannelHandlerContext handler) {
        doConnect();
    }

    private void doConnect() {
        LogUtils.d(TAG, "Try connect register");
        try {
            ChannelFuture channelFuture = register.connect();
            channelFuture.addListener((ChannelFuture f) -> {
                if (f.isSuccess()) {
                    doSuccess(f.channel());
                    return;
                }
                // 连接不上，三秒后重试
                EventLoop loop = f.channel().eventLoop();
                loop.schedule(this::doConnect, 3, TimeUnit.SECONDS);
            });
        } catch (Exception e) {
            LogUtils.f(TAG, e);
        }
    }

    // todo: any better solution ?
    private void handleInvokeRequest(
            ChannelHandlerContext context, Command command) {
        InvokeRequest invoke = command.factoryFromBody();
        InvokeResponse response = new InvokeResponse();
        Command result = new Command(Command.INVOKE_RESPONSE, null);
        result.setRequestId(command.getRequestId());

        LogUtils.d(TAG, "received invoke: " + invoke.getInterfaceName());

        // 找到服务
        Object service = services.get(invoke.getInterfaceName());
        if (service == null) {
            response.setThrowReason("can't found interface name");
            result.setBody(response);
            context.channel().writeAndFlush(result);
            return;
        }

        try {
            Class cls = Class.forName(invoke.getInterfaceName());

            List<Class> argsTypeList = new ArrayList<Class>(invoke.getParameterTypes().length);
            for (String s : invoke.getParameterTypes()) {
                argsTypeList.add(ReflectUtils.forName(s));
            }
            Method method = cls.getMethod(invoke.getMethodName(), argsTypeList.toArray(new Class[argsTypeList.size()]));
            Object invokeResult = method.invoke(service, invoke.getArguments());

            response.setResponse(invokeResult);
            result.setBody(response);
            context.channel().writeAndFlush(result);
        } catch (ClassNotFoundException | NoSuchMethodException
                | IllegalAccessException | InvocationTargetException e) {
            response.setThrowReason(e.toString());
            result.setBody(response);
            context.channel().writeAndFlush(result);
        }
    }

    @Override
    public void shutdown() {
        LogUtils.d(TAG, "server shutdown ");
        loopGroupWorkers.shutdownGracefully();
        loopGroupBoss.shutdownGracefully();
    }

    public static class Build {
        private Host remote;
        private Host local;
        private ConcurrentHashMap<String, Object> services =
                new ConcurrentHashMap<>();

        public Build() {
        }

        public Build addService(Class<?> clazz, Object object) {
            String name = clazz.getCanonicalName();
            services.put(name, object);
            return this;
        }

        public Host getRemote() {
            return remote;
        }

        public Build setRemote(Host remote) {
            this.remote = remote;
            return this;
        }

        public Host getLocal() {
            return local;
        }

        public Build setLocal(Host local) {
            this.local = local;
            return this;
        }

        public DefaultServer build() {
            ConditionUtils.checkNotNull(remote);
            ConditionUtils.checkNotNull(local);

            return new DefaultServer(remote, local, services);
        }
    }
}
