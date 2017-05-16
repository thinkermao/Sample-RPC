package net.hashcoding.samplerpc.common.handle;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.hashcoding.samplerpc.common.message.Command;
import net.hashcoding.samplerpc.common.utils.LogUtils;

/**
 * Created by MaoChuan on 2017/5/12.
 */
public class MessageEncoder extends MessageToByteEncoder<Command> {
    private static final String TAG = "MessageEncoder";

    @Override
    public void encode(ChannelHandlerContext ctx, Command cmd, ByteBuf out) {
        try {
            int length = cmd.length();
            out.writeInt(length);
            out.writeInt(cmd.getType());
            out.writeLong(cmd.getRequestId());
            if (cmd.getBody() != null)
                out.writeBytes(cmd.getBody());
        } catch (Exception e) {
            LogUtils.e(TAG, e);
            ctx.channel().close();
        }
    }
}