package net.hashcoding.samplerpc.common.utils;

import java.lang.reflect.Array;
import java.util.HashMap;

/**
 * Created by MaoChuan on 2017/5/15.
 */
public class ReflectUtils {
    private static final HashMap<String, Class> baseTypeMap;

    static {
        baseTypeMap = new HashMap<>();
        baseTypeMap.put("byte", byte.class);
        baseTypeMap.put("short", short.class);
        baseTypeMap.put("int", int.class);
        baseTypeMap.put("long", long.class);
        baseTypeMap.put("char", char.class);
        baseTypeMap.put("float", float.class);
        baseTypeMap.put("double", double.class);
        baseTypeMap.put("boolean", boolean.class);
        baseTypeMap.put("void", void.class);
    }

    public static Class forName(String name) throws ClassNotFoundException {
        if (name.endsWith("[]")) {// 支持可变参数和自定义类的数组
            String originName = name.substring(0, name.length() - 2);
            Class clazz = forName(originName);
            return Array.newInstance(clazz, 0).getClass();
        } else if (baseTypeMap.containsKey(name)) {
            return baseTypeMap.get(name);
        } else {
            return Class.forName(name);
        }
    }
}
