package com.movieflix.mobile.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.movieflix.mobile.PopcornApplication;
import com.movieflix.mobile.R;
import com.movieflix.mobile.ui.dialog.FirebaseMessagingDialog;
import com.movieflix.mobile.ui.dialog.ShareDialog;
import com.movieflix.model.messaging.IMessagingData;
import com.movieflix.model.messaging.IMessagingUseCase;
import com.movieflix.model.share.IShareData;
import com.movieflix.model.share.IShareUseCase;

public final class SettingsActivity extends UpdateActivity implements IShareUseCase.Observer, IMessagingUseCase.Observer {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_settings);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.settings);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((PopcornApplication) getApplication()).getShareUseCase().onViewResumed(SettingsActivity.this);
        ((PopcornApplication) getApplication()).getMessagingUseCase().subscribe(SettingsActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((PopcornApplication) getApplication()).getMessagingUseCase().unsubscribe(SettingsActivity.this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onShowShare(@NonNull IShareData data) {
        ShareDialog.open(getSupportFragmentManager(), "share_dialog");
    }

    @Override
    public void onShowMessagingDialog(@NonNull IMessagingData data) {
        FirebaseMessagingDialog.show(getSupportFragmentManager(), data, "firebase_messaging_dialog");
    }
}