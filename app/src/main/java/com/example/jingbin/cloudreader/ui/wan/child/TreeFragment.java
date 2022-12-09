package com.example.jingbin.cloudreader.ui.wan.child;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.jingbin.cloudreader.R;
import com.example.jingbin.cloudreader.adapter.TreeAdapter;
import com.example.jingbin.cloudreader.app.RxCodeConstants;
import com.example.jingbin.cloudreader.bean.wanandroid.TreeBean;
import com.example.jingbin.cloudreader.databinding.FragmentWanAndroidBinding;
import com.example.jingbin.cloudreader.databinding.HeaderItemTreeBinding;
import com.example.jingbin.cloudreader.utils.DataUtil;
import com.example.jingbin.cloudreader.utils.RefreshHelper;
import com.example.jingbin.cloudreader.utils.ToastUtil;
import com.example.jingbin.cloudreader.viewmodel.wan.TreeViewModel;

import me.jingbin.bymvvm.base.BaseFragment;
import me.jingbin.bymvvm.rxbus.RxBus;
import me.jingbin.library.decoration.SpacesItemDecoration;
import me.jingbin.library.view.OnItemFilterClickListener;

/**
 * @author jingbin
 * @date 2018/09/15.
 * @description 知识体系
 */
public class TreeFragment extends BaseFragment<TreeViewModel, FragmentWanAndroidBinding> {

    private boolean mIsFirst = true;
    private TreeAdapter mTreeAdapter;
    private FragmentActivity activity;

    @Override
    public int setContent() {
        return R.layout.fragment_wan_android;  // SwipeRefreshLayout  ByRecyclerView
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    public static TreeFragment newInstance() {
        return new TreeFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsFirst) {
            showLoading();  // 显示加载中状态
            initRefreshView(); // 刷新  头标签  点击事件
            getTree();     // 获取数据并监听变化
            mIsFirst = false;
        }
    }

    private void initRefreshView() {
        RefreshHelper.setSwipeRefreshView(bindingView.srlWan);  // 刷新
        bindingView.srlWan.setOnRefreshListener(() -> bindingView.srlWan.postDelayed(this::getTree, 150));

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        bindingView.xrvWan.setLayoutManager(layoutManager);
        mTreeAdapter = new TreeAdapter(activity);
        bindingView.xrvWan.setAdapter(mTreeAdapter);

        // 头标签
        HeaderItemTreeBinding oneBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.header_item_tree, null, false);
        bindingView.xrvWan.addHeaderView(oneBinding.getRoot());

        oneBinding.tvPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mTreeAdapter.isSelect()) {  // 一开始是false， 进入这个方法说明已经点击
                    GridLayoutManager layoutManager = new GridLayoutManager(activity, 2);
                    bindingView.xrvWan.setLayoutManager(layoutManager);
                    oneBinding.tvPosition.setText("选择类别");
                    mTreeAdapter.setSelect(true);
                    mTreeAdapter.notifyDataSetChanged();  // 更新数据改变内容
                    bindingView.xrvWan.addItemDecoration(new SpacesItemDecoration(activity).setNoShowDivider(1, 0).setDrawable(R.drawable.shape_line));
                } else {
                    LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
                    bindingView.xrvWan.setLayoutManager(layoutManager);
                    oneBinding.tvPosition.setText("发现页内容订制");
                    mTreeAdapter.setSelect(false);
                    mTreeAdapter.notifyDataSetChanged();// 更新数据改变内容
                    if (bindingView.xrvWan.getItemDecorationCount() > 0) {
                        bindingView.xrvWan.removeItemDecorationAt(0);   // 什么意思 TODO problem
                    }
                }
            }
        });
        // 子项点击
        bindingView.xrvWan.setOnItemClickListener(new OnItemFilterClickListener() {
            @Override
            public void onSingleClick(View v, int position) {
                if (mTreeAdapter.isSelect()) {  // 已经切换到定制页
                    if (mTreeAdapter.getSelectedPosition() == position) {  // 重复点击
                        ToastUtil.showToastLong("当前已经是\"" + mTreeAdapter.getData().get(position).getName() + "\"");
                        return;   // 重复点击 不执行下面内容
                    }

                    // 不是重复点击，选择后，更新数据，自动切换RV内容到相应位置，
                    LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
                    bindingView.xrvWan.setLayoutManager(layoutManager);
                    oneBinding.tvPosition.setText("发现页内容订制");
                    mTreeAdapter.setSelect(false);
                    mTreeAdapter.notifyDataSetChanged();
                    if (bindingView.xrvWan.getItemDecorationCount() > 0) {
                        bindingView.xrvWan.removeItemDecorationAt(0);  // 什么作用
                    }
                    // 切换到响应内容位置
                    layoutManager.scrollToPositionWithOffset(position + bindingView.xrvWan.getCustomTopItemViewCount(), 0);
                    RxBus.getDefault().post(RxCodeConstants.FIND_CUSTOM, position);
                }
            }
        });
    }

    private void getTree() {
        // viewModel.getTree()获取数据，再observe自动更新
        viewModel.getTree().observe(this, new androidx.lifecycle.Observer<TreeBean>() {
            @Override
            public void onChanged(@Nullable TreeBean treeBean) {
                showContentView();
                if (bindingView.srlWan.isRefreshing()) {
                    bindingView.srlWan.setRefreshing(false);
                }
                if (treeBean != null
                        && treeBean.getData() != null
                        && treeBean.getData().size() > 0) {

                    if (mTreeAdapter.getItemCount() == 0) {
                        DataUtil.putTreeData(activity, treeBean); //保存知识体系数据
                    }
                    mTreeAdapter.setNewData(treeBean.getData());
                    bindingView.xrvWan.loadMoreComplete();
                } else {
                    if (mTreeAdapter.getData().size() == 0) {
                        showError();
                    } else {
                        bindingView.xrvWan.loadMoreComplete();
                    }
                }
            }
        });
    }

    @Override
    protected void onRefresh() {
        bindingView.srlWan.setRefreshing(true);
        getTree();
    }
}
