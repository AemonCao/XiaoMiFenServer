package me.aemon;

/**
 * @创建人 Aemon Cao
 * @创建时间 2019/1/12 19:28
 * @描述
 */
public class Constants {
    // 图床部分常量

    // 是否上传到 sm.ms 图床
    public static final boolean IS_UPLOAD_SMMS = false;

    // SMMS图床每小时上传次数
    public static final int SMMS_HOURLY_UPLOADS = 180;

    // 上传间隔(单位：秒)
    public static final int SMMS_UPLOAD_INTERVAL = 3600 / SMMS_HOURLY_UPLOADS;

    // smms 上传api地址
    public static final String SMMS_UPLOAD_URL = "https://sm.ms/api/upload";

    // samba 部分常量

    // 是否备份到 samba
    public static final boolean IS_BACKUP_SAMBA = true;

    // samba 服务器地址
    public static final String SAMBA_SERVER_HOST = "192.168.1.20";

    // 图片 samba 备份地址
    public static final String SAMBA_IMG_BACKUP_PATH = "/sda2/XiaoMiFen";

    // samba 登录名
    public static final String SAMBA_USERNAME = "";

    // samba 密码
    public static final String SAMBA_PASSWORD = "";

    // 数据库部分常量

    // JDBC 驱动
    public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    // 树莓派地址
    public static final String RASPBERRY_HOST = "192.168.1.100";

    // 数据库连接地址
    public static final String DB_URL = "jdbc:mysql://" + RASPBERRY_HOST + ":3306/XiaoMiFen";

    // 数据库登录名
    public static final String DB_USERNAME = "";

    // 数据库密码
    public static final String DB_PASSWORD = "";

    // server 酱部分常量

    // server 酱 SCKEY
    public static final String SERVER_CHAN_SCKEY = "";

    // server 酱请求路径
    public static final String SERVER_CHAN_URL = "https://sc.ftqq.com/" + SERVER_CHAN_SCKEY + ".send";

    // 其他常量

    // 拍摄延时（单位：毫秒）
    public static final int SHOOTING_DELAY = 2500;
}
