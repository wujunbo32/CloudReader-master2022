package com.example.jingbin.cloudreader.ui.wan.child;

import androidx.lifecycle.Observer;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.View;

import com.example.jingbin.cloudreader.R;
import com.example.jingbin.cloudreader.adapter.NavigationAdapter;
import com.example.jingbin.cloudreader.adapter.NavigationContentAdapter;

import me.jingbin.bymvvm.base.BaseFragment;

import com.example.jingbin.cloudreader.bean.wanandroid.ArticlesBean;
import com.example.jingbin.cloudreader.bean.wanandroid.NaviJsonBean;
import com.example.jingbin.cloudreader.databinding.FragmentNaviBinding;
import com.example.jingbin.cloudreader.ui.WebViewActivity;
import com.example.jingbin.cloudreader.viewmodel.wan.NavigationViewModel;

import java.util.List;

import me.jingbin.library.ByRecyclerView;
import me.jingbin.library.stickyview.StickyGridLayoutManager;
import me.jingbin.library.view.OnItemFilterClickListener;

/**
 * @author jingbin
 * @date 2018/10/8.
 * @description 导航数据
 */
public class NavigationFragment extends BaseFragment<NavigationViewModel, FragmentNaviBinding> {

    private boolean mIsFirst = true;
    private NavigationAdapter mNaviAdapter;
    private NavigationContentAdapter mContentAdapter;
    private FragmentActivity activity;
    private int currentPosition = 0;
    private LinearLayoutManager layoutManager;

    @Override
    public int setContent() {
        return R.layout.fragment_navi;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    public static NavigationFragment newInstance() {
        return new NavigationFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsFirst){
            showLoading();
            initRefreshView();  // 左右RV配置，监听配置， 左右RV选中交互逻辑（）
            viewModel.getNavigationJson();
            mIsFirst = false;
        }
    }

    private void initRefreshView() {
        // 设置左标签RV适配器和布局
        mNaviAdapter = new NavigationAdapter();
        layoutManager = new LinearLayoutManager(activity);
        bindingView.xrvNavi.setLayoutManager(layoutManager);
        bindingView.xrvNavi.setAdapter(mNaviAdapter);

        // 设置右RV内容 布局和适配器
        mContentAdapter = new NavigationContentAdapter();
        // 粘性的布局  自定义一个布局管理器继承之GridLayoutManager
        StickyGridLayoutManager gridLayoutManager = new StickyGridLayoutManager(activity, 6, GridLayoutManager.VERTICAL, mContentAdapter);
        bindingView.xrvNaviDetail.setLayoutManager(gridLayoutManager);
        bindingView.xrvNaviDetail.setAdapter(mContentAdapter);

        // 左RV选择监听
        mNaviAdapter.setOnSelectListener(new NavigationAdapter.OnSelectListener() {
            @Override
            public void onSelected(int position) {
                selectItem(position);  // 选中状态更新
                moveToCenter(position);  //将当前选中的item居中

                /*
                *    titlePositions 是 List<Integer>
                * */
                Integer titlePosition = viewModel.getTitlePositions().getValue().get(position); // TODO 没看懂  这个是标题位置
                gridLayoutManager.scrollToPositionWithOffset(titlePosition, 0);  // 根据左边RV选择来更新右边RV对应内容
            }
        });
        // 右边RV滑动监听
        bindingView.xrvNaviDetail.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int itemPosition = gridLayoutManager.findFirstVisibleItemPosition();
                Integer integer = viewModel.getHashMap().get(itemPosition);   // HashMap存的内容搞不懂了 TODO problem
                if (integer != null && currentPosition != integer) {  // 这里滑动右边RV 来更新左边RV标题位置
                    selectItem(integer);   // 选中状态更新
                    moveToCenter(integer); //将当前选中的item居中
                }
            }
        });
        // 选中跳转网页
        bindingView.xrvNaviDetail.setOnItemClickListener(new OnItemFilterClickListener() {
            @Override
            public void onSingleClick(View v, int position) {   // 单次点击
                ArticlesBean itemData = mContentAdapter.getItemData(position);
                if (!TextUtils.isEmpty(itemData.getLink())) {
                    WebViewActivity.loadUrl(v.getContext(), itemData.getLink(), itemData.getTitle());
                }
            }
        });
        onObserveViewModel();
    }

    // LiveData 监听
    private void onObserveViewModel() {
        // 左侧标题
        viewModel.getDataTitle().observe(this, new Observer<List<NaviJsonBean.DataBean>>() {
            @Override
            public void onChanged(@Nullable List<NaviJsonBean.DataBean> dataBeans) {
                mNaviAdapter.setNewData(dataBeans);
                selectItem(0);
            }
        });
        // 右侧内容
        viewModel.getData().observe(this, new Observer<List<ArticlesBean>>() {
            @Override
            public void onChanged(@Nullable List<ArticlesBean> list) {
                if (list != null && list.size() > 0) {
                    showContentView();
                    mContentAdapter.setNewData(list);
                } else {
                    showError();
                }
            }
        });
    }

    // 选择状态变化更新
    private void selectItem(int position) {
        if (position < 0 || mNaviAdapter.getData().size() <= position) {
            return;
        }
        mNaviAdapter.getData().get(currentPosition).setSelected(false); //  取消之前选中
        mNaviAdapter.notifyItemChanged(currentPosition);  // 更新
        currentPosition = position;
        mNaviAdapter.getData().get(position).setSelected(true); // 选中当前选择
        mNaviAdapter.notifyItemChanged(position);   // 更新
    }

    /**
     * 将当前选中的item居中
     * 有bug 按下短视频sdk 直接跳到IM即时通讯了 TODO  IM即时通讯以下点击选择，右边内容不变化
     */
    private void moveToCenter(int position) {
        //将点击的position转换为当前屏幕上可见的item的位置以便于计算距离顶部的高度，从而进行移动居中
        View childAt = bindingView.xrvNavi.getChildAt(position - layoutManager.findFirstVisibleItemPosition());
        if (childAt != null) {
            int y = (childAt.getTop() - bindingView.xrvNavi.getHeight() / 2);
            bindingView.xrvNavi.smoothScrollBy(0, y);  // 正的向下移动， 负数向上移动
        }
    }

    @Override
    protected void onRefresh() {
        viewModel.getNavigationJson();
    }
}
