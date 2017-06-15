package net.hashcoding.simplerpc.common;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by MaoChuan on 2017/5/16.
 */
public interface ChannelHandlerCallback {
    void run(ChannelHandlerContext handler);
}
