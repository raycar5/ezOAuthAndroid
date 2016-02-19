package com.ezoauth;

import android.content.Context;
import android.webkit.WebView;

import java.net.URI;
import java.net.URL;

/**
 * Created by raycar5 on 19/02/2016.
 */
public class EZOAuth {
    public EZOAuth(URL serverUrl){

    }
    public EZOAuth(URL serverUrl, String path){

    }
    public class OAuthView extends WebView {
        public OAuthView(Context context) {
            super(context);
        }
    }
}
