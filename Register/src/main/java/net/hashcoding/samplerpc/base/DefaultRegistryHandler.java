package net.hashcoding.samplerpc.base;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.hashcoding.samplerpc.common.entity.Host;
import net.hashcoding.samplerpc.common.entity.Provider;
import net.hashcoding.samplerpc.common.message.Command;

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

    public interface Callback {
        List<Host> requireServiceList(String serviceName);

        void register(String serviceName, Host provider);

        void unregister(String serviceName, Host provider);

        void unregisterAll(Host provider);
    }
}
