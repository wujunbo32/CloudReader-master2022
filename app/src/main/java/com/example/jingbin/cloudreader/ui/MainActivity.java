package com.example.jingbin.cloudreader.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.viewpager.widget.ViewPager;

import com.example.jingbin.cloudreader.R;
import com.example.jingbin.cloudreader.app.ConstantsImageUrl;
import com.example.jingbin.cloudreader.app.RxCodeConstants;
import com.example.jingbin.cloudreader.bean.wanandroid.CoinUserInfoBean;
import com.example.jingbin.cloudreader.data.UserUtil;
import com.example.jingbin.cloudreader.databinding.ActivityMainBinding;
import com.example.jingbin.cloudreader.databinding.NavHeaderMainBinding;
import com.example.jingbin.cloudreader.ui.menu.NavAboutActivity;
import com.example.jingbin.cloudreader.ui.menu.NavAdmireActivity;
import com.example.jingbin.cloudreader.ui.menu.NavDeedBackActivity;
import com.example.jingbin.cloudreader.ui.menu.NavDownloadActivity;
import com.example.jingbin.cloudreader.ui.menu.NavHomePageActivity;
import com.example.jingbin.cloudreader.ui.menu.NavNightModeActivity;
import com.example.jingbin.cloudreader.ui.menu.SearchActivity;
import com.example.jingbin.cloudreader.ui.wan.WanCenterFragment;
import com.example.jingbin.cloudreader.ui.wan.WanFragment;
import com.example.jingbin.cloudreader.ui.wan.WanProjectFragment;
import com.example.jingbin.cloudreader.ui.wan.child.LoginActivity;
import com.example.jingbin.cloudreader.ui.wan.child.MyCoinActivity;
import com.example.jingbin.cloudreader.ui.wan.child.MyCollectActivity;
import com.example.jingbin.cloudreader.ui.wan.child.MyShareActivity;
import com.example.jingbin.cloudreader.utils.BaseTools;
import com.example.jingbin.cloudreader.utils.DialogBuild;
import com.example.jingbin.cloudreader.utils.GlideUtil;
import com.example.jingbin.cloudreader.utils.PerfectClickListener;
import com.example.jingbin.cloudreader.utils.SPUtils;
import com.example.jingbin.cloudreader.utils.StringFormatUtil;
import com.example.jingbin.cloudreader.utils.UpdateUtil;
import com.example.jingbin.cloudreader.view.CommonTabPagerAdapter;
import com.example.jingbin.cloudreader.view.OnLoginListener;
import com.example.jingbin.cloudreader.view.OnMyPageChangeListener;
import com.example.jingbin.cloudreader.viewmodel.wan.MainViewModel;

import java.util.Arrays;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import me.jingbin.bymvvm.base.BaseActivity;
import me.jingbin.bymvvm.rxbus.RxBus;
import me.jingbin.bymvvm.rxbus.RxBusBaseMessage;
import me.jingbin.bymvvm.utils.CommonUtils;
import me.jingbin.bymvvm.utils.StatusBarUtil;


/**
 * Created by jingbin on 16/11/21.
 *
 * <a href="https://github.com/youlookwhat">Follow me</a>
 * <a href="https://github.com/youlookwhat/CloudReader">source code</a>
 * <a href="http://www.jianshu.com/u/e43c6e979831">Contact me</a>
 */
public class MainActivity extends BaseActivity<MainViewModel, ActivityMainBinding> implements View.OnClickListener, CommonTabPagerAdapter.TabPagerListener {

    private static final String TAG = "MainActivity";
    public static boolean isLaunch;
    public boolean isClickCloseApp;
    private ViewPager vpContent;
    private ImageView ivTitleTwo;
    private ImageView ivTitleOne;
    private ImageView ivTitleThree;
    private NavHeaderMainBinding bind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showContentView();  // 在父类，显示内容
        isLaunch = true;   // 登录标志位
        initView();       // 去掉标题 注册监听 检查更新
        initContentFragment();   // fragment和viewpager  初始化Viewpager，设置Adapter, getItem中根据position返回 WanFragment、WanCenterFragment、WanProjectFragment                                                                      WanCenterFragment、WanProjectFragment
        initDrawerLayout();   // 初始化 完成数据绑定 （登录用户名，等级 排名）
        initRxBus();
    }

    @Override
    protected void initStatusBar() {
        StatusBarUtil.setColorNoTranslucentForDrawerLayout(MainActivity.this, bindingView.drawerLayout, CommonUtils.getColor(this, R.color.colorHomeToolBar));
        ViewGroup.LayoutParams layoutParams = bindingView.include.viewStatus.getLayoutParams();
        layoutParams.height = StatusBarUtil.getStatusBarHeight(this);
        bindingView.include.viewStatus.setLayoutParams(layoutParams);
    }

    // 去掉标题 注册监听 检查更新
    private void initView() {
        setNoTitle();  // 去掉原来布局文件中toolbar ，直接gone
        setSupportActionBar(bindingView.include.toolbar);   // 设为自定义个toolbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //去除默认Title显示
            actionBar.setDisplayShowTitleEnabled(false);  // 去掉标题
        }
        vpContent = bindingView.include.vpContent;  // 可以直接用，为什么还要在布局呢
        ivTitleOne = bindingView.include.ivTitleOne;
        ivTitleTwo = bindingView.include.ivTitleTwo;
        ivTitleThree = bindingView.include.ivTitleThree;
        bindingView.include.llTitleMenu.setOnClickListener(this);
        bindingView.include.ivTitleOne.setOnClickListener(this);
        bindingView.include.ivTitleTwo.setOnClickListener(this);
        bindingView.include.ivTitleThree.setOnClickListener(this);
        getClipContent();   // TODO problem 什么用
        UpdateUtil.check(this, false);
    }

    /**
     * inflateHeaderView 进来的布局要宽一些
     */
    private void initDrawerLayout() {
        bindingView.navView.inflateHeaderView(R.layout.nav_header_main);
        View headerView = bindingView.navView.getHeaderView(0);  // TODO problem 不懂
        bind = DataBindingUtil.bind(headerView);  // 不懂
        bind.setViewModel(viewModel);  // TODO problem 不懂   viewModel 是 MainViewModel
        viewModel.isReadOk.set(SPUtils.isRead());
        viewModel.isReadOkNight.set(SPUtils.isReadNight());

        GlideUtil.displayCircle(bind.ivAvatar, ConstantsImageUrl.IC_AVATAR);  // 固定图片
        bind.llNavExit.setOnClickListener(this);
        bind.ivAvatar.setOnClickListener(this);

        bind.llNavHomepage.setOnClickListener(listener);
        bind.llNavScanDownload.setOnClickListener(listener);
        bind.llNavDeedback.setOnClickListener(listener);
        bind.llNavAbout.setOnClickListener(listener);
        bind.llNavLogin.setOnClickListener(listener);
        bind.llNavCollect.setOnClickListener(listener);
        bind.llNavShare.setOnClickListener(listener);
        bind.llInfo.setOnClickListener(listener);
        bind.llNavCoin.setOnClickListener(listener);
        bind.llNavAdmire.setOnClickListener(listener);
        bind.tvRank.setOnClickListener(listener);
        bind.llNavNightMode.setOnClickListener(listener);

        /**
         *
         * getUserInfo->BaseViewModel/execute(传入HttpClient接口中设置网络参数 - 完成RxJava配置
         */
        viewModel.getUserInfo();  // 调用这个方法就发生了网络请求
        viewModel.coin.observe(this, new Observer<CoinUserInfoBean>() {  //注册观察者
            @Override
            public void onChanged(@Nullable CoinUserInfoBean coinUserInfoBean) {
                if (coinUserInfoBean != null) {
                    bind.tvUsername.setText(coinUserInfoBean.getUsername());
                    bind.tvLevel.setText(String.format("Lv.%s", UserUtil.getLevel(coinUserInfoBean.getCoinCount())));
                    bind.tvRank.setText(String.format("排名 %s", coinUserInfoBean.getRank()));
                } else {
                    bind.tvUsername.setText("玩安卓登录");
                    bind.tvLevel.setText("Lv.1");
                    bind.tvRank.setText("");
                }
            }
        });
    }

    /*ViewPager初始化*/
    private void initContentFragment() {
        // 注意使用的是：getSupportFragmentManager
        CommonTabPagerAdapter adapter = new CommonTabPagerAdapter(getSupportFragmentManager(), Arrays.asList("", "", ""));  // TODO 为什么传入FragmentManager
        adapter.setListener(this);   // TODO 怎么来的这个setListener方法 problem  直接this
        vpContent.setAdapter(adapter);
        // 设置ViewPager最大缓存的页面个数(cpu消耗少)
        vpContent.setOffscreenPageLimit(2);
        // TODO problem 为什么要用addOnPageChangeListener   （是不是要区分每个分类角标对应的 的ViewPager）
        vpContent.addOnPageChangeListener(new OnMyPageChangeListener() {   // TODO 直接ViewPager.OnPageChangeListener 不行吗？为什么要创建一个  实现了三个空方法

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        setCurrentItem(0);  // 这里是设置分类角标，所有一直就三项
                        break;
                    case 1:
                        setCurrentItem(1);
                        break;
                    case 2:
                        setCurrentItem(2);
                        break;
                    default:
                        break;
                }
            }
        });
        setCurrentItem(0);
    }


    // 抽屉页事件响应
    private PerfectClickListener listener = new PerfectClickListener() {
        @Override
        protected void onNoDoubleClick(final View v) {
            bindingView.drawerLayout.closeDrawer(GravityCompat.START);
            bindingView.drawerLayout.postDelayed(() -> {
                switch (v.getId()) {
                    case R.id.ll_nav_homepage:
                        // 项目主页
                        NavHomePageActivity.startHome(MainActivity.this);
                        break;
                    case R.id.ll_nav_scan_download:
                        //扫码下载
                        NavDownloadActivity.start(MainActivity.this);
                        break;
                    case R.id.ll_nav_deedback:
                        // 问题反馈
                        NavDeedBackActivity.start(MainActivity.this);
                        if (viewModel.isReadOk.get() != null && !viewModel.isReadOk.get().booleanValue()) {
                            SPUtils.setRead(true);
                            viewModel.isReadOk.set(true);
                        }
                        break;
                    case R.id.ll_nav_about:
                        // 关于云阅
                        NavAboutActivity.start(MainActivity.this);
                        break;
                    case R.id.ll_nav_collect:
                        // 玩安卓收藏
                        if (UserUtil.isLogin(MainActivity.this)) {
                            MyCollectActivity.start(MainActivity.this);
                        }
                        break;
                    case R.id.ll_nav_share:
                        // 玩安卓分享
                        if (UserUtil.isLogin(MainActivity.this)) {
                            MyShareActivity.start(MainActivity.this);
                        }
                        break;
                    case R.id.ll_nav_login:
                        // 玩安卓登录
                        DialogBuild.showItems(v, new OnLoginListener() {
                            @Override
                            public void loginWanAndroid() {
                                LoginActivity.start(MainActivity.this);
                            }

                            @Override
                            public void loginGitHub() {
                                WebViewActivity.loadUrl(v.getContext(), "https://github.com/login", "登录GitHub账号");
                            }
                        });
                        break;
                    case R.id.ll_info:
                        // 登录
                        if (!UserUtil.isLogin()) {
                            LoginActivity.start(MainActivity.this);
                        } else {
                            MyCoinActivity.start(MainActivity.this);
                        }
                        break;
                    case R.id.ll_nav_coin:
                        // 我的积分
                        if (UserUtil.isLogin(MainActivity.this)) {
                            MyCoinActivity.start(MainActivity.this);
                        }
                        break;
                    case R.id.tv_rank:
                        // 排行
                        if (UserUtil.isLogin(MainActivity.this)) {
                            MyCoinActivity.startRank(MainActivity.this);
                        }
                        break;
                    case R.id.ll_nav_admire:
                        // 赞赏
                        NavAdmireActivity.start(MainActivity.this);
                        break;
                    case R.id.ll_nav_night_mode:
                        // 深色模式
                        NavNightModeActivity.Companion.start(MainActivity.this);
                        if (viewModel.isReadOkNight.get() != null && !viewModel.isReadOkNight.get().booleanValue()) {
                            SPUtils.setReadNight(true);
                            viewModel.isReadOkNight.set(true);
                        }
                        break;
                    default:
                        break;
                }
            }, 260);
        }
    };

    // 响应分类角标  抽屉菜单  退出  头像
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_title_menu:
                // 开启菜单
                bindingView.drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.iv_title_two:
                // 不然cpu会有损耗    为什么要判断 TODO problem
                if (vpContent.getCurrentItem() != 1) {
                    setCurrentItem(1);
                }

                break;
            case R.id.iv_title_one:
                if (vpContent.getCurrentItem() != 0) {
                    setCurrentItem(0);
                }
                break;
            case R.id.iv_title_three:
                if (vpContent.getCurrentItem() != 2) {
                    setCurrentItem(2);
                }
                break;
            case R.id.iv_avatar:
                // 头像进入GitHub
                WebViewActivity.loadUrl(v.getContext(), CommonUtils.getString(this, R.string.string_url_cloudreader), "CloudReader");
                break;
            case R.id.ll_nav_exit:
                // 退出应用
                isClickCloseApp = true;
                finish();
                break;
            default:
                break;
        }
    }

    /**
     * 切换页面
     *
     * @param position 分类角标
     */
    private void setCurrentItem(int position) {   // ViewPager的onPageSelected和onClick 方法调用了它
        boolean isOne = false;
        boolean isTwo = false;
        boolean isThree = false;
        switch (position) {
            case 0:
                isOne = true;
                break;
            case 1:
                isTwo = true;
                break;
            case 2:
                isThree = true;
                break;
            default:
                isTwo = true;
                break;
        }
        //由fragment来确定分类角标
        vpContent.setCurrentItem(position);  // 每个分类角标对应一个ViewPager
        ivTitleOne.setSelected(isOne);   // 点击分类角标这里设置选中高亮，去掉的话就没有高亮了
        ivTitleTwo.setSelected(isTwo);
        ivTitleThree.setSelected(isThree);
    }


    // 搜索
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    // 启动搜索栏
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            SearchActivity.start(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

     /*抽屉栏退出  和 onKeyDown里处理的是否重复  按下返回键没有执行*/
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            Log.e(TAG, "onBackPressed: +++++++++");
        } else {
            super.onBackPressed();
        }
    }

    /**
     * TODO problem 什么作用
     * 获取剪切板链接
     */
    private void getClipContent() {
        String clipContent = BaseTools.getClipContent();
        if (!TextUtils.isEmpty(StringFormatUtil.formatUrl(clipContent))) {
            DialogBuild.showCustom(bindingView.drawerLayout, clipContent, "打开其中链接", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    WebViewActivity.loadUrl(MainActivity.this, clipContent, "加载中..");
                    BaseTools.clearClipboard();
                }
            });
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.fontScale != 1) {
            getResources();
        }
    }

    /**
     * 禁止改变字体大小
     */
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }


    /*和onBackPressed()区别*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (bindingView.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                bindingView.drawerLayout.closeDrawer(GravityCompat.START);
                Log.e(TAG, "onKeyDown: ++++");
            } else {
                // 不退出程序，进入后台
                moveTaskToBack(true);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 每日推荐点击"新电影热映榜"跳转
     */
    private void initRxBus() {
        Disposable subscribe = RxBus.getDefault().toObservable(RxCodeConstants.JUMP_TYPE_TO_ONE, RxBusBaseMessage.class)
                .subscribe(new Consumer<RxBusBaseMessage>() {
                    @Override
                    public void accept(RxBusBaseMessage rxBusBaseMessage) throws Exception {
                        setCurrentItem(2);  // 不太懂 problem TODO
                    }
                });
        addSubscription(subscribe);
        //登录
        Disposable subscribe2 = RxBus.getDefault().toObservable(RxCodeConstants.LOGIN, Boolean.class)  // 不太懂 problem TODO
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isLogin) throws Exception {
                        if (isLogin) {
                            viewModel.getUserInfo();
                        } else {
                            viewModel.coin.setValue(null);
                        }
                    }
                });
        addSubscription(subscribe2);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isLaunch = false;
        if (isClickCloseApp) {
            isClickCloseApp = false;
            // 杀死该应用进程 需要权限; 如果不控制会影响切换深色模式重启   TODO problem
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    public static void start(Context context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }


    // 在CommonTabPagerAdapter 中getItem调用
    @org.jetbrains.annotations.Nullable
    @Override
    public Fragment getFragment(int position) {
        switch (position) {
            case 0:
                return new WanFragment();
            case 1:
                return new WanCenterFragment();
            case 2:
                return new WanProjectFragment();
        }
        return new WanFragment();
    }
}
