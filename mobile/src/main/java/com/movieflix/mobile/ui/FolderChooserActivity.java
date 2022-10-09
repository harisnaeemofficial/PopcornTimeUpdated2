package com.movieflix.mobile.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.movieflix.base.storage.StorageUtil;
import com.movieflix.mobile.R;
import com.movieflix.mobile.ui.base.PopcornBaseActivity;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class FolderChooserActivity extends PopcornBaseActivity implements FileFilter {

    public static final String INIT_DIR = "init-dir";
    public static final String SELECTED_DIR = "selected-dir";

    final String UP_TEXT = "...";

    private ListView folderList;
    private Button cancel;
    private Button confirm;

    private ArrayAdapter<String> folderAdapter;
    private ArrayList<String> fileNames = new ArrayList<>();
    private File selectedDir;
    private File[] filesInDir;

    private AdView mAdView;
    private AdView mAdView2;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_MovieFlix_Classic);
        super.onCreate(savedInstanceState);

        // Header
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        getMovieFlixLogoView().setVisibility(View.GONE);
        getMovieFlixTitle().setVisibility(View.VISIBLE);
        Toolbar.LayoutParams titleParams = (Toolbar.LayoutParams) getMovieFlixTitle().getLayoutParams();
        titleParams.gravity = Gravity.START;
        getMovieFlixTitle().setLayoutParams(titleParams);
        getMovieFlixTitle().setSingleLine(true);
        getMovieFlixTitle().setEllipsize(TextUtils.TruncateAt.START);

        // Content
        View content = setMovieFlixContentView(R.layout.activity_folder_chooser);

        folderList = (ListView) content.findViewById(R.id.folder_chooser_list);
        folderList.setOnItemClickListener(folderListener);
        folderAdapter = new ArrayAdapter<>(FolderChooserActivity.this, android.R.layout.simple_list_item_1, fileNames);
        folderList.setAdapter(folderAdapter);

        cancel = (Button) content.findViewById(R.id.folder_chooser_cancel);
        cancel.setOnClickListener(cancelListener);

        confirm = (Button) content.findViewById(R.id.folder_chooser_confirm);
        confirm.setOnClickListener(confirmListener);

        MobileAds.initialize(this, "@string/app_id") ;
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        MobileAds.initialize(this, "@string/app_id") ;
        mAdView2 = findViewById(R.id.adView2);
        adRequest = new AdRequest.Builder().build();
        mAdView2.loadAd(adRequest);


        // @string/Interstitial_Ad       ca-app-pub-3940256099942544/1033173712
        MobileAds.initialize(this, "@string/app_id");
        mInterstitialAd = new InterstitialAd(FolderChooserActivity.this);
        mInterstitialAd.setAdUnitId("@string/Interstitial_Ad");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mInterstitialAd.show();
            }
            @Override
            public void onAdFailedToLoad(int errorCode) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mInterstitialAd.loadAd(new AdRequest.Builder().build());
                    }
                }, 60000 );
            }
            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
                //  final int intervalTime = 120000; // 8 min 480000
            }
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }
            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                mInterstitialAd.show();
            }
            @Override
            public void onAdClosed() {
            }
        });


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        File initialDir;
        if (getIntent().hasExtra(INIT_DIR)) {
            initialDir = new File(getIntent().getStringExtra(INIT_DIR));
        } else {
            initialDir = new File(File.separator);
        }

        changeDirectory(initialDir);
    }

    @Override
    public void updateLocaleText() {
        super.updateLocaleText();
        cancel.setText(android.R.string.cancel);
        confirm.setText(R.string.confirm);
    }

    @Override
    public boolean accept(File pathname) {
        if (!pathname.isHidden()) {
            if (pathname.isDirectory() && !StorageUtil.ROOT_FOLDER_NAME.equals(pathname.getName())) {
                return true;
            }
        }
        return false;
    }

    private void changeDirectory(File dir) {
        if (dir != null && dir.isDirectory()) {
            selectedDir = dir;
            File[] contents = selectedDir.listFiles(FolderChooserActivity.this);
            if (contents != null) {
                Arrays.sort(contents, fileComparator);
                filesInDir = contents;
                fileNames.clear();
                if (selectedDir.getParentFile() != null) {
                    fileNames.add(UP_TEXT);
                }
                for (File f : filesInDir) {
                    fileNames.add(f.getName());
                }
                getMovieFlixTitle().setText(selectedDir.getAbsolutePath());
                folderAdapter.notifyDataSetChanged();
                folderList.post(new Runnable() {
                    @Override
                    public void run() {
                        folderList.setSelection(0);
                    }
                });
            }
        }
        if (selectedDir != null) {
            confirm.setEnabled(isValidFile(selectedDir));
        }
    }

    private boolean isValidFile(File file) {
        return (file != null && file.isDirectory() && file.canRead() && file.canWrite());
    }

    private boolean createDirectory() {
        int text_id = -1;
        if (selectedDir != null && selectedDir.canWrite()) {
            File newDir = new File(selectedDir, StorageUtil.ROOT_FOLDER_NAME);
            if (!newDir.exists()) {
                boolean result = newDir.mkdir();
                if (result) {
                    selectedDir = newDir;
                } else {
                    text_id = R.string.no_write_access;
                }
            } else {
                selectedDir = newDir;
            }
        } else if (selectedDir != null && !selectedDir.canWrite()) {
            text_id = R.string.no_write_access;
        } else {
            text_id = R.string.no_write_access;
        }

        if (-1 != text_id) {
            Toast.makeText(FolderChooserActivity.this, R.string.no_write_access, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private Comparator<File> fileComparator = new Comparator<File>() {
        @Override
        public int compare(File lhs, File rhs) {
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }
    };

    private OnItemClickListener folderListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
            if (UP_TEXT.equals(fileNames.get(0))) {
                position -= 1;
            }

            if (position == -1) {
                File parent;
                if (selectedDir != null && (parent = selectedDir.getParentFile()) != null) {
                    changeDirectory(parent);
                }
            } else {
                if (filesInDir != null && position >= 0 && position < filesInDir.length) {
                    changeDirectory(filesInDir[position]);
                }
            }
        }
    };

    private OnClickListener cancelListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };

    private OnClickListener confirmListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (createDirectory()) {
                final Intent intent = new Intent();
                intent.putExtra(SELECTED_DIR, selectedDir.getAbsolutePath());
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    };

    public static void startForResult(Activity activity, @Nullable File initDir, int requestCode) {
        Intent intent = new Intent(activity, FolderChooserActivity.class);
        if (initDir != null) {
            intent.putExtra(INIT_DIR, initDir.getAbsolutePath());
        }
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startForResult(Fragment fragment, @Nullable File initDir, int requestCode) {
        Intent intent = new Intent(fragment.getContext(), FolderChooserActivity.class);
        if (initDir != null) {
            intent.putExtra(INIT_DIR, initDir.getAbsolutePath());
        }
        fragment.startActivityForResult(intent, requestCode);
    }
}
