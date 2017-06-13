package net.hashcoding.samplerpc.base;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.hashcoding.samplerpc.common.entity.Host;
import net.hashcoding.samplerpc.common.message.Command;
import net.hashcoding.samplerpc.common.utils.ConditionUtils;

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
                List<Host> hosts = command.factoryFromBody();
                callback.serviceResponse(command.getRequestId(), hosts);
                break;

            default:
                // Notice: last handler
                break;
        }
    }

    public interface Callback {
        void serviceResponse(long requestId, List<Host> hosts);
    }
}
