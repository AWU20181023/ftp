package com.example.demo.utls;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;

public class FtpUtils {

    private static FTPClient ftpClient;
    //从本地文件获取各种属性
    private static String ftpIP = "192.168.136.147";
    private static Integer ftpPort = 21;
    private static String ftpUserName = "zhangsan";
    private static String ftpPassword = "123456";
    private static String ftpEncode = "UTF-8";
    private static String localPath = "/usr/local/ftp2";

    public static synchronized boolean connectServer() {
        ftpClient = new FTPClient();
        ftpClient.setControlEncoding(ftpEncode);//解决上传文件时文件名乱码
        int reply = 0;
        try {
            // 连接至服务器
            ftpClient.connect(ftpIP, ftpPort);
            // 登录服务器
            ftpClient.login(ftpUserName, ftpPassword);
            //登陆成功，返回码是230
            reply = ftpClient.getReplyCode();
            // 判断返回码是否合法
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void main(String[] args) {
        boolean b = connectServer();
//        if (existFile("1547361072085/")) {
//            try {
//                ftpClient.removeDirectory("1547360230682/");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        try {
            InputStream inputStream = new FileInputStream(new File("F:\\作品\\github\\demo\\uklili.docx"));
            Long current = System.currentTimeMillis();
            createSubfolder("/", current + "");
            upload(inputStream, "uklili.docx", "/" + current);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        closeClient();
    }

    //判断ftp服务器文件是否存在
    public static boolean existFile(String path) {
        boolean flag = false;
        FTPFile[] ftpFileArr;
        try {
            ftpFileArr = ftpClient.listFiles(path);
            if (ftpFileArr.length > 0) {
                flag = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }

    //删除ftp文件
    public static synchronized boolean deleteFile(String pathname, String filename) {
        boolean flag = false;
        try {
            System.out.println("开始删除文件");
            //切换FTP目录
            ftpClient.changeWorkingDirectory(pathname);
            ftpClient.dele(filename);
            ftpClient.logout();
            flag = true;
            System.out.println("删除文件成功");
        } catch (Exception e) {
            System.out.println("删除文件失败");
            e.printStackTrace();
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return flag;
    }

    //从FTP server下载到本地文件夹
    public static synchronized boolean download(String path) {
        boolean flag = false;
        FTPFile[] fs;
        try {
            fs = ftpClient.listFiles(path);
            //1、遍历FTP路径下所有文件
            for (FTPFile file : fs) {
                File localFile = new File(localPath + "/" + file.getName());
                //2、保存到本地
                OutputStream os = new FileOutputStream(localFile);
                //retrieveFile(FTP服务端的源路径),这个路径要和listFiles的路径一致
                ftpClient.retrieveFile("aaa/" + file.getName(), os);
                //3、删除FTP中的上面保存的文件
                //循环外关闭，读一个关闭一次挺慢的
            }
            flag = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }


    public static synchronized boolean upload(InputStream inputStream, String fileName, String path) {
        try {

            //切换工作路径，设置上传的路径
            ftpClient.changeWorkingDirectory(path);
            //设置1M缓冲
            ftpClient.setBufferSize(1024);
            // 设置被动模式
            ftpClient.enterLocalPassiveMode();
            // 设置以二进制方式传输
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
             /*
              * 第一个参数：服务器端文档名
              * 第二个参数：上传文档的inputStream
              * 在前面设置好路径，缓冲，编码，文件类型后，开始上传
              */
            ftpClient.storeFile(fileName, inputStream);
            inputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            closeClient();
        }
    }

    public static boolean removeDirectory(String pathname) {
        try {
            return ftpClient.removeDirectory(pathname);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean checkSubfolder(String path, String subfolderName) {
        try {
            //切换到FTP根目录
            ftpClient.changeWorkingDirectory(path);
            //查看根目录下是否存在该文件夹
            InputStream is = ftpClient.retrieveFileStream(new String(subfolderName.getBytes("UTF-8")));
            if (is == null || ftpClient.getReplyCode() == FTPReply.FILE_UNAVAILABLE) {
                //若不存在该文件夹，则创建文件夹
                return createSubfolder(path, subfolderName);
            }
            is.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static synchronized boolean createSubfolder(String path, String subfolderName) {
        try {
            ftpClient.changeWorkingDirectory(path);
            ftpClient.makeDirectory(subfolderName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    /**
     * 断开与远程服务器的连接
     */
    public static void closeClient() {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
