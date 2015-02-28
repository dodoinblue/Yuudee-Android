package android.pattern.provider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.pattern.R;
import android.util.Log;

/**
 * Created by 郑志佳 on 1/23/15.
 * QQ: 34168035  Mail: statesman.zheng@gmail.com
 */
public class LocationDBManager {
    private final int BUFFER_SIZE = 1024;
    public static final String DB_NAME = "city_cn.s3db";
    public static String PACKAGE_NAME;
    public static String DB_PATH;
    private SQLiteDatabase database;
    private Context context;
    private File file=null;

    public LocationDBManager(Context context) {
        Log.e("cc", "DBManager");
        this.context = context;
        PACKAGE_NAME = context.getPackageName();
        DB_PATH = "/data"
                        + Environment.getDataDirectory().getAbsolutePath() + "/"+ PACKAGE_NAME;
    }

    public void openDatabase() {
        Log.e("cc", "openDatabase()");
        this.database = this.openDatabase(DB_PATH + "/" + DB_NAME);
    }
    public SQLiteDatabase getDatabase(){
        Log.e("cc", "getDatabase()");
        return this.database;
    }

    private SQLiteDatabase openDatabase(String dbfile) {
        try {
            Log.e("cc", "open and return");
            file = new File(dbfile);
            if (!file.exists()) {
                Log.e("cc", "file");
                InputStream is = context.getResources().openRawResource(R.raw.city);
                if(is!=null){
                    Log.e("cc", "is null");
                }else{
                }
                FileOutputStream fos = new FileOutputStream(dbfile);
                if(is!=null){
                    Log.e("cc", "fosnull");
                }else{
                }
                byte[] buffer = new byte[BUFFER_SIZE];
                int count = 0;
                while ((count =is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                    Log.e("cc", "while");
                    fos.flush();
                }
                fos.close();
                is.close();
            }
            database = SQLiteDatabase.openOrCreateDatabase(dbfile,null);
            return database;
        } catch (FileNotFoundException e) {
            Log.e("cc", "File not found");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("cc", "IO exception");
            e.printStackTrace();
        } catch (Exception e){
            Log.e("cc", "exception "+e.toString());
        }
        return null;
    }
    public void closeDatabase() {
        Log.e("cc", "closeDatabase()");
        if(this.database!=null)
            this.database.close();
    }
}