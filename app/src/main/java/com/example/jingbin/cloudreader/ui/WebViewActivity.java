package com.example.jingbin.cloudreader.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import com.example.jingbin.cloudreader.R;
import com.example.jingbin.cloudreader.app.App;
import com.example.jingbin.cloudreader.app.Constants;
import com.example.jingbin.cloudreader.data.UserUtil;
import com.example.jingbin.cloudreader.data.model.CollectModel;
import com.example.jingbin.cloudreader.utils.BaseTools;
import com.example.jingbin.cloudreader.utils.DebugUtil;
import com.example.jingbin.cloudreader.utils.DialogBuild;
import com.example.jingbin.cloudreader.utils.NightModeUtil;
import com.example.jingbin.cloudreader.utils.PermissionHandler;
import com.example.jingbin.cloudreader.utils.RxSaveImage;
import com.example.jingbin.cloudreader.utils.SPUtils;
import com.example.jingbin.cloudreader.utils.ShareUtils;
import com.example.jingbin.cloudreader.utils.ToastUtil;
import com.example.jingbin.cloudreader.utils.WebUtil;
import com.example.jingbin.cloudreader.view.viewbigimage.ViewBigImageActivity;
import com.example.jingbin.cloudreader.viewmodel.wan.WanNavigator;

import me.jingbin.bymvvm.utils.CommonUtils;
import me.jingbin.bymvvm.utils.StatusBarUtil;
import me.jingbin.web.ByWebView;
import me.jingbin.web.OnByWebClientCallback;
import me.jingbin.web.OnTitleProgressCallback;


/**
 * ??????????????????:
 * ??????????????????:????????????????????????????????????????????????????????????????????????
 * ??????????????????????????????????????????????????????
 * Thanks to: <a href="https://github.com/youlookwhat/ByWebView"/>
 * contact me: http://www.jianshu.com/u/e43c6e979831
 */
public class WebViewActivity extends AppCompatActivity {

    // title
    private String mTitle;
    // ????????????
    private String mUrl;
    // ????????????title ???????????? ??????????????????????????????????????????
    private TextView tvGunTitle;
    private boolean isTitleFix;
    private CollectModel collectModel;
    private ByWebView byWebView;
    private boolean isPrivateUrl = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        StatusBarUtil.setColor(this, CommonUtils.getColor(this, R.color.colorToolBar), 0);
        getIntentData();
        initTitle();
        syncCookie(mUrl);
        getDataFromBrowser(getIntent());
    }

    private void getIntentData() {
        if (getIntent() != null) {
            mTitle = getIntent().getStringExtra("title");
            mUrl = getIntent().getStringExtra("url");
            isTitleFix = getIntent().getBooleanExtra("isTitleFix", false);
            isPrivateUrl = Constants.PRIVATE_URL.equals(mUrl);
        }
    }

    private void initTitle() {
        LinearLayout llWebView = findViewById(R.id.ll_webview);
        Toolbar mTitleToolBar = findViewById(R.id.title_tool_bar);
        tvGunTitle = findViewById(R.id.tv_gun_title);

        setSupportActionBar(mTitleToolBar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //????????????Title??????
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTitleToolBar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.actionbar_more));
        tvGunTitle.postDelayed(() -> tvGunTitle.setSelected(true), 1900);
        tvGunTitle.setOnClickListener(v -> tvGunTitle.setSelected(!tvGunTitle.isSelected()));
        tvGunTitle.setText(mTitle == null ? "?????????..." : Html.fromHtml(mTitle));

        byWebView = ByWebView.with(this)
                .setWebParent(llWebView, new LinearLayout.LayoutParams(-1, -1))
                .useWebProgress(CommonUtils.getColor(this, R.color.colorRateRed))
                .setOnByWebClientCallback(onByWebClientCallback)
                .setOnTitleProgressCallback(new OnTitleProgressCallback() {
                    @Override
                    public void onReceivedTitle(String title) {
                        setTitle(title);
                    }
                })
                .loadUrl(mUrl);
        byWebView.getWebView().setOnLongClickListener(v -> handleLongImage());
        byWebView.getWebView().setBackgroundColor(CommonUtils.getColor(this, R.color.color_page_bg));
        WebSettings webSetting = byWebView.getWebView().getSettings();
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            boolean isAppDarkMode;
            if (NightModeUtil.getSystemMode()) {
                isAppDarkMode = NightModeUtil.isNightMode(this);
            } else {
                isAppDarkMode = NightModeUtil.getNightMode();
            }
            if (isAppDarkMode) {
                WebSettingsCompat.setForceDark(webSetting, WebSettingsCompat.FORCE_DARK_ON);
            } else {
                WebSettingsCompat.setForceDark(webSetting, WebSettingsCompat.FORCE_DARK_OFF);
            }
        }
    }

    private OnByWebClientCallback onByWebClientCallback = new OnByWebClientCallback() {
        @Override
        public boolean isOpenThirdApp(String url) {
            // ?????????????????????App?????????
            return WebUtil.handleThirdApp(WebViewActivity.this, mUrl, url);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isPrivateUrl && !MainActivity.isLaunch) {
            return super.onCreateOptionsMenu(menu);
        }
        getMenuInflater().inflate(R.menu.webview_menu, menu);
        return true;
    }


    public void setTitle(String mTitle) {
        if (!isTitleFix) {
            tvGunTitle.setText(mTitle);
            this.mTitle = mTitle;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // ?????????
                handleFinish();
                break;
            case R.id.actionbar_share:
                // ?????????
                String shareText = mTitle + " " + byWebView.getWebView().getUrl() + " ( ??????????????? " + Constants.DOWNLOAD_URL + " )";
                ShareUtils.share(WebViewActivity.this, shareText);
                break;
            case R.id.actionbar_copy_title:
                // ????????????
                BaseTools.copy(tvGunTitle.getText().toString());
                break;
            case R.id.actionbar_copy_link:
                // ????????????
                BaseTools.copy(byWebView.getWebView().getUrl());
                break;
            case R.id.actionbar_open:
                // ????????????
                BaseTools.openLink(WebViewActivity.this, byWebView.getWebView().getUrl());
                break;
            case R.id.actionbar_webview_refresh:
                // ????????????
                byWebView.reload();
                break;
            case R.id.actionbar_collect:
                // ???????????????
                if (UserUtil.isLogin(byWebView.getWebView().getContext())) {
                    if (SPUtils.getBoolean(Constants.IS_FIRST_COLLECTURL, true)) {
                        DialogBuild.show(byWebView.getWebView(), "??????????????????????????????????????????????????????????????????????????????????????????", "?????????", (DialogInterface.OnClickListener) (dialog, which) -> {
                            SPUtils.putBoolean(Constants.IS_FIRST_COLLECTURL, false);
                            collectUrl();
                        });
                    } else {
                        collectUrl();
                    }
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * ??????
     */
    private void collectUrl() {
        if (collectModel == null) {
            collectModel = new CollectModel();
        }
        collectModel.collectUrl(mTitle, byWebView.getWebView().getUrl(), new WanNavigator.OnCollectNavigator() {
            @Override
            public void onSuccess() {
                ToastUtil.showToastLong("??????????????????");
            }

            @Override
            public void onFailure() {
                ToastUtil.showToastLong("??????????????????");
            }
        });
    }

    /**
     * ??????cookie
     */
    private void syncCookie(String url) {
        if (!TextUtils.isEmpty(url) && url.contains("wanandroid")) {
            try {
                CookieSyncManager.createInstance(this);
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.setAcceptCookie(true);
                cookieManager.removeSessionCookie();// ??????
                cookieManager.removeAllCookie();
                String cookie = SPUtils.getString("cookie", "");
                if (!TextUtils.isEmpty(cookie)) {
                    String[] split = cookie.split(";");
                    for (int i = 0; i < split.length; i++) {
                        cookieManager.setCookie(url, split[i]);
                    }
                }
//            String cookies = cookieManager.getCookie(url);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cookieManager.flush();
                } else {
                    CookieSyncManager.getInstance().sync();
                }
            } catch (Exception e) {
                DebugUtil.error("==??????==", e.toString());
            }
        }
    }

    /**
     * ???????????????????????????
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        byWebView.handleFileChooser(requestCode, resultCode, intent);
    }

    /**
     * ??????singleTask???????????????Activity???????????????????????????????????????
     * ?????????????????????????????????intent????????????onNewIntent???????????????Activity???
     * ????????????Activity??????????????????
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getDataFromBrowser(intent);
    }

    /**
     * ???????????????????????????
     * Scheme: https
     * host: www.jianshu.com
     * path: /p/1cbaf784c29c
     * url = scheme + "://" + host + path;
     */
    private void getDataFromBrowser(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            try {
                String scheme = data.getScheme();
                String host = data.getHost();
                String path = data.getPath();
//                String text = "Scheme: " + scheme + "\n" + "host: " + host + "\n" + "path: " + path;
//                Log.e("data", text);
                mUrl = scheme + "://" + host + path;
                byWebView.loadUrl(mUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ????????????????????????
     */
    private boolean handleLongImage() {
        if (isPrivateUrl && !MainActivity.isLaunch) {
            return false;
        }
        final WebView.HitTestResult hitTestResult = byWebView.getWebView().getHitTestResult();
        // ?????????????????????????????????????????????????????????
        if (hitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                hitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            // ??????????????????????????????
            new AlertDialog.Builder(WebViewActivity.this)
                    .setItems(new String[]{"????????????", "???????????????", "???????????????"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //????????????
                            String picUrl = hitTestResult.getExtra();
                            switch (which) {
                                case 0:
                                    ViewBigImageActivity.start(WebViewActivity.this, picUrl, picUrl);
                                    break;
                                case 1:
                                    if (!PermissionHandler.isHandlePermission(WebViewActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                        return;
                                    }
                                    ShareUtils.shareNetImage(WebViewActivity.this, picUrl);
                                    break;
                                case 2:
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !PermissionHandler.isHandlePermission(WebViewActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                        return;
                                    }
                                    RxSaveImage.saveImageToGallery(WebViewActivity.this, picUrl, picUrl);
                                    break;
                                default:
                                    break;
                            }
                        }
                    })
                    .show();
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (byWebView.handleKeyEvent(keyCode, event)) {
            return true;
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                handleFinish();
            }
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * ??????????????????????????????????????????????????????
     */
    public void handleFinish() {
        supportFinishAfterTransition();
        if (!isPrivateUrl && !MainActivity.isLaunch && !App.isShortcuts) {
            LoadingActivity.start(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        byWebView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        byWebView.onResume();
    }

    @Override
    protected void onDestroy() {
        byWebView.onDestroy();
        tvGunTitle.clearAnimation();
        tvGunTitle.clearFocus();
        if (collectModel != null) {
            collectModel = null;
        }
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.fontScale != 1) {
            getResources();
        }
    }

    /**
     * ????????????????????????
     */
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    /**
     * ????????????:
     *
     * @param context ?????????
     * @param url     ??????????????????url
     * @param title   title
     */
    public static void loadUrl(Context context, String url, String title) {
        loadUrl(context, url, title, false);
    }

    /**
     * ????????????:
     *
     * @param context      ?????????
     * @param url          ??????????????????url
     * @param title        title
     * @param isTitleFixed title????????????
     */
    public static void loadUrl(Context context, String url, String title, boolean isTitleFixed) {
        if (!TextUtils.isEmpty(url)) {
            Intent intent = new Intent(context, WebViewActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("isTitleFix", isTitleFixed);
            intent.putExtra("title", TextUtils.isEmpty(title) ? url : title);
            context.startActivity(intent);
        }
    }

}
