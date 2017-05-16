package net.hashcoding.samplerpc.client.base;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import net.hashcoding.samplerpc.common.Host;
import net.hashcoding.samplerpc.common.handle.*;
import net.hashcoding.samplerpc.common.utils.LogUtils;
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
                .group(new NioEventLoopGroup(1))
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .handler(new ChannelInitializer<Channel>() {
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipe = ch.pipeline();
                        pipe.addLast(new IdleStateHandler(0, 5, 0));
                        pipe.addLast(new HeartBeatSendTrigger());
                        pipe.addLast(new ConnectionWatchdog());
                        pipe.addLast(new MessageEncoder());
                        pipe.addLast(new MessageDecoder());
                        pipe.addLast(new DefaultClientHandler());
                        pipe.addLast(new DefaultExceptionCaught());
                    }
                });
        try {
            ChannelFuture future = bootstrap.connect(remote.toAddress()).sync();
            return future.channel();
        } catch (InterruptedException e) {
            LogUtils.e(TAG, e);
        }
        return null;
    }

    @Override
    public Channel create() throws Exception {
        return createNewConChannel();
    }

    @Override
    public PooledObject<Channel> wrap(Channel obj) {
        //排查出错，之前直接返回个null，未对方法进行重写，导致出错，拿不出对象
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
}