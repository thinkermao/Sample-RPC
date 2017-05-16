package net.hashcoding.samplerpc.client.base;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.hashcoding.samplerpc.common.Promise;
import net.hashcoding.samplerpc.common.message.Command;
import net.hashcoding.samplerpc.common.message.InvokeResponse;

/**
 * Created by MaoChuan on 2017/5/13.
 */
public class DefaultClientHandler extends SimpleChannelInboundHandler<Command> {
    private static final String TAG = "DefaultClientHandler";

    protected void channelRead0(ChannelHandlerContext context, Command command) throws Exception {
        switch (command.getType()) {
            case Command.INVOKE_RESPONSE:
                long requestId = command.getRequestId();
                Promise<InvokeResponse> promise =
                        ResponseMapHelper.responses.get(requestId);
                InvokeResponse response = command.factoryFromBody(); //InvokeResponse.factory(command.getBody());
                promise.setValue(response);
                break;
        }
    }
}
