package net.hashcoding.samplerpc.common.message;

import net.hashcoding.samplerpc.common.utils.ConditionUtils;
import net.hashcoding.samplerpc.common.utils.SerializeUtils;

import java.util.concurrent.atomic.AtomicLong;

public class Command {
    // 注册服务端
    public final static int REGISTER_SERVER = 1;
    // 取消注册
    public final static int UNREGISTER_SERVER = 2;
    // 调用方法
    public final static int INVOKE_REQUEST = 3;
    // 方法返回
    public final static int INVOKE_RESPONSE = 4;
    // 获取服务列表
    public final static int GET_SERVER_LIST = 5;
    public final static int GET_SERVER_LIST_RESPONSE = 6;
    // Heart beat
    public final static int HEART_BEAT_REQUEST = 7;
    public final static int HEART_BEAT_RESPONSE = 8;

    public final static int SPLIT_MESSAGE = 9;
    public final static int SPLIT_MESSAGE_DONE = 10;

    private static AtomicLong IDS = new AtomicLong(0);

    private int type;
    private long requestId;

    // 存放具体的方法调用信息、调用结果等
    private byte[] body;

    public Command(int type) {
        this.type = type;
        this.body = null;
        this.requestId = IDS.incrementAndGet();
    }

    public Command(int type, byte[] body) {
        this.type = type;
        this.body = body;
        this.requestId = IDS.incrementAndGet();
    }

    public Command(int type, Object object) {
        this.type = type;
        this.body = SerializeUtils.serialize(object);
        this.requestId = IDS.incrementAndGet();
    }

    public static Command heartBeatRequest() {
        return new Command(Command.HEART_BEAT_REQUEST);
    }

    // command 长度 = type(4) + requestId(8) + body.length
    public int length() {
        return 12 + (body == null ? 0 : body.length);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void setBody(Object o) {
        this.body = SerializeUtils.serialize(o);
    }

    public <T> T factoryFromBody() {
        ConditionUtils.checkNotNull(body);
        return SerializeUtils.deserialize(body);
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }
}