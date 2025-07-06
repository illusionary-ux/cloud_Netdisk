package edu.cuit.cloud_netdisk.exception;

/**
 * 无权限异常
 */
public class NoPermissionException extends BaseException {
    public NoPermissionException() {}
    public NoPermissionException(String message) {
        super(message);
    }
} 