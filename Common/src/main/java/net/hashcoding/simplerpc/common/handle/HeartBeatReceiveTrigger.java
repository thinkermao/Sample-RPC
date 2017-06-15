package net.hashcoding.simplerpc.common.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import net.hashcoding.simplerpc.common.ChannelHandlerCallback;
import net.hashcoding.simplerpc.common.entity.Host;
import net.hashcoding.simplerpc.common.utils.LogUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Created by MaoChuan on 2017/5/16.
 */
public class HeartBeatReceiveTrigger extends ChannelInboundHandlerAdapter {
    private static final String TAG = "HeartBeatReceiveTrigger";

    private ChannelHandlerCallback callback;

    public HeartBeatReceiveTrigger() {
        this(null);
    }

    public HeartBeatReceiveTrigger(ChannelHandlerCallback callback) {
        this.callback = callback;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                channelReaderIdle(ctx);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    private void channelReaderIdle(ChannelHandlerContext context) {
        SocketAddress remoteAddress = context.channel().remoteAddress();
        Host host = Host.factory((InetSocketAddress) remoteAddress);
        LogUtils.t(TAG, "May be "
                + host.toString() + " has disconnect");
        if (callback != null) {
            callback.run(context);
        }
        context.close();
    }
}
