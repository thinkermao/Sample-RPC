package net.hashcoding.samplerpc.common;

/**
 * Created by MaoChuan on 2017/5/13.
 */
public class Response {

    private Object response;
    private String throwReason;

    public Response() {
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public String getThrowReason() {
        return throwReason;
    }

    public void setThrowReason(String reason) {
        this.throwReason = reason;
    }
}
