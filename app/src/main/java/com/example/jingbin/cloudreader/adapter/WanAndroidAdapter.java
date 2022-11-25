package com.example.jingbin.cloudreader.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;

import com.example.jingbin.cloudreader.R;
import com.example.jingbin.cloudreader.app.Constants;
import com.example.jingbin.cloudreader.bean.wanandroid.ArticlesBean;
import com.example.jingbin.cloudreader.data.UserUtil;
import com.example.jingbin.cloudreader.data.model.CollectModel;
import com.example.jingbin.cloudreader.databinding.ItemWanAndroidBinding;
import com.example.jingbin.cloudreader.ui.WebViewActivity;
import com.example.jingbin.cloudreader.ui.wan.child.ArticleListActivity;
import com.example.jingbin.cloudreader.utils.BaseTools;
import com.example.jingbin.cloudreader.utils.DialogBuild;
import com.example.jingbin.cloudreader.utils.PerfectClickListener;
import com.example.jingbin.cloudreader.utils.SPUtils;
import com.example.jingbin.cloudreader.utils.ToastUtil;
import com.example.jingbin.cloudreader.viewmodel.wan.WanNavigator;

import me.jingbin.bymvvm.adapter.BaseBindingAdapter;
import me.jingbin.bymvvm.adapter.BaseBindingHolder;

/**
 * Created by jingbin on 2016/11/25.
 */

public class WanAndroidAdapter extends BaseBindingAdapter<ArticlesBean, ItemWanAndroidBinding> {

    private Activity activity;
    private CollectModel model;
    // 是我的收藏页进来的，全部是收藏状态。bean里面没有返回isCollect信息
    public boolean isCollectList = false;
    // 不显示类别信息
    public boolean isNoShowChapterName = false;
    // 不显示作者名字
    public boolean isNoShowAuthorName = false;
    // 列表中是否显示图片
    public boolean isNoImage = false;
    // 列表中是否显示描述
    public boolean isShowDesc = false;
    // 是否是我的分享
    public boolean isMyShare = false;

    public WanAndroidAdapter(Activity activity) {
        super(R.layout.item_wan_android);
        this.activity = activity;
        model = new CollectModel();   // 收藏相关
    }

    @Override
    protected void bindView(BaseBindingHolder holder, ArticlesBean bean, ItemWanAndroidBinding binding, int position) {
        if (bean != null) {
            if (isCollectList) {
                bean.setCollect(true);// 是否收藏
            }
            if (isShowDesc) {
                binding.tvTitle.setSingleLine();// 列表中是否显示描述
            }
            binding.setBean(bean);
            binding.setAdapter(WanAndroidAdapter.this);
            if (!TextUtils.isEmpty(bean.getEnvelopePic()) && !isNoImage) {  // getEnvelopePic图片地址
                bean.setShowImage(true);    // 控制子项图片是否显示
            } else {
                bean.setShowImage(false);
            }

            // 子项点击跳转
            if (!isMyShare) {
                binding.getRoot().setOnClickListener(new PerfectClickListener() {
                    @Override
                    protected void onNoDoubleClick(View v) {   // binding 是 ItemWanAndroidBinding
                        WebViewActivity.loadUrl(activity, bean.getLink(), bean.getTitle());
                    }
                });
            }

            // 红心 收藏   TODO problem 看不懂
            binding.vbCollect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (UserUtil.isLogin(activity) && model != null) {
                        // 为什么状态值相反？因为点了之后控件已改变状态
//                            DebugUtil.error("-----binding.vbCollect.isChecked():" + binding.vbCollect.isChecked());
                        if (!binding.vbCollect.isChecked()) {
                            model.unCollect(isCollectList, bean.getId(), bean.getOriginId(), new WanNavigator.OnCollectNavigator() {
                                @Override
                                public void onSuccess() {
                                    ToastUtil.showToastLong("已取消收藏");
                                    if (isCollectList) {
                                        int indexOf = getData().indexOf(bean);
                                        // 移除数据增加删除动画
                                        getData().remove(indexOf);
                                        refreshNotifyItemRemoved(indexOf);
                                    } else {
                                        bean.setCollect(binding.vbCollect.isChecked());
                                    }
                                }

                                @Override
                                public void onFailure() {
                                    bean.setCollect(true);
                                    refreshNotifyItemChanged(position);
                                    ToastUtil.showToastLong("取消收藏失败");
                                }
                            });
                        } else {
                            model.collect(bean.getId(), new WanNavigator.OnCollectNavigator() {
                                @Override
                                public void onSuccess() {
                                    bean.setCollect(true);
                                    ToastUtil.showToastLong("收藏成功");
                                    v.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (BaseTools.hasPackage(activity, "com.coolapk.market")) {
                                                boolean show = SPUtils.getBoolean(Constants.SHOW_MARKET, false);
                                                if (!show) {
                                                    SPUtils.putBoolean(Constants.SHOW_MARKET, true);
                                                    DialogBuild.showCustom(v, 1, "很高兴你使用云阅，如果用的愉快的话可以去酷安支持一下哦~", "去支持", "取消", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            BaseTools.launchAppDetail(activity, "com.example.jingbin.cloudreader", "com.coolapk.market");
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    }, 200);

                                }

                                @Override
                                public void onFailure() {
                                    ToastUtil.showToastLong("收藏失败");
                                    bean.setCollect(false);
                                    refreshNotifyItemChanged(position);
                                }
                            });
                        }
                    } else {
                        bean.setCollect(false);
                        refreshNotifyItemChanged(position);
                    }
                }
            });
        }
    }

    public void setCollectList() {
        this.isCollectList = true;
    }

    public void setNoShowChapterName() {
        this.isNoShowChapterName = true;
    }

    public void setNoShowAuthorName() {
        isNoShowAuthorName = true;
    }

    public void setNoImage(boolean isNoImage) {
        this.isNoImage = isNoImage;
    }

    //网页
    public void openDetail(ArticlesBean bean) {
        WebViewActivity.loadUrl(activity, bean.getLink(), bean.getTitle());
    }

    //玩安卓分类文章列表
    public void openArticleList(ArticlesBean bean) {
        ArticleListActivity.start(activity, bean.getChapterId(), bean.getChapterName());
    }
}
