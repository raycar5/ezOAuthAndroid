package com.ezoauth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.ArrayMap;
import android.util.Log;
import android.webkit.JavascriptInterface;

import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by raycar5 on 19/02/2016.
 */
public class EZOAuth {
    //Globals
    private String path;
    private String titleString = "Login";
    private String closeButtonString = "Close";
    private String loadingString = "Loading...";
    private HashMap<String,String> headers = new HashMap<>();


    //Constructors
    public EZOAuth(String serverUrl) {
            path = serverUrl + "/ezoauth";
    }
    public EZOAuth(String serverUrl, String inpath){
            path = serverUrl + inpath;
    }

    //Public setters
    public void setDefaultTitleString(String titleString) {
        this.titleString = titleString;
    }

    public void setDefaultCloseButtonString(String closeButtonString) {
        this.closeButtonString = closeButtonString;
    }

    public void setDefaultLoadingString(String loadingString) {
        this.loadingString = loadingString;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        if (deviceIdentifier == null){
            headers.remove("ezdeviceid");
        }else {
            headers.put("ezdeviceid", deviceIdentifier);
        }
    }

    public void setMetaData(String metadata){
        if (metadata == null){
            headers.remove("ezmetadata");
        }else {
            headers.put("ezmetadata", metadata);
        }
    }

    public void addHeaders(HashMap<String,String> inHeaders){
        headers.putAll(inHeaders);
    }

    //Interface for callbacks
    public interface AuthenticateCallback {
        void done(JSONObject data);
        void error(String err);
    }

    //Authenticate with default strings but with additional headers
    public void  authenticate(final Activity context,final String method, final AuthenticateCallback callback){
        authenticate(context,method,new HashMap<String, String>(),titleString,closeButtonString,loadingString,callback);
    }

    //Authenticate with default strings but with additional headers
    public void  authenticate(final Activity context,final String method, HashMap<String,String> additionalHeaders, final AuthenticateCallback callback){
        authenticate(context,method,additionalHeaders,titleString,closeButtonString,loadingString,callback);
    }

    //Main authenticate function
    public void authenticate(final Activity context, final String method, HashMap<String,String> additionalHeaders ,final String titleString, final String closeButtonString, final String loadingString, final AuthenticateCallback callback){

        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(titleString);
        WebView wv = new WebView(context){
            //This allows the user to enter text in the webview
            @Override public boolean onCheckIsTextEditor() {
                return true;
            }
        };
        alert.setView(wv);
        alert.setNegativeButton(closeButtonString, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        final AlertDialog dialog = alert.create();
        dialog.show();
        final ProgressDialog pd = ProgressDialog.show(context, "", loadingString,true);
        //Json extractor JS interface
        class JsonReaderJSInterface {
            private Context ctx;
            JsonReaderJSInterface(Context ctx) {
                this.ctx = ctx;
            }
            @JavascriptInterface
            public void extractJson(String html) {
                //Pattern match the json object and take it as a string
                Pattern p = Pattern.compile("\\{.*\\}");
                Matcher m = p.matcher(html);
                //If we find json in the response call the callback, if not, call with an error
                if(m.find()){
                    try {
                        callback.done(new JSONObject(m.group()));
                    } catch (JSONException e) {
                        callback.error(e.getMessage());
                        e.printStackTrace();
                    }
                }else{
                    callback.error("Could not find a json object in the response");
                    Log.e("ezOAuth", "Could not find a json object in the response");
                }

                //Close the window once the object is retrieved
                dialog.dismiss();
                return;
            }
        }
        //Enable javascript for the extractor
        wv.getSettings().setJavaScriptEnabled(true);
        //Load the appropriate url with the headers
        HashMap<String,String> finalHeaders = headers;
        finalHeaders.putAll(additionalHeaders);
        wv.loadUrl(path + "/" + method, finalHeaders);

        //Add the interface to the webview
        wv.addJavascriptInterface(new JsonReaderJSInterface(context), "jsonExtractor");

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if(pd!=null && pd.isShowing())
                {
                    pd.dismiss();
                }
                //If the url is the callback, extract the json content
                if (url.contains(path + "/" + method + "/callback")) {
                    view.loadUrl("javascript:jsonExtractor.extractJson" +
                            "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    callback.error(error.getDescription().toString());
                }else{
                    callback.error(error.toString());
                }
                dialog.dismiss();
                return;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                callback.error(error.toString());
                dialog.dismiss();
                return;
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    callback.error(errorResponse.getReasonPhrase());
                }else{
                    callback.error(errorResponse.toString());
                }
                dialog.dismiss();
                return;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        }
    }
