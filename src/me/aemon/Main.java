package me.aemon;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbSession;
import me.aemon.Helper.*;

import com.alibaba.fastjson.*;
import me.aemon.Helper.Database.DBHelper;
import me.aemon.Helper.Http.HttpHelper;
import me.aemon.Helper.Http.HttpUploadFile;

import javax.imageio.ImageIO;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("请输入你要操作的功能序号，并以回车结束：");
        System.out.println("1.定时拍摄照片；");
        System.out.println("2.合成视频。");
        char input = (char) System.in.read();
        if (input == '1') {
            //samba服务器上的文件
            UniAddress ua = UniAddress.getByName(Constants.SAMBA_SERVER_HOST);
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(Constants.SAMBA_SERVER_HOST, Constants.SAMBA_USERNAME, Constants.SAMBA_PASSWORD);
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
                    String ShootingCommand = "raspistill -t " + Constants.SHOOTING_DELAY + " -o " + FileName + "." + FileExtensions;
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
                    if (Constants.IS_BACKUP_SAMBA) {
                        try {
                            BackupPath = "smb://" + Constants.SAMBA_SERVER_HOST + Constants.SAMBA_IMG_BACKUP_PATH + "/" + Helper.getTime("yyyy") + "/" + Helper.getTime("MM") + "/" + Helper.getTime("dd") + "/" + new File(NewImgAllPath).getName();
                            FileTransfer.uploadFileToSamba(BackupPath, auth, NewImgAllPath);
                            BackupStatus = 1;
                        } catch (Exception ex) {
                            System.out.println(ex.toString());
                            BackupStatus = -1;
                        }
                    } else
                        System.out.println("跳过备份");

                    System.out.println("开始上传");
                    if (Constants.IS_UPLOAD_SMMS) {
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
            service.scheduleAtFixedRate(runnable, 1, Constants.SMMS_UPLOAD_INTERVAL, TimeUnit.SECONDS);

        } else if (input == '2') {
            String imgPath = "D:\\XiaoMiFen\\2018\\12\\aa";
            File file = new File(imgPath);
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    System.out.println(files[i]);
                    VideoHelper.CompositeVideo(files[i].toString());
                    HttpHelper.SendWeChatMessage("视频已经合成好啦！","`"+files[i]+"` 已经拼接完成。");
                }
            }
            // VideoHelper.CompositeVideo(imgPath + "\\07");
        }
    }
}
