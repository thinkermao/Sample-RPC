package net.hashcoding.samplerpc.client.base;

import io.netty.channel.Channel;
import net.hashcoding.samplerpc.common.Host;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;


/**
 * Created by MaoChuan on 2017/5/13.
 */
public class ChannelConf {
    private Host remote;
    private String connStr;
    private Channel channel;
    private ObjectPool<Channel> channelObjectPool;

    public ChannelConf(Host remote) {
        this.remote = remote;
        this.connStr = remote.getIp() + ":" + remote.getPort();
        channelObjectPool = new GenericObjectPool<Channel>(
                new ConnectionObjectFactory(remote));
    }

    public Host getRemote() {
        return remote;
    }

    public ObjectPool<Channel> getObjectPool() {
        return channelObjectPool;
    }

    public void close() {
        channelObjectPool.close();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChannelConf{");
        sb.append("connStr='").append(connStr).append('\'');
        sb.append(", channel=").append(channel);
        sb.append(", channelObjectPool=").append(channelObjectPool);
        sb.append('}');
        return sb.toString();
    }
}