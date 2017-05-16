package net.hashcoding.samplerpc.server.base;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.hashcoding.samplerpc.common.message.Command;
import net.hashcoding.samplerpc.common.utils.LogUtils;

/**
 * Created by MaoChuan on 2017/5/12.
 */
public class DefaultServerHandler
        extends SimpleChannelInboundHandler<Command> {
    private static final String TAG = "DefaultServerHandler";

    private InvokeCallback callback;

    public DefaultServerHandler(InvokeCallback callback) {
        this.callback = callback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext handlerContext,
                                Command command) throws Exception {
        switch (command.getType()) {
            case Command.INVOKE_REQUEST:
                LogUtils.d(TAG, "invoke request");
                callback.call(handlerContext, command);
                break;

            default:
                // Notice: last handler
                break;
        }
    }

    public interface InvokeCallback {
        void call(ChannelHandlerContext context, Command command);
    }
}
