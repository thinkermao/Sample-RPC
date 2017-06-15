package net.hashcoding.simplerpc.server.base;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.hashcoding.simplerpc.common.message.Command;

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
