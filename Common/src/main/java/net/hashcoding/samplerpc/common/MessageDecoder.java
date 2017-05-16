package net.hashcoding.samplerpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import net.hashcoding.samplerpc.common.utils.LogUtils;

import java.nio.ByteBuffer;

/**
 * Created by MaoChuan on 2017/5/12.
 */

public class MessageDecoder extends LengthFieldBasedFrameDecoder {

    private static final String TAG = "MessageDecoder";

    public MessageDecoder() {
        super(65536000, 0, 4, 0, 4);
    }

    @Override
    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = null;
        try {
            frame = (ByteBuf) super.decode(ctx, in);
            if (null == frame) {
                return null;
            }

            ByteBuffer byteBuffer = frame.nioBuffer();
            int length = byteBuffer.limit();
            int type = byteBuffer.getInt();
            long requestId = byteBuffer.getLong();
            byte[] bodyData = null;
            if ((length - 12) > 0) {
                bodyData = new byte[length - 12];
                byteBuffer.get(bodyData);
            }
            Command cmd = new Command(type, bodyData);
            cmd.setRequestId(requestId);
            return cmd;
        } catch (Exception e) {
            ctx.channel().close().addListener((ChannelFutureListener)
                    future -> LogUtils.e(TAG, e));
        } finally {
            if (null != frame) {
                frame.release();
            }
        }
        return null;
    }
}