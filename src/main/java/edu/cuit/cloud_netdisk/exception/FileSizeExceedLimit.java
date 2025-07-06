package edu.cuit.cloud_netdisk.exception;

public class FileSizeExceedLimit extends BaseException {
    public FileSizeExceedLimit() {}
    public FileSizeExceedLimit(String message) {
        super(message);
    }
}
