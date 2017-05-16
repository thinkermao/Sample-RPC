package net.hashcoding.samplerpc.common.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import net.hashcoding.samplerpc.common.message.Command;
import net.hashcoding.samplerpc.common.utils.LogUtils;

/**
 * Created by MaoChuan on 2017/5/16.
 */
public class HeartBeatSendTrigger extends ChannelInboundHandlerAdapter {
    private static final String TAG = "HeartBeatSendTrigger";

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                sendHeartBeatMessage(ctx);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    private void sendHeartBeatMessage(ChannelHandlerContext context) {
        // write heartbeat to server
        LogUtils.t(TAG, "Send heart beat request to server");
        context.channel().writeAndFlush(Command.heartBeatRequest());
    }
}
