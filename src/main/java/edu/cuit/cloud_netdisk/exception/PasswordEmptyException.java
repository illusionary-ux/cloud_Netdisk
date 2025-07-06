package edu.cuit.cloud_netdisk.exception;

public class PasswordEmptyException extends BaseException{
    public PasswordEmptyException(){}
    public PasswordEmptyException(String message){
        super(message);
    }
}
