package net.hashcoding.samplerpc.common;

import io.netty.channel.ChannelHandler;

/**
 * Created by MaoChuan on 2017/5/16.
 */
public interface ChannelHandlerCallback {
    void run(ChannelHandler handler);
}
