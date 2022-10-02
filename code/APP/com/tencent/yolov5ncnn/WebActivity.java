package com.tencent.yolov5ncnn;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tencent.yolov5ncnn.R;

public class WebActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBarColor();
        setContentView(R.layout.activity_web);
        WebView encyclopedia = (WebView) findViewById(R.id.encyclopedia);
        encyclopedia.getSettings().setJavaScriptEnabled(true);
        encyclopedia.setWebViewClient(new WebViewClient());
        encyclopedia.loadUrl("http://baidu.com");
    }

    private void setBarColor()
    {
        getWindow().setNavigationBarColor(getColor(R.color.blue));
        getWindow().setStatusBarColor(getColor(R.color.blue));
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getColor(R.color.blue)));
    }
}