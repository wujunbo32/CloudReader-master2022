package com.example.jingbin.cloudreader.viewmodel.wan;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.example.jingbin.cloudreader.bean.wanandroid.BaseResultBean;
import com.example.jingbin.cloudreader.bean.wanandroid.CoinUserInfoBean;
import com.example.jingbin.cloudreader.data.UserUtil;
import com.example.jingbin.cloudreader.data.OnUserInfoListener;
import com.example.jingbin.cloudreader.http.HttpClient;
import com.example.jingbin.cloudreader.utils.DataUtil;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import me.jingbin.bymvvm.base.BaseViewModel;
import me.jingbin.bymvvm.room.User;

/**
 * @author jingbin
 * @data 2019/9/24
 * @Description 首页ViewModel
 */

public class MainViewModel extends BaseViewModel {

    // 问题反馈是否已读
    public ObservableField<Boolean> isReadOk = new ObservableField<>();   //红点 app/src/main/res/layout/nav_header_main.xml  id是 iv_no_read
    // 深色模式是否已读
    public ObservableField<Boolean> isReadOkNight = new ObservableField<>();  // 红点
    // 赞赏入口是否开放
    public ObservableField<Boolean> isShowAdmire = new ObservableField<>();    // nav_header_main.xml  有时间限制，到时间才显示ll_nav_admire
    public MutableLiveData<CoinUserInfoBean> coin = new MutableLiveData<>();  // 在MainActivity中注册观察者

    public MainViewModel(@NonNull Application application) {
        super(application);
        isShowAdmire.set(DataUtil.isShowAdmire());
    }

    // 调用这个方法就发生了网络请求
    public void getUserInfo() {   // 在MainActivity中的initDrawerLayout()调用了
        UserUtil.getUserInfo(new OnUserInfoListener() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    execute(HttpClient.Builder.getWanAndroidServer().getCoinUserInfo(),  //getWanAndroidServer 配置好retrofit
                            new Observer<BaseResultBean<CoinUserInfoBean>>() {
                                @Override
                                public void onSubscribe(Disposable d) {   // 默认最先调用复写的 onSubscribe（）
                                    addDisposable(d);  // 什么意思 TODO problem  相当于将网络与UI的生命周期绑定
                                }

                                @Override
                                public void onNext(BaseResultBean<CoinUserInfoBean> bean) {
                                    if (bean != null && bean.getData() != null) {
                                        CoinUserInfoBean infoBean = bean.getData();
                                        infoBean.setUsername(user.getUsername());   // user 好像是数据库中的，那它在哪里初始化数据
                                        coin.setValue(infoBean);

                                        UserUtil.getUserInfo(new OnUserInfoListener() {
                                            @Override
                                            public void onSuccess(User user) {
                                                if (user != null) {
                                                    user.setCoinCount(infoBean.getCoinCount());
                                                    user.setRank(infoBean.getRank());
                                                    UserUtil.setUserInfo(user);
                                                }
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    coin.setValue(null);
                                }

                                @Override
                                public void onComplete() {

                                }
                            });
                } else {
                    coin.setValue(null);
                }
            }
        });
    }

    public MutableLiveData<CoinUserInfoBean> getCoin() {
        return coin;
    }
}
