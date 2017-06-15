package net.hashcoding.simplerpc.common.utils;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by MaoChuan on 2017/5/12.
 */
public class LogUtils {
    private final static Logger logger;

    static {
        logger = LogManager.getLogger();
    }

    // trace
    public static void t(String TAG, String message) {
        logger.trace(TAG + ": " + message);
    }

    public static void t(String TAG, Throwable e) {
        t(TAG, e.toString());
    }

    public static <T> void t(Class<T> clazz, String message) {
        t(clazz.getName(), message);
    }

    public static <T> void t(Class<T> clazz, Throwable e) {
        t(clazz.getName(), e.toString());
    }

    // information
    public static void i(String TAG, String message) {
        logger.info(TAG + ": " + message);
    }

    public static <T> void i(Class<T> clazz, String message) {
        i(clazz.getName(), message);
    }

    // warning
    public static void w(String TAG, String message) {
        logger.warn(TAG + ": " + message);
    }

    public static void w(String TAG, Throwable e) {
        w(TAG, e.toString());
    }

    public static <T> void w(Class<T> clazz, String message) {
        w(clazz.getName(), message);
    }

    public static <T> void w(Class<T> clazz, Throwable e) {
        w(clazz.getName(), e.toString());
    }

    // debug
    public static void d(String TAG, String message) {
        logger.debug(TAG + ": " + message);
    }

    public static void d(String TAG, Throwable e) {
        d(TAG, e.toString());
    }

    public static <T> void d(Class<T> clazz, String message) {
        d(clazz.getName(), message);
    }

    public static <T> void d(Class<T> clazz, Throwable e) {
        d(clazz.getName(), e.toString());
    }

    // error
    public static void e(String TAG, String message) {
        logger.error(TAG + ": " + message);
    }

    public static void e(String TAG, Throwable ee) {
        e(TAG, ee.toString());
    }

    public static <T> void e(Class<T> clazz, String message) {
        e(clazz.getName(), message);
    }

    public static <T> void e(Class<T> clazz, Throwable ee) {
        e(clazz.getName(), ee.toString());
    }

    // fatal
    public static void f(String TAG, String message) {
        logger.fatal(TAG + ": " + message);
    }

    public static void f(String TAG, Throwable e) {
        f(TAG, e.toString());
    }

    public static <T> void f(Class<T> clazz, String message) {
        f(clazz.getName(), message);
    }

    public static <T> void f(Class<T> clazz, Throwable e) {
        f(clazz.getName(), e.toString());
    }
}
