package android.pattern.util;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JsonUtil {
    private static Gson mGson = new Gson();
    private JsonUtil() {
    }
    
    public static <T> List<T> parseArray(JSONArray jsonArray) {
        return mGson.fromJson(jsonArray.toString(), new TypeToken<List<T>>(){}.getType());
    }
    
    public static <T> T parseObject(JSONObject jsonObj) {
        return mGson.fromJson(jsonObj.toString(), new TypeToken<T>(){}.getType());
    }
}