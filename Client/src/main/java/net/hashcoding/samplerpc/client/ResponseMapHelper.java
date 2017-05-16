package net.hashcoding.samplerpc.client;

import net.hashcoding.samplerpc.common.Promise;
import net.hashcoding.samplerpc.common.Response;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by MaoChuan on 2017/5/13.
 */
public class ResponseMapHelper {
    public static ConcurrentHashMap<Long, Promise<Response>> responses
            = new ConcurrentHashMap<>();
}
