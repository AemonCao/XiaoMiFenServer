package me.aemon.Helper;

import jcifs.UniAddress;
import jcifs.smb.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

/**
 * @创建人 Aemon Cao
 * @创建时间 2018/12/5 18:34
 * @描述
 */
public class FileTransfer {
    // samba 地址
    public static String host = "192.168.1.20";

    // samba 登录名
    public static String username = "root";

    // samba 密码
    public static String password = "cao19960528";

    private FileTransfer() {
    }

    public static void downloadFileFromSamba(SmbFile remoteSmbFile, String localDir) throws SmbException {
        if (!remoteSmbFile.exists()) {
            System.out.println("Samba服务器远程文件不存在");
            return;
        }
        if ((localDir == null) || ("".equals(localDir.trim()))) {
            System.out.println("本地目录路径不可以为空");
            return;
        }

        InputStream in = null;
        OutputStream out = null;

        try {
            //获取远程文件的文件名,这个的目的是为了在本地的目录下创建一个同名文件
            String remoteSmbFileName = remoteSmbFile.getName();

            //本地文件由本地目录，路径分隔符，文件名拼接而成
            File localFile = new File(localDir + File.separator + remoteSmbFileName);

            // 如果路径不存在,则创建
            File parentFile = localFile.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }

            //打开文件输入流，指向远程的smb服务器上的文件，特别注意，这里流包装器包装了SmbFileInputStream
            in = new BufferedInputStream(new SmbFileInputStream(remoteSmbFile));
            //打开文件输出流，指向新创建的本地文件，作为最终复制到的目的地
            out = new BufferedOutputStream(new FileOutputStream(localFile));

            //缓冲内存
            byte[] buffer = new byte[1024];
            while (in.read(buffer) != -1) {
                out.write(buffer);
                buffer = new byte[1024];
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void uploadFileToSamba(String url, NtlmPasswordAuthentication auth, String localFilePath) throws MalformedURLException, SmbException {
        if ((localFilePath == null) || ("".equals(localFilePath.trim()))) {
            System.out.println("本地文件路径不可以为空");
            return;
        }

        //检查远程父路径，不存在则创建
        SmbFile remoteSmbFile = new SmbFile(url, auth);
        String parent = remoteSmbFile.getParent();
        SmbFile parentSmbFile = new SmbFile(parent, auth);
        if (!parentSmbFile.exists()) {
            parentSmbFile.mkdirs();
        }

        InputStream in = null;
        OutputStream out = null;

        try {
            File localFile = new File(localFilePath);

            //打开一个文件输入流执行本地文件，要从它读取内容
            in = new BufferedInputStream(new FileInputStream(localFile));

            //打开一个远程Samba文件输出流，作为复制到的目的地
            out = new BufferedOutputStream(new SmbFileOutputStream(remoteSmbFile));

            //缓冲内存
            byte[] buffer = new byte[1024];
            while (in.read(buffer) != -1) {
                out.write(buffer);
                buffer = new byte[1024];
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
