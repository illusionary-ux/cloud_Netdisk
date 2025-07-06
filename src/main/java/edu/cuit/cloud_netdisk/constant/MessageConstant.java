package edu.cuit.cloud_netdisk.constant;

public class MessageConstant {
    // 异常定义
    public static final String ACCOUNT_NOT_FOUND = "账号不存在";
    public static final String PASSWORD_ERROR = "密码错误";
    public static final String VERIFICATION_CODE_ERROR = "验证码错误或过期";
    public static final String VIllegalArgumentException = "验证码不能为空";
    public static final String PASSWORD_EMPTY = "密码不能为空";
    public static final String UnsupportedLoginMethod = "不支持的登录方式";
    public static final String EmailIllegal = "邮箱格式错误";
    public static final String EmailAlreadyExist = "邮箱已经存在";
    public static final String UsernameLenthIllegal = "用户名长度至少为3位";
    public static final String UsernameAlreadyExist = "用户名已存在，请重新输入";
    
    // 文件相关异常消息
    public static final String FILE_NOT_FOUND = "要更新的文件不存在";
    public static final String FILE_NAME_EMPTY = "文件名不能为空";
    public static final String FILE_TYPE_EMPTY = "文件类型不能为空";
    public static final String FILE_STORAGE_PATH_EMPTY = "文件存储路径不能为空";
    public static final String FILE_UPLOAD_USER_EMPTY = "上传用户不能为空";
    public static final String FILE_UPLOAD_FAILED = "文件上传失败";
    public static final String FOLDER_NOT_FOUND = "文件夹不存在";
    public static final String FOLDER_NAME_EMPTY = "文件夹名称不能为空";
    public static final String FOLDER_NAME_DUPLICATE = "文件夹名称已存在";
    public static final String FOLDER_CREATOR_EMPTY = "创建者ID不能为空";
    public static final String FOLDER_PARENT_NOT_FOUND = "父文件夹不存在";
}
