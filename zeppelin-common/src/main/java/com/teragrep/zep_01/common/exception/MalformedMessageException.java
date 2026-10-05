package com.teragrep.zep_01.common.exception;

public class MalformedMessageException extends RuntimeException {
    public MalformedMessageException(String message){
        super(message);
    }

    public MalformedMessageException(String message, Throwable cause){
        super(message, cause);
    }
}
