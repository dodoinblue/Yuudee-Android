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

import android.content.Context;
import android.pattern.BaseActivity;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;

/**
 * Created by 郑志佳 on 1/23/15.
 * QQ: 34168035  Mail: statesman.zheng@gmail.com
 */
public class HttpRequest {
	public static String BASE_URL;
	private final static String TAG = "zheng HttpRequest";
	
	public static StringEntity getStringEntity(String content) {
	    StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(content, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        return stringEntity;
	}
    public static void post(String url, HttpEntity entity, HttpTextHandler handler) {
        BaseActivity baseActivity = handler.getActivity();
        if (baseActivity == null || baseActivity.showNoNetworkAlert()) {
            return;
        }

        handler.showLoadingDialog();
        HttpClient.post(handler.getActivity(), url, entity, handler);
//        HttpClient.post(handler.getActivity(), url, entity, "application/octet-stream", handler);
    }
    public static void post(String url, RequestParams params, HttpTextHandler handler) {
        if (params != null) {
            Log.d("zheng", "HttpRequest post params:" + params.toString());
        }
        BaseActivity baseActivity = handler.getActivity();
        if (baseActivity == null || baseActivity.showNoNetworkAlert()) {
            return;
        }

        handler.showLoadingDialog();
        HttpClient.post(url, params, handler);
    }
    public static void post(String url, RequestParams params, HttpResponseHandler handler) {
        if (params != null) {
            Log.d("zheng", "HttpRequest post params:" + params.toString());
        }
        BaseActivity baseActivity = handler.getActivity();
        if (baseActivity == null || baseActivity.showNoNetworkAlert()) {
            return;
        }

        handler.showLoadingDialog();
        HttpClient.post(url, params, handler);
    }

    public static void post(String url, HttpEntity entity, HttpResponseHandler handler) {
        BaseActivity baseActivity = handler.getActivity();
        if (baseActivity == null || baseActivity.showNoNetworkAlert()) {
            return;
        }

        handler.showLoadingDialog();
        HttpClient.post(handler.getActivity(), url, entity, handler);
//        HttpClient.post(handler.getActivity(), url, entity, "application/octet-stream", handler);
    }

    public static void get(String url, RequestParams params, HttpResponseHandler handler) {
    	get(url, params, true, true, handler);
    }
    
    public static void get(String url, RequestParams params, boolean showProgress, HttpResponseHandler handler) {
        get(url, params, showProgress, true, handler);
    }
    
    public static void get(String url, RequestParams params, boolean showProgress, boolean showNoNetwork, HttpResponseHandler handler) {
        Log.d("zheng", "HttpRequest get params:" + params.toString());
        BaseActivity baseActivity = handler.getActivity();
        if (baseActivity == null) {
            return;
        }
        if (!baseActivity.hasNetwork()) {
            if (showNoNetwork) {
//                baseActivity.showNoNetworkAlert();
            }
            return;
        }
        if (showProgress) {
        	handler.showLoadingDialog();
        }
        HttpClient.get(url, params, handler);
    }
    public static class HttpTextHandler extends TextHttpResponseHandler {
        private final WeakReference<BaseActivity> mReference;

        public HttpTextHandler(BaseActivity reference) {
            mReference = new WeakReference<BaseActivity>(reference);
        }

        public BaseActivity getActivity() {
            return mReference.get();
        }

        private void showLoadingDialog() {
            try {
                BaseActivity activity = mReference.get();
                if (activity != null && !activity.isFinishing()) {
                    activity.showLoadingDialog(null);
                }
            } catch (Exception e) {
            }

        }

        private void dismissLoadingDialog() {
            try {
                BaseActivity activity = mReference.get();
                if (activity != null && !activity.isFinishing()) {
                    activity.dismissLoadingDialog();
                }
            } catch (Exception e) {
            }
        }

        private void showAlertCrouton(String info) {
            try {
                BaseActivity activity = mReference.get();
                if (activity != null && !activity.isFinishing()) {
                    activity.showAlertCrouton(info);
                }
            } catch (Exception e) {
            }
        }


        @Override
        public void onSuccess(int i, Header[] headers, String response) {
            dismissLoadingDialog();
            if (Utility.DEBUG) Log.d(TAG, "onSuccess, response:" + response);
            onSuccess(response);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers,
                              String responseString, Throwable throwable) {
            if (Utility.DEBUG) {
                Log.d(TAG, "onFailure, statusCode:" + statusCode + " response:" + responseString);
            }
            dismissLoadingDialog();
            showAlertCrouton("获取网络数据失败");
        }

        public void onSuccess(String response) {
            dismissLoadingDialog();
            if (Utility.DEBUG) Log.d(TAG, "onSuccess, response:" + response);
        }

        public void onFailure(int statusCode, String response) {
            dismissLoadingDialog();
            if (Utility.DEBUG) {
                Log.d(TAG, "onFailure(int statusCode, String response):" + response);
            }
        }

        public void onFailure(String response) {
            dismissLoadingDialog();
            if (Utility.DEBUG) {
                Log.d(TAG, "onFailure(String response):" + response);
            }
        }
    }
    public static class HttpResponseHandler extends JsonHttpResponseHandler {
        private final WeakReference<BaseActivity> mReference;
        
        public HttpResponseHandler(BaseActivity reference) {
            mReference = new WeakReference<BaseActivity>(reference);
        }

        public BaseActivity getActivity() {
            return mReference.get();
        }

        private void showLoadingDialog() {
        	try {
        		BaseActivity activity = mReference.get();
                if (activity != null && !activity.isFinishing()) {
                    activity.showLoadingDialog(null);
                }
			} catch (Exception e) {
			}

        }

        private void dismissLoadingDialog() {
        	try {
        		BaseActivity activity = mReference.get();
                if (activity != null && !activity.isFinishing()) {
                    activity.dismissLoadingDialog();
                }
			} catch (Exception e) {
			}
        }
        
        private void showAlertCrouton(String info) {
        	try {
        		BaseActivity activity = mReference.get();
                if (activity != null && !activity.isFinishing()) {
                    activity.showAlertCrouton(info);
                }
			} catch (Exception e) {
			}
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            if (Utility.DEBUG) {
                Log.d(TAG, "onSuccess, statusCode:" + statusCode + " response:" + response.toString());
            }
            boolean success = statusCode == 200;
            if (success) {
                if(response.has("ERROR")){
                    onFailure(response);
                }else{
                    onSuccess(response);
                }
            } else {
                onFailure(response);
                onFailure(statusCode, response);
            }
        }

        public void onSuccess(JSONObject response) {
            dismissLoadingDialog();
            if (Utility.DEBUG) {
                Log.d(TAG, "onSuccess, response:" + response.toString());
            }
        }
        
        public void onFailure(int statusCode, JSONObject response) {
        	dismissLoadingDialog();
            if (Utility.DEBUG) {
                Log.d(TAG, "onFailure(int statusCode, JSONObject response):" + response.toString());
            }
        }

        public void onFailure(JSONObject response) {
            dismissLoadingDialog();
            if (Utility.DEBUG) {
                Log.d(TAG, "onFailure(JSONObject response):" + response.toString());
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                        JSONObject errorResponse) {
            super.onFailure(statusCode, headers, throwable, errorResponse);
            dismissLoadingDialog();
        }

        @Override
        public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
            if (Utility.DEBUG) {
                Log.d(TAG, "onFailure, statusCode:" + statusCode + " response:" + responseString);
            }
            dismissLoadingDialog();
            showAlertCrouton("获取网络数据失败");
        }
    }

    private static class HttpClient {
        private static String mDefaultContentType = "application/json";

        private static AsyncHttpClient client = new AsyncHttpClient();

        public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
            client.get(getAbsoluteUrl(url), params, responseHandler);
        }

        public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
            client.post(getAbsoluteUrl(url), params, responseHandler);
        }
        
        public static void post(Context context, String url, HttpEntity entity, AsyncHttpResponseHandler responseHandler) {
            post(context, url, entity, mDefaultContentType, responseHandler);
        }

        public static void post(Context context, String url, HttpEntity entity, String contentType,
                        AsyncHttpResponseHandler responseHandler) {
            client.post(context, getAbsoluteUrl(url), entity, contentType, responseHandler);
        }

        private static String getAbsoluteUrl(String relativeUrl) {
            return BASE_URL + relativeUrl;
        }
    }
}
