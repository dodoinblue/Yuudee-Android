package com.gcwt.yudee.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.content.Context;
import android.util.Log;

public class FileService {
    public static final String TAG = "FileService";
    private Context context;

    //得到传入的上下文对象的引用
    public FileService(Context context) {
        this.context = context;
    }

    /**
     * 保存文件
     *
     * @param fileName 文件名
     * @param content  文件内容
     * @throws Exception
     */
    public void save(String fileName, String content) throws Exception {
        byte[] buf = fileName.getBytes("iso8859-1");

        Log.e(TAG, new String(buf,"utf-8"));

        fileName = new String(buf,"utf-8");

        Log.e(TAG, fileName);

        // Context.MODE_PRIVATE：为默认操作模式，代表该文件是私有数据，只能被应用本身访问，在该模式下，写入的内容会覆盖原文件的内容，如果想把新写入的内容追加到原文件中。可以使用Context.MODE_APPEND
        // Context.MODE_APPEND：模式会检查文件是否存在，存在就往文件追加内容，否则就创建新文件。
        // Context.MODE_WORLD_READABLE和Context.MODE_WORLD_WRITEABLE用来控制其他应用是否有权限读写该文件。
        // MODE_WORLD_READABLE：表示当前文件可以被其他应用读取；MODE_WORLD_WRITEABLE：表示当前文件可以被其他应用写入。
        // 如果希望文件被其他应用读和写，可以传入：
        // openFileOutput("output.txt", Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

        FileOutputStream fos = context.openFileOutput(fileName, context.MODE_PRIVATE);
        fos.write(content.getBytes());
        fos.close();
    }

    /**
     * 读取文件内容
     *
     * @param fileName 文件名
     * @return 文件内容
     * @throws Exception
     */
    public String read(String fileName) throws Exception {
//        FileInputStream fis = context.openFileInput(fileName);
        File file = new File(fileName);
        FileInputStream fis = new FileInputStream(file);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buf = new byte[1024];
        int len = 0;

        //将读取后的数据放置在内存中---ByteArrayOutputStream
        while ((len = fis.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }

        fis.close();
        baos.close();

        //返回内存中存储的数据
        return baos.toString();
    }

}