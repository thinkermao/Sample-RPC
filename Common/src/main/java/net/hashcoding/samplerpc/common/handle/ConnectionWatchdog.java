package net.hashcoding.samplerpc.common.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.hashcoding.samplerpc.common.ChannelHandlerCallback;
import net.hashcoding.samplerpc.common.utils.LogUtils;

/**
 * Created by MaoChuan on 2017/5/16.
 */
public class ConnectionWatchdog extends ChannelInboundHandlerAdapter {
    private static final String TAG = "ConnectionWatchdog";

    private ChannelHandlerCallback callback;

    public ConnectionWatchdog() {
        this(null);
    }

    public ConnectionWatchdog(ChannelHandlerCallback callback) {
        this.callback = callback;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        LogUtils.t(TAG, "connection is interrupted");

        if (callback != null) {
            // notify client
            callback.run(ctx);
        }
        ctx.close();
    }
}
