package edu.cuit.cloud_netdisk.exception;

public class BaseException extends RuntimeException {
    public BaseException(){}
    public BaseException(String message){
        super(message);
    }
}
