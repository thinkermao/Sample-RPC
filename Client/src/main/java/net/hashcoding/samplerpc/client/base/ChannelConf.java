package net.hashcoding.samplerpc.client.base;

import io.netty.channel.Channel;
import net.hashcoding.samplerpc.common.entity.Host;
import net.hashcoding.samplerpc.common.utils.ConditionUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;


/**
 * Created by MaoChuan on 2017/5/13.
 */
public class ChannelConf {
    private Host remote;
    private Channel channel;
    private ObjectPool<Channel> channelObjectPool;

    public ChannelConf(Host remote) {
        this.remote = remote;
        channelObjectPool = new GenericObjectPool<>(
                new ConnectionObjectFactory(remote));
    }

    public Host getRemote() {
        return remote;
    }

    public Channel borrow() throws Exception {
        ConditionUtils.checkArgument(this.channel == null);
        channel = channelObjectPool.borrowObject();
        return channel;
    }

    public void restore(Channel channel) throws Exception {
        ConditionUtils.checkArgument(channel == this.channel);
        this.channel = null;
        channelObjectPool.returnObject(channel);
    }

    public void close() {
        channelObjectPool.close();
    }

    @Override
    public String toString() {
        return "ChannelConf{" + remote.toString() +
                ", channelObjectPool=" + channelObjectPool +
                '}';
    }
}