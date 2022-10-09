package com.movieflix.mobile.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.movieflix.mobile.R;
import com.movieflix.mobile.ui.stepperview.IStepperAdapter;
import com.movieflix.mobile.ui.stepperview.VerticalStepperItemView;
import com.movieflix.mobile.ui.stepperview.VerticalStepperView;

public class StartActivity extends AppCompatActivity implements IStepperAdapter {

    private VerticalStepperView mVerticalStepperView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mVerticalStepperView = findViewById(R.id.vertical_stepper_view);
        mVerticalStepperView.setStepperAdapter(this);
    }

    @NonNull
    @Override
    public CharSequence getTitle(int index) {
        return null;
    }

    @Nullable
    @Override
    public CharSequence getSummary(int index) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public View onCreateCustomView(int index, Context context, VerticalStepperItemView parent) {
        return null;
    }

    @Override
    public void onShow(int index) {

    }

    @Override
    public void onHide(int index) {

    }
}
