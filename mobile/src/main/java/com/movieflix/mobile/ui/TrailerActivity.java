package com.movieflix.mobile.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.movieflix.base.utils.FullscreenableChromeClient;
import com.movieflix.mobile.R;

public class TrailerActivity extends BaseSwipeBackActivity {

    public static final String TRAILER_URL_KEY = "trailer-url";

    private WebView trailer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trailer);

        String url = getIntent().getStringExtra(TRAILER_URL_KEY);
        trailer = findViewById(R.id.trailer_view);
        trailer.setWebChromeClient(new FullscreenableChromeClient(this));
        trailer.setWebViewClient(new WebViewClient());
        trailer.getSettings().setJavaScriptEnabled(true);
        trailer.getSettings().setDomStorageEnabled(true);
        trailer.loadUrl(url);
    }

    @Override
    protected void onDestroy() {
        trailer.loadUrl("about:blank");
        super.onDestroy();
    }
    public static void start(Context context, String url) {
        Intent intent = new Intent(context, TrailerActivity.class);
        intent.putExtra(TrailerActivity.TRAILER_URL_KEY, url);
        context.startActivity(intent);
    }
}