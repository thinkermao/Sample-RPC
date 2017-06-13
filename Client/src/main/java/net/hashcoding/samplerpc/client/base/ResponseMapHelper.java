package net.hashcoding.samplerpc.client.base;

import net.hashcoding.samplerpc.common.Promise;
import net.hashcoding.samplerpc.common.message.InvokeResponse;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by MaoChuan on 2017/5/13.
 */
public class ResponseMapHelper {
    public static ConcurrentHashMap<Long, Promise<InvokeResponse>>
            responses = new ConcurrentHashMap<>();
}
