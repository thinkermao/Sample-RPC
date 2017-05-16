package net.hashcoding.samplerpc.common.message;

/**
 * Created by MaoChuan on 2017/5/12.
 */

import java.io.Serializable;

public class Request implements Serializable {
    private String interfaceName;
    private String methodName;
    private String[] parameterTypes;
    private Object[] arguments;
    private boolean extData;
    private Object extObject;

//    public static Request factory(Command command) {
//        ConditionUtils.checkNotNull(command.getBody());
//        return factory(command.getBody());
//    }
//
//    public static Request factory(byte [] bytes) {
//        return JSON.parseObject(bytes, Request.class);
//    }
//
//    public byte[] toBytes() {
//        return JSON.toJSONBytes(this, SerializerFeature.WriteClassName);
//    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public boolean isExtData() {
        return extData;
    }

    public void setExtData(boolean hasExtData) {
        this.extData = hasExtData;
    }

    public Object getExtObject() {
        return extObject;
    }

    public void setExtObject(Object extObject) {
        this.extObject = extObject;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(String[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }
}