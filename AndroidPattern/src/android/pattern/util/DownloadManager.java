package android.pattern.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.protocol.HTTP;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.pattern.BaseActivity;
import android.util.Log;

public class DownloadManager {
    public final static String mSDCardMoviePath = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/";

    public final static String mSDCardDownloadsPath = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
    public final static int BYTES_IN_1_M = 1024 * 1024;
    private int fileSize;
    private int downLoadFileSize;
    private static String mUrl;
    private Handler mHandler = null;
    private boolean mDownloadCanceled;
    private boolean mIsDownloading;
    private String mDownloadPath;
    private String filename;
    private BaseActivity mActivity;

    public DownloadManager(BaseActivity activity, String url) {
        mHandler = activity.mHandler;
        mActivity = activity;
        mUrl = url;
        filename = url.substring(url.lastIndexOf("/") + 1);
    }
    public boolean startDownload() {
        return startDownload(mSDCardDownloadsPath);
    }

    public boolean startDownload(final String storagePath) {
        boolean allowDownload = mActivity.allowDownloadByCurrentNetwork();
        if (!allowDownload) {
            mActivity.showNetworkOption();
            mActivity.showCustomToast("当前网络不允许下载, 请打开相应网络设置");
            return false;
        }
        mActivity.showCustomToast("开始下载, 请稍候...");
        mDownloadPath = storagePath;
        new Thread() {
            @Override
            public void run() {
                try {
                    File file = new File(storagePath);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    downloadFile();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        return true;
    }

    public void cancelDownload() {
        if (mIsDownloading) {
            mDownloadCanceled = true;
            mIsDownloading = false;
            Utility.deleteFile(new File(mDownloadPath + filename));
        }
    }

    public boolean isDownloading() {
        return mIsDownloading;
    }

    private void downloadFile() throws IOException {
        String url = mUrl;
        String fileName = Utility.getFileNameFromUrl(url);
        fileName = Utility.getFileNameNoEx(fileName);

        String encodedFileName = URLEncoder.encode(fileName, HTTP.UTF_8).replaceAll("\\+", "%20");
        //        url = url.replaceAll("%3A", ":").replaceAll("%2F", "/");
        url = url.replace(fileName, encodedFileName);
        Log.d("zheng", "encoded url:" +  url);

        URL myURL = new URL(url);
        URLConnection conn = myURL.openConnection();
        conn.connect();
        InputStream is = conn.getInputStream();
        this.fileSize = conn.getContentLength();// 根据响应获取文件大小
        if (this.fileSize <= 0) {
            throw new RuntimeException("无法获知文件大小 ");
        }
        if (is == null) {
            throw new RuntimeException("stream is null");
        }
        FileOutputStream fos = new FileOutputStream(mDownloadPath + filename);
        // 把数据存入路径+文件名
        byte buf[] = new byte[1024];
        downLoadFileSize = 0;
        sendMsg(BaseActivity.MESSAGE_DOWNLOAD_STARTED, fileSize);
        mIsDownloading = true;
        do {
            if (mDownloadCanceled) {
                break;
            }
            // 循环读取
            int numread = is.read(buf);
            if (numread == -1) {
                break;
            }
            fos.write(buf, 0, numread);
            downLoadFileSize += numread;
            sendMsg(BaseActivity.MESSAGE_DOWNLOADING, downLoadFileSize);// 更新进度条
        } while (true);
        mIsDownloading = false;

        try {
            fos.close();
            is.close();
        } catch (Exception ex) {
            Log.e("tag", "error: " + ex.getMessage(), ex);
            Utility.deleteFile(new File(mDownloadPath + filename));
        }

        if (mDownloadCanceled) {
            sendMsg(BaseActivity.MESSAGE_DOWNLOAD_CANCELLED);
        } else {
            sendMsg(BaseActivity.MESSAGE_DOWNLOAD_COMPLETED);// 通知下载完成
        }
    }

    private void sendMsg(int flag, Object obj) {
        Message msg = new Message();
        msg.what = flag;
        msg.obj = obj;
        mHandler.sendMessage(msg);
    }

    private void sendMsg(int flag) {
        Message msg = new Message();
        msg.what = flag;
        mHandler.sendMessage(msg);
    }

    /**
     * 安装APK文件
     */
    public void installApk() {
        File apkfile = new File(mDownloadPath, filename);
        if (!apkfile.exists()) {
            return;
        }
        // 通过Intent安装APK文件
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                        "application/vnd.android.package-archive");
        mActivity.startActivity(i);
    }
}