package net.hashcoding.samplerpc.server.base;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.hashcoding.samplerpc.common.Command;
import net.hashcoding.samplerpc.common.utils.LogUtils;

/**
 * Created by MaoChuan on 2017/5/12.
 */
public class DefaultServerHandler
        extends SimpleChannelInboundHandler<Command> {
    private static final String TAG = "DefaultServerHandler";

    private InvokeCallback callback;
    private SplitMessageCallback splitMessageCallback;

    public DefaultServerHandler(InvokeCallback callback,
                                SplitMessageCallback splitMessageCallback) {
        this.callback = callback;
        this.splitMessageCallback = splitMessageCallback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext handlerContext,
                                Command command) throws Exception {
        switch (command.getType()) {
            case Command.INVOKE_REQUEST:
                LogUtils.d(TAG, "invoke request");
                callback.call(handlerContext, command);
                break;

            case Command.SPLIT_MESSAGE:
                LogUtils.d(TAG, "received split message");
                splitMessageCallback.call(handlerContext, command, false);
                break;

            case Command.SPLIT_MESSAGE_DONE:
                splitMessageCallback.call(handlerContext, command, true);
                break;

            default:
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //ctx.fireExceptionCaught(cause);

        LogUtils.d(TAG, cause);
        ctx.close();
    }

    public interface InvokeCallback {
        void call(ChannelHandlerContext context, Command command);
    }

    public interface SplitMessageCallback {
        void call(ChannelHandlerContext context, Command command, boolean last);
    }
}
