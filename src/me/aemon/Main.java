package me.aemon;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.*;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbSession;
import me.aemon.Helper.*;

import com.alibaba.fastjson.*;

import javax.imageio.ImageIO;

public class Main {
    // 是否上传到 sm.ms
    public static boolean IsUpload = false;

    // 是否备份到 samba
    public static boolean IsBackup = true;

    // 每小时上传次数
    public static int HourlyUploads = 180;

    // 上传间隔(单位：秒)
    public static int UploadInterval = 3600 / HourlyUploads;

    // 拍摄延时（单位：毫秒）
    public static int ShootingDelay = 2500;

    // samba 地址
    public static String host = "192.168.1.20";

    // samba 登录名
    public static String username = "root";

    // samba 密码
    public static String password = "cao19960528";

    // 图片本地备份地址
    public static String ImgBackupPath = "/sda2/XiaoMiFen";

    public static void main(String[] args) throws Exception, IOException, UnknownHostException, SmbException {

        System.out.println("请输入你要操作的功能序号，并以回车结束：");
        System.out.println("1.定时拍摄照片；");
        System.out.println("2.合成视频。");
        char input = (char) System.in.read();
        if (input == '1') {
            //samba服务器上的文件
            UniAddress ua = UniAddress.getByName(host);
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(host, username, password);
            SmbSession.logon(ua, auth);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    String FileExtensions = "jpg",
                            FileName = Helper.getTime(),
                            Timestamp = Helper.getTimestamp().toString().substring(0, 10),
                            RootPath = System.getProperty("user.dir");

                    // 图片全路径
                    String ImgAllPath = RootPath + "/" + FileName + "." + FileExtensions;
                    // 压缩图片后的图片路径
                    String NewImgAllPath = RootPath + "/" + FileName + "S." + FileExtensions;
                    // 图片备份路径
                    String BackupPath = "";

                    int ImgWidth = 0, ImgHeight = 0, UploadStatus = -1, BackupStatus = 0;
                    long ImgSize = 0;
                    String StoreName = "", url = "", DeleteUrl = "", hash = "", ip = "", path = "", msg = "", strJson = "";

                    String strSql;

                    // 拼接拍摄命令
                    String ShootingCommand = "raspistill -t " + ShootingDelay + " -o " + FileName + "." + FileExtensions;
                    System.out.println(ShootingCommand);
                    Helper.exeCmd(ShootingCommand);

                    System.out.println("开始压缩图片");
                    // 生成压缩图片
                    try {
                        Helper.compressPic(ImgAllPath, NewImgAllPath);
                    } catch (Exception ex) {
                        System.out.println(ex.toString());
                    }

                    System.out.println("开始获取图片详情");
                    // 获取图片详细信息
                    File picture = new File(NewImgAllPath);
                    try {
                        BufferedImage sourceImg = ImageIO.read(new FileInputStream(picture));
                        ImgWidth = sourceImg.getWidth();
                        ImgHeight = sourceImg.getHeight();
                        ImgSize = picture.length();
                    } catch (Exception ex) {
                        System.out.println(ex.toString());
                    }

                    System.out.println("开始备份");
                    if (IsBackup) {
                        try {
                            BackupPath = "smb://" + host + ImgBackupPath + "/" + Helper.getTime("yyyy") + "/" + Helper.getTime("MM") + "/" + Helper.getTime("dd") + "/" + new File(NewImgAllPath).getName();
                            FileTransfer.uploadFileToSamba(BackupPath, auth, NewImgAllPath);
                            BackupStatus = 1;
                        } catch (Exception ex) {
                            System.out.println(ex.toString());
                            BackupStatus = -1;
                        }
                    } else
                        System.out.println("跳过备份");

                    System.out.println("开始上传");
                    if (IsUpload) {
                        // 上传并返回 Json
                        strJson = HttpUploadFile.formUpload(NewImgAllPath);
                        System.out.println(strJson);
                        // json 解析
                        JSONObject jsonObject = JSON.parseObject(strJson);
                        if (jsonObject.getString("code").equals("success")) {
                            JSONObject dataObject = JSON.parseObject(jsonObject.getString("data"));
                            StoreName = dataObject.getString("storename");
                            ImgWidth = dataObject.getIntValue("width");
                            ImgHeight = dataObject.getIntValue("height");
                            ImgSize = dataObject.getIntValue("size");
                            url = dataObject.getString("url");
                            DeleteUrl = dataObject.getString("delete");
                            Timestamp = dataObject.getLong("timestamp").toString();
                            hash = dataObject.getString("hash");
                            ip = dataObject.getString("ip");
                            path = dataObject.getString("path");
                            UploadStatus = 1;
                        } else if (jsonObject.getString("code").equals("error")) {
                            JSONObject dataObject = JSON.parseObject(jsonObject.getString("data"));
                            msg = dataObject.getString("msg");
                            UploadStatus = -1;
                        }
                    } else
                        System.out.println("跳过上传");

                    strSql = "INSERT INTO photo_tb ( photo_name, file_extensions, photo_store_name, size, width, height, `hash`, url, delete_url, msg, upload_status, delete_status, path, `timestamp`, ip, response, backup_url, backup_status ) VALUES ( '" + FileName + "." + FileExtensions + "', '" + FileExtensions + "', '" + StoreName + "', " + ImgSize + ", " + ImgWidth + ", " + ImgHeight + ", '" + hash + "', '" + url + "', '" + DeleteUrl + "', '" + msg + "', " + UploadStatus + ", 0, '" + path + "', " + Timestamp + ", '" + ip + "', '" + strJson + "', '" + BackupPath + "'," + BackupStatus + " )";

                    System.out.println(strSql);
                    System.out.println("插入数据库成功，新增了：" + DBHelper.uploadSql(strSql) + " 条数据。");

                    // 删除文件
                    System.out.println("删除文件");
                    String RmCommand = "rm " + FileName + "." + FileExtensions;
                    Helper.exeCmd(RmCommand);
                    System.out.println(RmCommand);
                    RmCommand = "rm " + FileName + "S." + FileExtensions;
                    Helper.exeCmd(RmCommand);
                    System.out.println(RmCommand);
                }
            };
            ScheduledExecutorService service = Executors
                    .newSingleThreadScheduledExecutor();
            service.scheduleAtFixedRate(runnable, 1, UploadInterval, TimeUnit.SECONDS);

        } else if (input == '2') {
            String imgPath = "D:\\XiaoMiFen\\2018\\12\\aa";
            File file = new File(imgPath);
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    System.out.println(files[i]);
                    VideoHelper.CompositeVideo(files[i].toString());
                }
            }
            // VideoHelper.CompositeVideo(imgPath + "\\07");
        }

    }
}
