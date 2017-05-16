package net.hashcoding.samplerpc.base;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.hashcoding.samplerpc.common.entity.Host;
import net.hashcoding.samplerpc.common.message.Command;
import net.hashcoding.samplerpc.common.message.RegisterRequest;
import net.hashcoding.samplerpc.common.utils.LogUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
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
                List<Host> providers = callback == null
                        ? new ArrayList<>()
                        : callback.requireServiceList(serviceName);
                Command response = new Command(
                        Command.GET_SERVER_LIST_RESPONSE, providers);
                response.setRequestId(command.getRequestId());
                context.channel().writeAndFlush(response);
                break;
            }

            case Command.REGISTER_SERVER: {
                if (callback != null) {
                    RegisterRequest request = command.factoryFromBody();
                    SocketAddress address = context.channel().remoteAddress();
                    Host remote = Host.factory((InetSocketAddress) address);
                    callback.register(remote, request);
                }
                break;
            }

            case Command.UNREGISTER_SERVER: {
                if (callback != null) {
                    SocketAddress address = context.channel().remoteAddress();
                    Host remote = Host.factory((InetSocketAddress) address);
                    callback.unregister(remote);
                }
                break;
            }
            case Command.HEART_BEAT_REQUEST:
                LogUtils.d(TAG, "receive heart beat message");
            default:
                // NOTICE: must be last handle
                break;
        }
    }

    public interface Callback {
        List<Host> requireServiceList(String serviceName);

        void register(Host remote, RegisterRequest request);

        void unregister(Host remote);
    }
}
