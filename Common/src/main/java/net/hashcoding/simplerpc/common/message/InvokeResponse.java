package net.hashcoding.simplerpc.common.message;

/**
 * Created by MaoChuan on 2017/5/13.
 */
public class InvokeResponse {

    private Object response;
    private String throwReason;

    public InvokeResponse() {
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
