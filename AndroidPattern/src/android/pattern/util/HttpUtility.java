package android.pattern.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;

import com.loopj.android.http.RequestParams;

public class HttpUtility {
    public final static String client_secret = "fa1fc45f1741f31ba29c0ad9dfa387a8";

    public static RequestParams getHttpRequestParams(Context context, HashMap<String, String> apiParamHm) {
        HashMap<String, String> hm = getBaseParams(context);
        hm.putAll(apiParamHm);
        LinkedHashMap<String, String> linkedHm = sortHashMap(hm);
        String apiSign = "";
        for (Entry<String, String> entry : linkedHm.entrySet()) {
            apiSign += entry.getKey() + "=" + entry.getValue();
        }
        apiSign += client_secret;
        apiSign = Utility.md5(apiSign);
        linkedHm.put("api_sign", apiSign);

        RequestParams params = new RequestParams();
        for (Entry<String, String> entry : linkedHm.entrySet()) {
            params.put(entry.getKey(), entry.getValue());
        }

        return params;
    }

    public static HashMap<String, String> getBaseParams(Context context) {
        HashMap<String, String> hm = new HashMap<String, String>(8);
        hm.put("client_id", "3000000020");
        hm.put("device_sn", Utility.getLocalMacAddress(context));
        hm.put("api_time", String.valueOf(System.currentTimeMillis()));
        hm.put("client_version", "1");
        return hm;
    }

    public static LinkedHashMap<String, String> sortHashMap(HashMap<String, String> hm) {
        LinkedHashMap<String, String> linkedHM = new LinkedHashMap<String, String>();

        Set<String> keySet = hm.keySet();
        Object[] keys = keySet.toArray();
        Arrays.sort(keys);
        Set<Entry<String, String>> set = hm.entrySet();
        for (int j = 0; j < keys.length; j++) {
            Iterator<Entry<String, String>> it = set.iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> me = (Map.Entry<String, String>) it.next();
                if (keys[j].equals(me.getKey())) {
                    linkedHM.put(me.getKey(), me.getValue());
                    break;
                }
            }
        }
        return linkedHM;
    }
}