package net.hashcoding.samplerpc.common.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.hashcoding.samplerpc.common.utils.LogUtils;

/**
 * Created by MaoChuan on 2017/5/16.
 */
public class DefaultExceptionCaught extends ChannelInboundHandlerAdapter {
    private static final String TAG = "DefaultExceptionCaught";

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LogUtils.d(TAG, cause);
        ctx.close();
    }
}
