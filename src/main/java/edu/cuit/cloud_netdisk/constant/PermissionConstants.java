package edu.cuit.cloud_netdisk.constant;

/**
 * 权限常量
 */
public class PermissionConstants {
    // 公共权限
    public static final String PUBLIC_ACCESS = "publicaccess";
    public static final String ACCOUNT_LOGIN = "account:login";
    public static final String FILE_LIST = "file:list";
    public static final String FILE_PREVIEW = "file:preview";
    public static final String FILE_PUBLIC = "file:public";
    
    // 账户操作权限
    public static final String ACCOUNT_REGISTER = "account:register";
    public static final String ACCOUNT_PROFILE = "account:profile";
    public static final String ACCOUNT_UPDATE = "account:update";
    
    // 文件操作权限
    public static final String FILE_UPLOAD = "file:upload";
    public static final String FILE_DOWNLOAD = "file:download";
    public static final String FILE_DELETE = "file:delete";
    public static final String FILE_SHARE = "file:share";
    public static final String FILE_MOVE = "file:move";
    public static final String FILE_COPY = "file:copy";
    public static final String FILE_RENAME = "file:rename";
    
    // 存储管理权限
    public static final String STORAGE_VIEW = "storage:view";
    public static final String STORAGE_EXPAND = "storage:expand";
    public static final String STORAGE_QUOTA = "storage:quota";
    
    // 用户管理权限
    public static final String USER_MANAGE = "user:manage";
    public static final String FILE_MANAGE = "file:manage";
    public static final String ROLE_MANAGE = "role:manage";
    public static final String PERMISSION_MANAGE = "permission:manage";
    
    // 系统管理权限
    public static final String SYSTEM_SETTINGS = "system:settings";
    public static final String SYSTEM_LOG = "system:log";
    public static final String SYSTEM_MONITOR = "system:monitor";
    
    // 文件夹操作权限
    public static final String FOLDER_CREATE = "folder:create";
    public static final String FOLDER_DELETE = "folder:delete";
    public static final String FOLDER_RENAME = "folder:rename";
    public static final String FOLDER_MOVE = "folder:move";
    public static final String FOLDER_COPY = "folder:copy";
    public static final String FOLDER_SHARE = "folder:share";
    public static final String FOLDER_LIST = "folder:list";
    public static final String FOLDER_MANAGE = "folder:manage";
} 