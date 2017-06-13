package net.hashcoding.samplerpc.common.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by MaoChuan on 2017/5/14.
 */
public class TextUtils {
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String join(CharSequence separator, String... strings) {
        return Arrays.stream(strings)
                .collect(Collectors.joining(separator));
    }
}
