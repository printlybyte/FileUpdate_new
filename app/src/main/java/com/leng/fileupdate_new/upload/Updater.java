/**
 * @Title: Updater.java
 * @Package com.hutu.localfile.manager
 * @Description: TODO
 * @author Long Li
 * @date 2015-5-6 下午4:38:05
 * @version V1.0
 */
package com.leng.fileupdate_new.upload;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.leng.fileupdate_new.contrl.CabackPv;
import com.leng.fileupdate_new.upload.uploadUtil.PreferenceUtil;
import com.leng.fileupdate_new.upload.uploadUtil.httpUtils;
import com.leng.fileupdate_new.utils.FileUtils;
import com.leng.fileupdate_new.utils.SharedPreferencesUtils;
import com.leng.fileupdate_new.utils.SpUtil;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.SocketException;

/**
 * TODO<上传器>
 *
 * @author Long Li
 * @data: 2015-5-6 下午4:38:05
 * @version: V1.0
 */
public class Updater {

    // 枚举类UploadStatus代码
    public enum UploadStatus {
        Create_Directory_Fail, // 远程服务器相应目录创建失败
        Create_Directory_Success, // 远程服务器闯将目录成功
        Upload_New_File_Success, // 上传新文件成功
        Upload_New_File_Failed, // 上传新文件失败
        File_Exits, // 文件已经存在
        Remote_Bigger_Local, // 远程文件大于本地文件
        Upload_From_Break_Success, // 断点续传成功
        Upload_From_Break_Failed, // 断点续传失败
        Delete_Remote_Faild; // 删除远程文件失败
    }

    //    public enum FtpState {
//        INIT, // 初始化 0
//        PAUSE, // 暂停 1
//        UPDATING, // 上传中 2
//    }
    private String TAG = "qweqweqwe";

    private String FtpHostAdress = null;


    public FTPClient ftpClient = new FTPClient();
    private String LOCAL_CHARSET = "GBK";
    private TestBean bean;
    private Context context;
    // FTP协议里面，规定文件名编码为iso-8859-1
    private String SERVER_CHARSET = "ISO-8859-1";
    private CabackPv cp;
    private Handler mHandler;


    private String FtpUserName = "FTPuser"; // FTP 用户名 密码
    private String FtpUserPwd = "Ftp1029384756";
//    private int FtpHostPort = 21; // 端口

    private int FtpHostPort; // 端口
    private String FtpHostPortSTR; // 端口

    public Updater(Context mContext, TestBean bean, Handler mHandler) {
        this.mHandler = mHandler;
        this.bean = bean;
        this.context = mContext;
        cp = (CabackPv) mContext;
    }

    // 开始上传一个文件
    public void StartUpdate() {
        FtpHostAdress = (String) SharedPreferencesUtils.getParam(context, "ftpIpX", "218.246.35.197");
        FtpHostPortSTR = (String) SharedPreferencesUtils.getParam(context, "ftpPortX", "21");
        FtpHostPort = Integer.parseInt(FtpHostPortSTR);
        Log.i(TAG, "ip： " + FtpHostAdress + "  端口：" + FtpHostPort + "  用户名： " + FtpUserName + " 密码 ：" + FtpUserPwd);

        try {
            if (connect()) {
                UploadStatus mStatus = upload();
                Log.i(TAG, "ftp result status is " + mStatus);
                if ((UploadStatus.Upload_New_File_Success == mStatus) // 上传成功
                        || (UploadStatus.Upload_From_Break_Success == mStatus)) {
//					cFtp.sendMsg(100);
                } else if ((UploadStatus.Upload_From_Break_Failed == mStatus) // 上传失败
                        || (UploadStatus.Upload_New_File_Failed == mStatus)) {
//					cFtp.setFtpState(ContinueFtp.FtpState.INIT);
                } else if (UploadStatus.File_Exits == mStatus) { // 移除文件到已上传列表
//					cFtp.sendMsg(100);
                } else {
                    Log.i(TAG, "status is " + mStatus);
                }
            } else {
                Toast.makeText(context, "连接失败,请检查配置是否正确", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // pause(); //pase state
            Log.i(TAG, "连接一场了");
        } catch (NullPointerException e) {
            Log.i("qweqweasd", "asdasdasdasdasd");
            // TODO: handle exception
            e.printStackTrace();

            // pause();
        } finally {
//			cFtp.setFtpState(ContinueFtp.FtpState.INIT);
            try {
                disconnect();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }


    // 暂停上传
    public void pause() {
        bean.setUpLoadStatus("2");
        Log.i(TAG, "*********************************" + bean.hashCode() + bean.getLocfileName());
        try {
            disconnect();
        } catch (Exception e) { // IOException
            // TODO Auto-generated catch block
            System.out.println("暂停失败");
            e.printStackTrace();

        }
    }

    /**
     * @return
     * @throws IOException
     * @连接到Ftp
     */
    public boolean connect() throws IOException {
        // String LOCAL_CHARSET;


        // ftpClient.setControlEncoding("utf-8");// 设置字符集，必须在connect之前设置
        try {
            ftpClient.connect(FtpHostAdress, FtpHostPort);// 地址和端口
        } catch (SocketException e) {
            Log.i(TAG, "连接失败 " + e);
            return false;
        } catch (IOException e) {
            // TODO: handle exception
            Log.i(TAG, "ftp 连接失败 " + e);
            return false;
        }


        if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            if (ftpClient.login(FtpUserName, FtpUserPwd)) {
                Log.i(TAG, "connect FTP service success");
                if (FTPReply.isPositiveCompletion(ftpClient.sendCommand(
                        "OPTS UTF8", "ON"))) {// 开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码，否则就使用本地编码（GBK）.
                    LOCAL_CHARSET = "UTF-8";
                    Log.i(TAG, "Ftp Mode is UTF-8");
                }
                // ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding(LOCAL_CHARSET);
                ftpClient.enterLocalPassiveMode();// 设置被动模式
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);// 设置传输的模式
                return true;
            }
        }
        disconnect();
        return false;
    }

    /**
     * 断开与远程服务器的连接
     *
     * @throws IOException
     */
    public void disconnect() throws IOException {
        if (ftpClient.isConnected()) {
            ftpClient.disconnect();
            System.out.println("if connected try dis connect");
        }
    }


    /**
     * @return
     * @throws IOException
     * @上传文件
     */
    public UploadStatus upload() throws IOException {
        UploadStatus result;
        // 对远程目录的处理
        String localFilePath = bean.getLocfilepath();
        String remoteFileName = bean.getRemotefilepath();


        String creatType = bean.getCrateFileType();

        Log.i(TAG, "" + localFilePath + "=====" + remoteFileName);
        //创建远程文件

        String remoteFilePath = creatType + remoteFileName;
        CreateDirecroty(remoteFilePath, ftpClient);


        FTPFile[] files = ftpClient.listFiles(remoteFileName);

        if (files.length == 1) {
            long remoteSize = files[0].getSize();
            File f = new File(localFilePath);
            long localSize = f.length();
            if (remoteSize == localSize) {
//				Log.i(TAG, "file name is " + file.getName()
//						+ " progress is " + mBxFile.getFileProgress());
//				Log.i(TAG, "服务器中文件等于要上传文件，所以不上传");
//                sendMsg(100);
                return UploadStatus.File_Exits;
            } else if (remoteSize > localSize) {
                Log.i(TAG, "服务器中文件大于要上传文件，所以不上传");
                return UploadStatus.Remote_Bigger_Local;
            }

            // 尝试移动文件内读取指针,实现断点续传
            result = uploadFile2(remoteFileName, f, ftpClient, remoteSize);

            // 如果断点续传没有成功，则删除服务器上文件，重新上传
            if (result == UploadStatus.Upload_From_Break_Failed) {
                if (!ftpClient.deleteFile(remoteFileName)) {
                    return UploadStatus.Delete_Remote_Faild;
                }
                result = uploadFile2(remoteFileName, f, ftpClient, 0);
            }
        } else {
            result = uploadFile2(remoteFileName, new File(localFilePath), ftpClient, 0);
        }

        if ((UploadStatus.Upload_New_File_Success == result) // 上传成功
                || (UploadStatus.Upload_From_Break_Success == result)) {
            //
            int httpNums = 1;
            httpUtils mHttpUtils = new httpUtils(context, bean.getCrateFileTypenums(),
                    localFilePath, remoteFileName);
            while (httpNums <= 3) {

                mHttpUtils.getHttpRequest();

                try {
                    Thread.sleep(3000);
                    if (mHttpUtils.getHttpResult() == 1) {
                        removeFileName(localFilePath);
                        break;
                    } else if (mHttpUtils.getHttpResult() == 0) {
                        httpNums++;
                    } else {
                        Thread.sleep(3000);
                        if (mHttpUtils.getHttpResult() == 1) {
                            removeFileName(localFilePath);
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }

        return result;
    }


    public static void removeFileName(String oldFile) {
        PreferenceUtil mPrefManager = PreferenceUtil.getInstance(null, null);
        if (mPrefManager != null) {
            mPrefManager.remove(oldFile);
        }
    }

    /**
     * 递归创建远程服务器目录
     *
     * @param remote    远程服务器文件绝对路径
     * @param ftpClient FTPClient 对象
     * @return 目录创建是否成功
     * @throws IOException
     */
    public UploadStatus CreateDirecroty(String remote, FTPClient ftpClient)
            throws IOException {
        UploadStatus status = UploadStatus.Create_Directory_Success;
        String directory = remote.substring(0, remote.lastIndexOf("/") + 1);
        Log.i(TAG, "目录名：" + directory);
        if (!directory.equalsIgnoreCase("/")
                && !ftpClient.changeWorkingDirectory(new String(directory
                .getBytes(LOCAL_CHARSET), SERVER_CHARSET))) {
            // 如果远程目录不存在，则递归创建远程服务器目录
            int start = 0;
            int end = 0;
            if (directory.startsWith("/")) {
                start = 1;
            } else {
                start = 0;
            }
            end = directory.indexOf("/", start);
            while (true) {
                String subDirectory = new String(remote.substring(start, end)
                        .getBytes(LOCAL_CHARSET), SERVER_CHARSET);
                System.out.println("subDirectory = " + subDirectory);
                if (!ftpClient.changeWorkingDirectory(subDirectory)) {
                    if (ftpClient.makeDirectory(subDirectory)) {
                        ftpClient.changeWorkingDirectory(subDirectory);
                    } else {
                        Log.e(TAG, "创建目录失败");
                        return UploadStatus.Create_Directory_Fail;
                    }
                }

                start = end + 1;
                end = directory.indexOf("/", start);

                // 检查所有目录是否创建完毕
                if (end <= start) {
                    break;
                }
            }
        }
        return status;
    }

    /**
     * @param process 上传过程中文件进程百分比
     */
    public void sendMsg(int process) {


        synchronized (mHandler) {
            bean.setUploadProgress(process);
            if (99 < process) {
                bean.setUpLoadStatus("3");
            } else {
                bean.setUpLoadStatus("1");
            }
            Message message = new Message();
            message.what = 100;
            message.obj = bean;
            mHandler.sendMessage(message);

        }

    }

    /** */
    /**
     * 上传文件到服务器,新上传和断点续传
     *
     * @param remoteFile 远程文件名，在上传之前已经将服务器工作目录做了改变
     * @param localFile  本地文件 File句柄，绝对路径
     * @param process    需要显示的处理进度步进值
     * @param ftpClient  FTPClient 引用
     * @return
     * @throws IOException
     */
    public UploadStatus uploadFile2(String remoteFile, File localFile, FTPClient ftpClient, long remoteSize) throws IOException {
//        fileStatusBeanMap.put(localFile.getName(), bean);

        //对象序列化保存到本地
        SpUtil.putObject(context, localFile.getName(), bean);
        Log.i(TAG, localFile.getName() + "保存对象的名字");

        UploadStatus status;
        //显示进度的上传
        long step = localFile.length() / 100;
        long process = 0;
        long localreadbytes = 0L;
        RandomAccessFile raf = new RandomAccessFile(localFile, "r");
        OutputStream out = ftpClient.appendFileStream(new String(remoteFile.getBytes("UTF-8"), "iso-8859-1"));
        //断点续传
        if (remoteSize > 0) {
            ftpClient.setRestartOffset(remoteSize);
            process = remoteSize / step;
            raf.seek(remoteSize);
            localreadbytes = remoteSize;
        }
        byte[] bytes = new byte[1024];
        int c;


        while ((c = raf.read(bytes)) != -1 && !bean.getUpLoadStatus().equals("2")) {
            out.write(bytes, 0, c);
            localreadbytes += c;
            if (localreadbytes / step != process) {
                process = localreadbytes / step;
                Log.i(TAG, remoteFile + "的上传进度:" + process + bean.getLocfilepath());
                cp.setProgresValues(bean.getLocfileName(), process + "", bean.getLocfilepath());
//
//                FileUser uu=new FileUser();
//                uu.setId(FileUtils.longPressLong(bean.getLocfileName()));
//                uu.setMFileProgresdao(process);
//                APP.getDaoInstant().getFileUserDao().update(uu);
                //TODO 汇报上传状态
            }
        }
        Log.i(TAG, "本地名字是：" + bean.getLocfileName());
        out.flush();
        raf.close();
        out.close();
        boolean result = ftpClient.completePendingCommand();
        if (remoteSize > 0) {
            status = result ? UploadStatus.Upload_From_Break_Success : UploadStatus.Upload_From_Break_Failed;
        } else {
            status = result ? UploadStatus.Upload_New_File_Success : UploadStatus.Upload_New_File_Failed;
        }
        return status;
    }

//    /**
//     * 上传文件到服务器,新上传和断点续传
//     *
//     * @param remoteFile 远程文件名，在上传之前已经将服务器工作目录做了改变
//     * @param localFile  本地文件 File句柄，绝对路径
//     *                   需要显示的处理进度步进值
//     * @param ftpClient  FTPClient 引用
//     * @return
//     * @throws IOException
//     */
//    public UploadStatus uploadFile(String remoteFile, File localFile,
//                                   FTPClient ftpClient, long remoteSize) throws IOException {
//        UploadStatus status;
//        boolean result = false;
//        // 显示进度的上传
//        long step = localFile.length() / 100;
//        long process = 0;
//        long localreadbytes = 0L;
//        RandomAccessFile raf = new RandomAccessFile(localFile, "r");
//        OutputStream out = ftpClient.appendFileStream(remoteFile);
//        // 断点续传
//        if (remoteSize > 0) {
//            ftpClient.setRestartOffset(remoteSize);
//            process = remoteSize / step;
//            raf.seek(remoteSize);
//            localreadbytes = remoteSize;
//        }
//        byte[] bytes = new byte[1024];
//        int c;
//        while (((c = raf.read(bytes)) != -1)) {
//            out.write(bytes, 0, c);
//            localreadbytes += c;
//            Log.i("qweqweqwe", "" + step);
//            if (step != 0) {
//                if ((localreadbytes / step) != process && !bean.getUpLoadStatus().equals("2")) {
//                    process = localreadbytes / step;
//                    // Log.i(TAG, "localFile " + localFile.getName()
//                    // + " , update process :" + process);
//                    sendMsg((int) process);
//                    Log.i("qweqweqwe", "" + process);
////					// 更新数据库信息
////					mDbFile.updataInfos(mBxFile.getFileProgress(),
////							BXFile.FileStateSwitch(mBxFile.getFileState()),
////							mBxFile.getFileName(), mBxFile.getFilePath());
//                }
//            } else {
////				sendMsg(100);
////				// 更新数据库信息
////				mDbFile.updataInfos(mBxFile.getFileProgress(),
////						BXFile.FileStateSwitch(mBxFile.getFileState()),
////						mBxFile.getFileName(), mBxFile.getFilePath());
//            }
//        }
//
//        out.flush();
//        raf.close();
//        out.close();
//
//        if ((ftpClient != null) && (result == false)) {
//            result = ftpClient.completePendingCommand();
//        }
////        if (mState == FtpState.PAUSE) {
////            result = false;
////        }
//
//        if (remoteSize > 0) {
//            status = result ? UploadStatus.Upload_From_Break_Success
//                    : UploadStatus.Upload_From_Break_Failed;
//        } else {
//            status = result ? UploadStatus.Upload_New_File_Success
//                    : UploadStatus.Upload_New_File_Failed;
//        }
//
//        // Log.i(TAG, "process is " + process + "|exit name is " +
//        // mBxFile.getFileName() + "| status is " + status);
//        return status;
//    }
}