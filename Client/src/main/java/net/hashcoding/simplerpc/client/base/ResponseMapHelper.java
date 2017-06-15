package net.hashcoding.simplerpc.client.base;

import net.hashcoding.simplerpc.common.Promise;
import net.hashcoding.simplerpc.common.message.InvokeResponse;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by MaoChuan on 2017/5/13.
 */
public class ResponseMapHelper {
    public static ConcurrentHashMap<Long, Promise<InvokeResponse>>
            responses = new ConcurrentHashMap<>();
}
