package edu.cuit.cloud_netdisk.exception;

public class StorageExceededException extends BaseException{
    public StorageExceededException(){}
    public StorageExceededException(String message){
        super(message);
    }

}
