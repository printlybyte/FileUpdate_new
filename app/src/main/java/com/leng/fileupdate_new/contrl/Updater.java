///**
// * @Title: Updater.java
// * @Package com.hutu.localfile.manager
// * @Description: TODO
// * @author Long Li
// * @date 2015-5-6 下午4:38:05
// * @version V1.0
// */
//package com.leng.fileupdate_new.contrl;
//
//import android.util.Log;
//
//import java.io.IOException;
//
///**
// * TODO<上传器>
// *
// * @author Long Li
// * @data: 2015-5-6 下午4:38:05
// * @version: V1.0
// */
//public class Updater {
//
//	private String TAG = "qweqwe";
//	private ContinueFtp cFtp;
//
//	private String FtpHostAdress = null;
//
//	// private String FtpUserName = "FTPtest";
//	// private String FtpUserPwd = "Ftp112233";
//	private String FtpUserName = "FTPuser"; // FTP 用户名 密码
//	private String FtpUserPwd = "Ftp1029384756";
//	// private String FtpRmtPath = "/FTPUpload/test/";
//	// for release
//	private int FtpHostPort = 21; // 端口
//	private String FtpRmtPath = "/web/KuaiChuan/web/Upload/"; // 服务器端路径
//
//	public	Updater(){}
////	public Updater(Context mContext, BXFile mFile, Handler mHandler,
////                   DbFile mDbFile) {
////		this.cFtp = new ContinueFtp(mContext, mHandler, mDbFile, mFile);
////		FtpHostAdress = mContext.getString(R.string.ServerIp);
////		// SharedPreferences prefs =
////		// PreferenceManager.getDefaultSharedPreferences(mContext);
////		// FtpHostAdress = prefs.getString("ServerIp", FtpHostAdress);
////
////		// 使用上帝从SharedPreferences里面得到databaseIp
////		SharedPreferences ServerSetting = LFBApplication.getApplication()
////				.getSharedPreferences("ServerSetting", 0);
////		FtpHostAdress = ServerSetting.getString("ftpIp", FtpHostAdress);
////
////		Log.d("IPandPort", FtpHostAdress);
////	}
//
//	// 开始上传一个文件
//	public void StartUpdate(String PATH){
////		if (cFtp.getFtpState() == FtpState.UPDATING) {
////			Log.i(TAG, "StartUpDate ftp state is updating");
////			return;
////		}
//
//		// 使用上帝从SharedPreferences里面得到databasePort
////		SharedPreferences ServerSetting = LFBApplication.getApplication()
////				.getSharedPreferences("ServerSetting", 0);
////		FtpHostPort = Integer.valueOf(ServerSetting.getString("ftpPort", "21"));
//
//		Log.d("IPandPort", String.valueOf(FtpHostPort));
//
////		cFtp.setFtpState(FtpState.UPDATING);
//		Log.i(TAG, "cft connect IP is " + FtpHostAdress);
//		cFtp = new ContinueFtp();
//		cFtp.SetConnectInfos(FtpHostAdress, FtpHostPort, FtpUserName,
//				FtpUserPwd, FtpRmtPath);
//		Log.i(TAG, "ftp connect status " + FtpHostAdress+FtpHostPort+FtpUserName+FtpUserPwd+FtpRmtPath);
//		try {
//
//			boolean isConnect = cFtp.connect();
//			Log.i(TAG, "ftp是不是连接上了 " + isConnect);
//			if (isConnect) {
//				Log.i(TAG, "在断点续传的时候挂了");
//				ContinueFtp.UploadStatus mStatus = cFtp.upload(PATH);
//				Log.i(TAG, "ftp的状态是 " + mStatus);
////				if ((UploadStatus.Upload_New_File_Success == mStatus) // 上传成功
////						|| (UploadStatus.Upload_From_Break_Success == mStatus)) {
////					cFtp.sendMsg(100);
////				} else if ((UploadStatus.Upload_From_Break_Failed == mStatus) // 上传失败
////						|| (UploadStatus.Upload_New_File_Failed == mStatus)) {
////					cFtp.setFtpState(FtpState.INIT);
////				} else if (UploadStatus.File_Exits == mStatus) { // 移除文件到已上传列表
////					cFtp.sendMsg(100);
////				} else {
////					Log.i(TAG, "status is " + mStatus);
////				}
//			}else {
//				Log.i(TAG, "没有连接成功");
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			// pause(); //pase state
//		} catch (NullPointerException e) {
//			// TODO: handle exception
//			e.printStackTrace();
//			// pause();
//		} finally {
////			cFtp.setFtpState(FtpState.INIT);
//			try {
//				cFtp.disconnect();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//
//	}
//
//	//
////	public boolean isupdating() {
//		// return FtpState.UPDATING == mState;
////		return FtpState.UPDATING == cFtp.getFtpState();
////	}
//
//	// 暂停上传
//	public void pause() {
//		cFtp.pause();
//		try {
//			cFtp.disconnect();
//		} catch (Exception e) { // IOException
//			// TODO Auto-generated catch block
//			System.out.println("暂停失败");
//			e.printStackTrace();
//
//		}
//	}
//}
