package com.example.jingbin.cloudreader.ui.wan.child;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.jingbin.cloudreader.R;
import com.example.jingbin.cloudreader.adapter.WanAndroidAdapter;
import com.example.jingbin.cloudreader.adapter.WxArticleAdapter;
import com.example.jingbin.cloudreader.app.Constants;
import com.example.jingbin.cloudreader.app.RxCodeConstants;
import com.example.jingbin.cloudreader.bean.wanandroid.ArticlesBean;
import com.example.jingbin.cloudreader.bean.wanandroid.TreeBean;
import com.example.jingbin.cloudreader.bean.wanandroid.WxarticleItemBean;
import com.example.jingbin.cloudreader.databinding.FragmentWanFindBinding;
import com.example.jingbin.cloudreader.utils.DataUtil;
import com.example.jingbin.cloudreader.utils.RefreshHelper;
import com.example.jingbin.cloudreader.utils.SPUtils;
import com.example.jingbin.cloudreader.utils.ToastUtil;
import com.example.jingbin.cloudreader.viewmodel.wan.WanFindViewModel;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import me.jingbin.bymvvm.base.BaseFragment;
import me.jingbin.bymvvm.rxbus.RxBus;
import me.jingbin.library.ByRecyclerView;

/**
 * @author jingbin
 * @date 2019/9/29.
 * @description 发现订制
 */
public class WanFindFragment extends BaseFragment<WanFindViewModel, FragmentWanFindBinding> {

    private boolean mIsFirst = true;
    private WxArticleAdapter wxArticleAdapter;
    private WanAndroidAdapter mContentAdapter;
    private FragmentActivity activity;
    private int currentPosition = 0;
    private int wxArticleId;
    private boolean isAddFooter = false;

    @Override
    public int setContent() {
        return R.layout.fragment_wan_find;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    public static WanFindFragment newInstance() {
        return new WanFindFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsFirst) {
            showLoading();// 显示加载状态
            initRefreshView();  // 左右RV设置Adapter 、监听、LiveData数据变化
            initRxBus();        // 体系页定制发现内容处理
            bindingView.rvWxarticle.postDelayed(this::getData, 150);
            mIsFirst = false;
        }
    }

    private void getData() {
        int anInt = SPUtils.getInt(Constants.FIND_POSITION, -1);
        if (anInt == -1) {
            viewModel.getWxArticle();
        } else {
            if (!viewModel.handleCustomData(DataUtil.getTreeData(activity), anInt)) {
                viewModel.getWxArticle();
            }
        }
    }

    private void initRefreshView() {
        // 左边RV设置Adapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        bindingView.rvWxarticle.setLayoutManager(layoutManager);  // 左边recyclerView线性布局
        wxArticleAdapter = new WxArticleAdapter(activity);
        bindingView.rvWxarticle.setAdapter(wxArticleAdapter);

        // 右边recyclerView设置刷新 和 Adapter
        RefreshHelper.initLinear(bindingView.recyclerView, true, 1);
        mContentAdapter = new WanAndroidAdapter(activity);
        mContentAdapter.setNoShowChapterName();
//        mContentAdapter.setNoShowAuthorName();
        bindingView.recyclerView.setAdapter(mContentAdapter);

        // 左边RV 监听
        wxArticleAdapter.setOnSelectListener(new WxArticleAdapter.OnSelectListener() {
            @Override
            public void onSelected(int position) {
                selectItem(position);
            }
        });
        //右边RV 监听
        bindingView.recyclerView.setOnLoadMoreListener(new ByRecyclerView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {  // 加载更多
                int page = viewModel.getPage();
                viewModel.setPage(++page);
                viewModel.getWxarticleDetail(wxArticleId);
            }
        }, 300);

        bindingView.recyclerView.setOnRefreshListener(new ByRecyclerView.OnRefreshListener() {
            @Override
            public void onRefresh() {  // 刷新
                viewModel.setPage(1);
                viewModel.getWxarticleDetail(wxArticleId);
            }
        }, 300);

        onObserveViewModel();   // LiveData 观察数据
    }

    private void onObserveViewModel() {
        // 左侧标题
        viewModel.getDataTitle().observe(getViewLifecycleOwner(), new Observer<List<WxarticleItemBean>>() {
            @Override
            public void onChanged(@Nullable List<WxarticleItemBean> dataBeans) {
                if (dataBeans != null && dataBeans.size() > 0) {
                    wxArticleAdapter.setNewData(dataBeans);
                    selectItem(0);
                } else {
                    showError();
                }
            }
        });
        // 右侧内容
        viewModel.getData().observe(getViewLifecycleOwner(), new Observer<List<ArticlesBean>>() {
            @Override
            public void onChanged(@Nullable List<ArticlesBean> list) {
                showContentView();
                if (list != null && list.size() > 0) {
                    if (viewModel.getPage() == 1) {   // TODO 没懂这段操作
                        bindingView.recyclerView.setLoadMoreEnabled(true);
                        bindingView.recyclerView.setRefreshEnabled(true);
                        bindingView.recyclerView.setFootViewEnabled(false);
                        mContentAdapter.setNewData(list);
                        bindingView.recyclerView.getLayoutManager().scrollToPosition(0);
                    } else {
                        mContentAdapter.addData(list);
                        bindingView.recyclerView.loadMoreComplete();
                    }

                } else {
                    if (viewModel.getPage() == 1) {
                        bindingView.recyclerView.setLoadMoreEnabled(false);
                        bindingView.recyclerView.setRefreshEnabled(false);
                        if (!isAddFooter) {
                            bindingView.recyclerView.addFooterView(LayoutInflater.from(activity).inflate(R.layout.layout_loading_empty, (ViewGroup) bindingView.recyclerView, false));
                            isAddFooter = true;
                        } else {
                            bindingView.recyclerView.setFootViewEnabled(true);
                        }
                        mContentAdapter.setNewData(null);
                    } else {
                        bindingView.recyclerView.loadMoreEnd();
                    }
                }
            }
        });
    }

    // 左边RV选中后显示 右边RV内容
    private void selectItem(int position) {
        if (position < wxArticleAdapter.getData().size()) {
            wxArticleId = wxArticleAdapter.getData().get(position).getId();
            wxArticleAdapter.setId(wxArticleId);
            viewModel.setPage(1);
            viewModel.getWxarticleDetail(wxArticleId);
            bindingView.recyclerView.setRefreshEnabled(true);  // 右边RV刷新
        }
        wxArticleAdapter.notifyItemChanged(currentPosition);
        currentPosition = position;
    }

    @Override
    protected void onRefresh() {
        viewModel.getWxArticle();   // 左边RV获取公众号列表
    }

    /**
     * 知识体系页 更改 发现页 内容
     * 在TreeFragment.java中发送
     */
    private void initRxBus() {
        Disposable subscribe = RxBus.getDefault().toObservable(RxCodeConstants.FIND_CUSTOM, Integer.class)  // TODO Integer.class 什么意思
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        if (integer != null) {
                            if (!mIsFirst) {
                                TreeBean treeData = DataUtil.getTreeData(activity);   // 发现fragment和体系fragment之间通信
                                if (viewModel.handleCustomData(treeData, integer)) {
                                    ToastUtil.showToastLong("发现页内容已改为\"" + treeData.getData().get(integer).getName() + "\"");
                                }
                            } else {
                                ToastUtil.showToastLong("发现页内容已更改，请打开查看~");
                            }
                        }
                    }
                });
        addSubscription(subscribe);
    }
}
