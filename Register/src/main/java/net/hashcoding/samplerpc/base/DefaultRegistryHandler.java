package net.hashcoding.samplerpc.base;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import net.hashcoding.samplerpc.common.Command;
import net.hashcoding.samplerpc.common.Host;
import net.hashcoding.samplerpc.common.Provider;
import net.hashcoding.samplerpc.common.utils.LogUtils;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by MaoChuan on 2017/5/13.
 */
public class DefaultRegistryHandler extends SimpleChannelInboundHandler<Command> {
    private static final String TAG = "DefaultRegistryHandler";

    private Callback callback;

    public DefaultRegistryHandler() {
        this(null);
    }

    public DefaultRegistryHandler(Callback callback) {
        this.callback = callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, Command command) throws Exception {
        switch (command.getType()) {
            case Command.GET_SERVER_LIST: {
                String serviceName = command.factoryFromBody();
                List<Host> providers = callback.requireServiceList(serviceName);
                Command response = new Command(
                        Command.GET_SERVER_LIST_RESPONSE, providers);
                response.setRequestId(command.getRequestId());
                context.channel().writeAndFlush(response);
                break;
            }

            case Command.REGISTER_SERVER: {
                Provider provider = command.factoryFromBody();
                if (callback != null)
                    callback.register(provider.getName(), provider.getHost());
                break;
            }

            case Command.UNREGISTER_SERVER: {
                Provider provider = command.factoryFromBody();
                if (callback != null)
                    callback.unregister(provider.getName(), provider.getHost());
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        LogUtils.t(TAG, evt.toString());
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                Host host = Host.factory((InetSocketAddress) ctx.channel().remoteAddress());
                LogUtils.t(TAG, "host will be close " + host.toString());
                if (callback != null) {
                    callback.unregisterAll(host);
                }
                ctx.close();
            } else if (event.state().equals(IdleState.WRITER_IDLE)) {
                LogUtils.t(TAG, "write idle ");
            } else if (event.state().equals(IdleState.ALL_IDLE)) {
                Host host = Host.factory((InetSocketAddress) ctx.channel().remoteAddress());
                LogUtils.t(TAG, "send heart beat to request " + host.toString());
                Command command = new Command(Command.HEART_BEAT_REQUEST, null);
                ctx.channel().writeAndFlush(command);
            } else {
                LogUtils.t(TAG, "what the fuck?");
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        if (callback != null) {
            Host host = Host.factory((InetSocketAddress) ctx.channel().remoteAddress());
            callback.unregisterAll(host);
        }
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LogUtils.e(TAG, cause);
//        if (callback != null) {
//            Host host = Host.factory((InetSocketAddress) ctx.channel().remoteAddress());
//            callback.unregisterAll(host);
//        }
        ctx.close();
    }

    public interface Callback {
        List<Host> requireServiceList(String serviceName);

        void register(String serviceName, Host provider);

        void unregister(String serviceName, Host provider);

        void unregisterAll(Host provider);
    }
}
