package net.hashcoding.samplerpc.server.base;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.hashcoding.samplerpc.common.Command;
import net.hashcoding.samplerpc.common.utils.LogUtils;

/**
 * Created by MaoChuan on 2017/5/12.
 */
public class DefaultServerRegisterHandler
        extends SimpleChannelInboundHandler<Command> {
    private static final String TAG = "DefaultServerRegisterHandler";

    private InactiveCallback callback;

    public DefaultServerRegisterHandler(InactiveCallback callback) {
        this.callback = callback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext handlerContext,
                                Command command) throws Exception {
        switch (command.getType()) {
            case Command.HEART_BEAT_REQUEST:
                LogUtils.t(TAG, "received heart beat request from register");
                Command response = new Command(Command.HEART_BEAT_RESPONSE, null);
                response.setRequestId(command.getRequestId());
                handlerContext.channel().writeAndFlush(response);
                break;

            default:
                break;
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        LogUtils.d(TAG, "与服务器断开连接服务器");

        callback.call();
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //ctx.fireExceptionCaught(cause);

        LogUtils.e(TAG, cause);
        ctx.close();
    }

    public interface InactiveCallback {
        void call();
    }
}
