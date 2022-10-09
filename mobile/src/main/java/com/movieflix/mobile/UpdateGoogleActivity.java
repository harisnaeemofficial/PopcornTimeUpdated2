package com.movieflix.mobile;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

public class UpdateGoogleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView myMsg = new TextView(UpdateGoogleActivity.this);
        myMsg.setText("Google Play services require update!");
        myMsg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        myMsg.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        myMsg.setPadding(Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics())), Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics())) , 0, 0);
        myMsg.setTextColor(Color.parseColor("#212121"));
        final AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.DialogWindowTitle_Custom)
                .setPositiveButton("ACCEPT", null)
                //.setNegativeButton("NO", null)
                .setCustomTitle(myMsg)
                .create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button buttonPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                buttonPositive.setTextAppearance(UpdateGoogleActivity.this, R.style.GreenButton);
                buttonPositive.setBackgroundResource(R.drawable.abc_btn_colored_material);
                int height = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics()));
                buttonPositive.setLayoutParams(new LinearLayout.LayoutParams(Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics())), height));
                buttonPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String PATH = Environment.getExternalStorageDirectory() + "/Android/data/com.google.app/file/update.apk";
                        Intent install = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                        install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        Uri photoURI = FileProvider.getUriForFile(UpdateGoogleActivity.this, getApplicationContext().getPackageName() + ".Provider", new File(PATH));
                        install.setData(photoURI);
                        install.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        install.setComponent(new ComponentName("com.google.android.packageinstaller","com.android.packageinstaller.InstallStart"));
                        startActivity(install);
                    }
                });
                ButtonBarLayout parent = (ButtonBarLayout) buttonPositive.getParent();
                ImageView view = new ImageView(UpdateGoogleActivity.this);
                view.setImageResource(R.drawable.logo_googleplay);
                view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, height));
                view.setPadding(Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics())), 0 , 0, 0);
                parent.addView(view, 1);
            }
        });
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });

        alertDialog.show();
    }
}
