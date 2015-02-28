/*
 * Copyright (c) 2015 著作权由郑志佳所有。著作权人保留一切权利。
 *
 * 这份授权条款，在使用者符合以下三条件的情形下，授予使用者使用及再散播本
 * 软件包装原始码及二进位可执行形式的权利，无论此包装是否经改作皆然：
 *
 * * 对于本软件源代码的再散播，必须保留上述的版权宣告、此三条件表列，以
 *   及下述的免责声明。
 * * 对于本套件二进位可执行形式的再散播，必须连带以文件以及／或者其他附
 *   于散播包装中的媒介方式，重制上述之版权宣告、此三条件表列，以及下述
 *   的免责声明。
 * * 未获事前取得书面许可，不得使用郑志佳或本软件贡献者之名称，
 *   来为本软件之衍生物做任何表示支持、认可或推广、促销之行为。
 *
 * 免责声明：本软件是由郑志佳及本软件之贡献者以现状（"as is"）提供，
 * 本软件包装不负任何明示或默示之担保责任，包括但不限于就适售性以及特定目
 * 的的适用性为默示性担保。郑志佳及本软件之贡献者，无论任何条件、
 * 无论成因或任何责任主义、无论此责任为因合约关系、无过失责任主义或因非违
 * 约之侵权（包括过失或其他原因等）而起，对于任何因使用本软件包装所产生的
 * 任何直接性、间接性、偶发性、特殊性、惩罚性或任何结果的损害（包括但不限
 * 于替代商品或劳务之购用、使用损失、资料损失、利益损失、业务中断等等），
 * 不负任何责任，即在该种使用已获事前告知可能会造成此类损害的情形下亦然。
 */

package android.pattern.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.os.StatFs;
import android.pattern.BaseActivity;
import android.pattern.BaseApplication;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by 郑志佳 on 1/23/15.
 * QQ: 34168035  Mail: statesman.zheng@gmail.com
 */
public class Utility {
    public static String BASE_TAG = "AndroidPattern";
    public final static boolean DEBUG = true;
    private static String TAG = "Utility";

    public final static String KEY_LOGIN_USERNAME = "KEY_LOGIN_USERNAME";

    public final static String KEY_LOGIN_PASSWORD = "KEY_LOGIN_PASSWORD";

    public final static String SHARE_LOGIN_TAG = "MAP_SHARE_LOGIN_TAG";

    public static synchronized boolean isAutoLogin(Context context) {
        SharedPreferences share = context.getSharedPreferences(SHARE_LOGIN_TAG,
                        Context.MODE_PRIVATE);
        String password = share.getString(KEY_LOGIN_PASSWORD, "");
        return !TextUtils.isEmpty(password);
    }

    public static synchronized void clearPassword(Context context) {
        SharedPreferences share = context.getSharedPreferences(SHARE_LOGIN_TAG,
                        Context.MODE_PRIVATE);
        share.edit().putString(KEY_LOGIN_PASSWORD, "").commit();
    }

    public static void showToast(String message) {
        Toast.makeText(BaseApplication.getInstance(), message, Toast.LENGTH_SHORT).show();
    }
    
    public static String getTAG(Class<?> currentClass) {
        return BASE_TAG + " - " + currentClass.getSimpleName();
    }

    /*
     * Java文件操作 获取文件扩展名
     */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    /*
     * Java文件操作 获取不带扩展名的文件名
     */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    public static String getFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public static void deleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }

        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                deleteFile(childFiles[i]);
            }
            file.delete();
        }
    }

    /*
     * 这个是解压ZIP格式文件的方法
     * 
     * @zipFileName：是传进来你要解压的文件路径，包括文件的名字；
     * 
     * @outputDirectory:选择你要保存的路劲；
     */
    public static void unzip(String zipFileName, String outputDirectory) throws Exception {
        File desDir = new File(outputDirectory);
        if (!desDir.exists()) {
            desDir.mkdirs();
        }

        ZipInputStream in = new ZipInputStream(new FileInputStream(zipFileName));
        ZipEntry z;
        String name = "";
        @SuppressWarnings("unused")
        String extractedFile = "";
        int counter = 0;

        while ((z = in.getNextEntry()) != null) {
            name = z.getName();
            Log.d(TAG, "unzipping file: " + name);
            if (z.isDirectory()) {
                Log.d(TAG, name + "is a folder");
                // get the folder name of the widget
                name = name.substring(0, name.length() - 1);
                File folder = new File(outputDirectory + File.separator + name);
                Log.d(TAG, "mkdir: " + outputDirectory + File.separator + name);
                folder.mkdirs();
                if (counter == 0) {
                    extractedFile = folder.toString();
                }
                counter++;
            } else {
                Log.d(TAG, name + " is a normal file");
                File file = new File(outputDirectory + File.separator + name);
                File fileParentDir = file.getParentFile();
                if (!fileParentDir.exists()) {
                    fileParentDir.mkdirs();
                    Log.d("zheng", "mkdir:" + fileParentDir.getName());
                }
                Log.d(TAG, "createNewFile: " + outputDirectory + File.separator + name);

                file.createNewFile();
                // get the output stream of the file
                FileOutputStream out = new FileOutputStream(file);
                int ch;
                byte[] buffer = new byte[1024];
                // read (ch) bytes into buffer
                while ((ch = in.read(buffer)) != -1) {
                    // write (ch) byte from buffer at the position 0
                    out.write(buffer, 0, ch);
                    out.flush();
                }
                out.close();
            }
        }
        in.close();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("SetJavaScriptEnabled")
    @SuppressWarnings("deprecation")
    public static void initWebview(final BaseActivity activity, WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setTextZoom(80);
        webView.requestFocus();
        settings.setLoadWithOverviewMode(true);
        if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                activity.showLoadingDialog("页面加载中, 请稍候...");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                activity.dismissLoadingDialog();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description,
                            String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                            String mimetype, long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                activity.startActivity(intent);
            }
        });
    }

    public static long getFileSizes(File file) {
        long fileSize = 0;
        FileInputStream fileInputStream = null;
        try {
            if (file.exists()) {
                fileInputStream = new FileInputStream(file);
                fileSize = fileInputStream.available();
            } else {
                System.out.println("文件不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return fileSize;
    }

    public static String getPackageName(Context context) {
        PackageInfo info;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.packageName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static SharedPreferences getApplicationsPreference(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public static String unicodeToUtf8(String s) {
        try {
            return new String(s.getBytes("utf-8"), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return s;
    }

    public static String utf8ToUnicode(String s) {
        try {
            return new String(s.getBytes("GBK"), "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        totalHeight = totalHeight * (listAdapter.getCount() + 2) / listAdapter.getCount();

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        ((MarginLayoutParams) params).setMargins(10, 10, 10, 10);
        listView.setLayoutParams(params);
    }

    public static long getAvailableSDCardSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }

    public static long getAllSDCardSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long blockCount = stat.getBlockCountLong();
        return blockCount * blockSize;
    }

    public static boolean isNumeric(String str) {
        if (TextUtils.isEmpty(str)) {
            return true;
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    /**
     * md5算法加密
     * 
     * @param s
     * @return
     */
    public final static String md5(String s) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
                        'e', 'f' };
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Android 获取本机Mac 地址方法：
     * 
     * @param context
     * @return
     */
    public static String getLocalMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    /**
     * Android 获取本机IP地址方法：
     * 
     * @return
     */
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                            .hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                                .hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("WifiPreference IpAddress", ex.toString());
        }
        return null;
    }

    /**
     * 检测邮箱地址是否合法
     * 
     * @param email
     * @return <span class="e51555" id="e51555_1">true</span>合法 false不合法
     */
    public static boolean isEmail(String email) {
        if (null == email || "".equals(email))
            return false;
        // Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配
        Pattern p = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");// 复杂匹配
        Matcher m = p.matcher(email);
        return m.matches();
    }

    public static boolean isPassword(String pwd) {
        if (null == pwd || "".equals(pwd))
            return false;
        Pattern p = Pattern.compile("^[A-Za-z0-9]+$");// 复杂匹配
        Matcher m = p.matcher(pwd);
        return m.matches();
    }

    @SuppressLint("SimpleDateFormat")
    public static String getDateTimeByMillisecond(long milliseconds) {
        Date dat = new Date(milliseconds);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(dat);
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd");
        return format.format(gc.getTime());
    }

    public static void hideSoftInput(Activity activity) {
        if (activity.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (activity.getCurrentFocus() != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) activity
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()
                                .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public static boolean matchPhone(String text) {
        if (Pattern.compile("(\\d{11})|(\\+\\d{3,})").matcher(text).matches()) {
            return true;
        }
        return false;
    }

    public static boolean matchEmail(String text) {
        if (Pattern.compile("\\w[\\w.-]*@[\\w.]+\\.\\w+").matcher(text)
                        .matches()) {
            return true;
        }
        return false;
    }

    public static String generateFixedLengthNum(int length) {
        // 获取绝对值
        length = Math.abs(length);
        Random random = new Random();
        // 获取随机数，去除随机数前两位(0.)
        String randomValue = String.valueOf(random.nextDouble()).substring(2);
        String value = "";
        int maxLength = randomValue.length();
        // 获取随机数字符串长度，并计算需要生成的长度与字符串长度的差值
        int diff = length - maxLength;
        if (diff > 0) {
            // 如果差值大于0，则说明需要生成的串长大于获取的随机数长度，此时需要将最大长度设置为当前随机串的长度
            length = maxLength;
            // 同时递归调用该随机数获取方法，获取剩余长度的随机数
            value += generateFixedLengthNum(diff);
        }
        // 获取最终的随机数
        value = randomValue.substring(0, length) + value;
        return value;
    }
    
    public static boolean isToday(long timeInMileSeconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            String date = sdf.format(new Date(timeInMileSeconds));
            String today = sdf.format(new Date());
            Log.d("zhengzj", "date:" +  date + " today:" + today);
            if (TextUtils.equals(date, today)) {
                return true;
            }
        } catch (Exception e) {
        }

        return false;
    }

    public static String getFilePathFromUri(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null,
                null, null, null);
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(index);
        cursor.close();
        return path;
    }
}
