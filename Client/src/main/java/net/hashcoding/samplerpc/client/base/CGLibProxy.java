package net.hashcoding.samplerpc.client.base;

import net.hashcoding.samplerpc.client.Client;
import net.hashcoding.samplerpc.client.Proxy;
import net.hashcoding.samplerpc.common.message.InvokeResponse;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class CGLibProxy implements Proxy {
    public <T> T getInterface(Client client, Class<T> serviceInterface) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(serviceInterface);
        enhancer.setCallback(new CglibInterceptor(client, serviceInterface));
        Object enhancedObject = enhancer.create();
        return (T) enhancedObject;
    }

    /**
     * 搞个静态内部类来做Method的cglib代理
     */
    private static class CglibInterceptor implements MethodInterceptor {
        //首先判断所要代理的方法是通用方法，是的话就此返回此代理对象的相关内容
        private static Method hashCodeMethod;
        private static Method equalsMethod;
        private static Method toStringMethod;

        static {
            try {
                hashCodeMethod = Object.class.getMethod("hashCode");
                equalsMethod = Object.class.getMethod("equals", Object.class);
                toStringMethod = Object.class.getMethod("toString");
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodError(e.getMessage());
            }
        }

        /**
         * 加入rpc客户端和传入所调用服务的接口
         */
        private Client client;
        private Class<?> serviceInterface;

        public CglibInterceptor(Client client, Class<?> serviceInterface) {
            this.client = client;
            this.serviceInterface = serviceInterface;
        }

        /**
         * 针对这几个方法做相应的策略
         *
         * @param proxy
         * @return
         */
        private int proxyHashCode(Object proxy) {
            return System.identityHashCode(proxy);
        }

        private boolean proxyEquals(Object proxy, Object other) {
            return (proxy == other);
        }

        private String proxyToString(Object proxy) {
            return proxy.getClass().getName() + '@' + Integer.toHexString(proxy.hashCode());
        }

        public Object intercept(Object o, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            //先对方法进行判断是否是通用方法，假如都不是，最后再通过client来调用
            if (hashCodeMethod.equals(method)) {
                return proxyHashCode(proxy);
            }
            if (equalsMethod.equals(method)) {
                return proxyEquals(proxy, args[0]);
            }
            if (toStringMethod.equals(method)) {
                return proxyToString(proxy);
            }
            InvokeResponse response = client.sendMessage(serviceInterface, method, args);
            Object result = response.getResponse();
            if (result == null) {
                throw new RuntimeException(response.getThrowReason());
            }
            return result;
        }
    }
}