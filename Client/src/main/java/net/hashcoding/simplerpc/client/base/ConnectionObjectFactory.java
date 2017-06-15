package net.hashcoding.simplerpc.client.base;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import net.hashcoding.simplerpc.common.entity.Host;
import net.hashcoding.simplerpc.common.handle.*;
import net.hashcoding.simplerpc.common.utils.LogUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Created by MaoChuan on 2017/5/13.
 */
public class ConnectionObjectFactory extends BasePooledObjectFactory<Channel> {
    private static final String TAG = "ConnectionObjectFactory";
    private Host remote;

    public ConnectionObjectFactory(Host remote) {
        this.remote = remote;
    }

    private Channel createNewConChannel() {
        LogUtils.d(TAG, "try create new connection");
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class)
                .remoteAddress(remote.toAddress())
                .group(new NioEventLoopGroup(1))
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .handler(new ChannelInitializer<Channel>() {
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipe = ch.pipeline();
                        pipe.addLast(new IdleStateHandler(0, 5, 0));
                        pipe.addLast(new HeartBeatSendTrigger());
                        pipe.addLast(new ConnectionWatchdog(
                                ConnectionObjectFactory.this::inactive));
                        pipe.addLast(new MessageEncoder());
                        pipe.addLast(new MessageDecoder());
                        pipe.addLast(new DefaultClientHandler());
                        pipe.addLast(new DefaultExceptionCaught());
                    }
                });
        ChannelFuture future = bootstrap.connect();
        try {
            future.sync();
            return future.channel();
        } catch (InterruptedException e) {
            // if there are something wrong, close
            LogUtils.e(TAG, e);
            future.channel().close();
            return null;
        }
    }

    @Override
    public Channel create() throws Exception {
        return createNewConChannel();
    }

    @Override
    public PooledObject<Channel> wrap(Channel obj) {
        return new DefaultPooledObject<>(obj);
    }

    @Override
    public void destroyObject(PooledObject<Channel> p) throws Exception {
        p.getObject().close();
    }

    @Override
    public boolean validateObject(PooledObject<Channel> p) {
        Channel object = p.getObject();
        return object.isActive();
    }

    private void inactive(ChannelHandlerContext context) {
        // because of validate begin use, this is safe.
        // just ignore it.
    }
}