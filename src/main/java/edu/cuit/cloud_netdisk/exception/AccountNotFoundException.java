package edu.cuit.cloud_netdisk.exception;

/**
 * 账号不存在异常
 */
public class AccountNotFoundException extends BaseException {
    public AccountNotFoundException() {}
    public AccountNotFoundException(String message) {
        super(message);
    }
}
