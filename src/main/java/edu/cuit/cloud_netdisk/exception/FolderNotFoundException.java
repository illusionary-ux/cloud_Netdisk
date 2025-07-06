package edu.cuit.cloud_netdisk.exception;

public class FolderNotFoundException extends BaseException {
    public FolderNotFoundException() {}

    public FolderNotFoundException(String message) {
        super(message);
    }
}