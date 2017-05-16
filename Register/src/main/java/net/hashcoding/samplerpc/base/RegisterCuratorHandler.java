package net.hashcoding.samplerpc.base;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.hashcoding.samplerpc.common.Command;
import net.hashcoding.samplerpc.common.Host;
import net.hashcoding.samplerpc.common.utils.ConditionUtils;
import net.hashcoding.samplerpc.common.utils.LogUtils;

import java.util.List;

/**
 * Created by MaoChuan on 2017/5/13.
 */
public class RegisterCuratorHandler extends SimpleChannelInboundHandler<Command> {
    private static final String TAG = "RegisterCuratorHandler";

    private Callback callback;

    public RegisterCuratorHandler(Callback callback) {
        ConditionUtils.checkNotNull(callback);
        this.callback = callback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, Command command) throws Exception {
        switch (command.getType()) {
            case Command.GET_SERVER_LIST_RESPONSE:
                List<Host> hosts = command.factoryFromBody();//Host.factoryArray(command.getBody());
                callback.serviceResponse(command.getRequestId(), hosts);
                break;

            case Command.HEART_BEAT_REQUEST:
                LogUtils.t(TAG, "received heart beat request from register");
                Command response = new Command(Command.HEART_BEAT_RESPONSE, null);
                response.setRequestId(command.getRequestId());
                context.channel().writeAndFlush(response);
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //super.exceptionCaught(ctx, cause);
        LogUtils.e(TAG, cause);
        ctx.close();
        cause.printStackTrace();
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //super.channelInactive(ctx);

        LogUtils.d(TAG, "与服务器断开连接服务器");

        callback.inactive(ctx.channel());
        ctx.close();
    }

    public interface Callback {
        void inactive(Channel channel);

        void serviceResponse(long requestId, List<Host> hosts);
    }
}
