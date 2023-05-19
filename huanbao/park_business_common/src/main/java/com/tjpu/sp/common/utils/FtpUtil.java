package com.tjpu.sp.common.utils;



import com.tjpu.pk.common.utils.DataFormatUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import java.io.*;
import java.net.SocketException;


public class FtpUtil {
    private static String ip;
    private static String post;
    private static String userName;
    private static String password;

    static {
        ip = DataFormatUtil.parseProperties("ftp.ip");
        post = DataFormatUtil.parseProperties("ftp.post");
        userName = DataFormatUtil.parseProperties("ftp.username");
        password = DataFormatUtil.parseProperties("ftp.password");
    }

    public static FTPClient connectFtpServer() throws SocketException {
        if (StringUtils.isNotBlank(ip) &&
                StringUtils.isNotBlank(post) &&
                StringUtils.isNotBlank(userName)
                && StringUtils.isNotBlank(password)) {
            FTPClient ftpClient = new FTPClient();


            ftpClient.setDefaultTimeout(10 * 60 * 1000);
            ftpClient.setConnectTimeout(10 * 60 * 1000);
            ftpClient.setDataTimeout(10 * 60 * 1000);
           // ftpClient.setSoTimeout(60 * 1000);
            ftpClient.setBufferSize(1024 * 1024 * 32);
            ftpClient.setControlKeepAliveTimeout(3000);
            ftpClient.setControlKeepAliveReplyTimeout(3000);
            //ftpClient.setKeepAlive(true);
            ftpClient.setRemoteVerificationEnabled(false);

            ftpClient.setControlEncoding("utf-8");//设置ftp字符集
            ftpClient.enterLocalPassiveMode();//设置被动模式，文件传输端口设置
            try {
                ftpClient.connect(ip, Integer.parseInt(post));
                ftpClient.login(userName, password);
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);//设置文件传输模式为二进制，可以保证传输的内容不会被改变
                int replyCode = ftpClient.getReplyCode();
                if (!FTPReply.isPositiveCompletion(replyCode)) {
                    ftpClient.disconnect();
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return ftpClient;
        } else {
            return null;
        }
    }


}
