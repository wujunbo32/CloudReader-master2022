package com.example.jingbin.cloudreader.adapter;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jingbin.cloudreader.R;
import com.example.jingbin.cloudreader.bean.wanandroid.ArticlesBean;
import com.example.jingbin.cloudreader.databinding.ItemNavigationContentBinding;
import com.example.jingbin.cloudreader.databinding.ItemNavigationTitleBinding;

import me.jingbin.bymvvm.adapter.BaseBindingHolder;
import me.jingbin.bymvvm.utils.CommonUtils;
import me.jingbin.library.adapter.BaseByRecyclerViewAdapter;
import me.jingbin.library.stickyview.StickyHeaderHandler;

/**
 * Created by jingbin on 2019/7/14.
 */

public class NavigationContentAdapter extends BaseByRecyclerViewAdapter<ArticlesBean, BaseBindingHolder> {

    private static final int TYPE_TITLE = 0x001;
    private static final int TYPE_CONTENT = 0x002;

    @NonNull
    @Override
    public BaseBindingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == StickyHeaderHandler.TYPE_STICKY_VIEW) {
            return new ViewTitleHolder(parent, R.layout.item_navigation_title);
        }
        return new ViewContentHolder(parent, R.layout.item_navigation_content);
    }

    @Override
    public int getItemViewType(int position) {
        if (!TextUtils.isEmpty(getData().get(position).getNavigationName())) {
            return StickyHeaderHandler.TYPE_STICKY_VIEW;
        } else {
            return TYPE_CONTENT;
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int type = getItemViewType(position);
                    /**
                     * ??????GridLayoutManager???getSpanSize???????????????????????????item?????????
                     * ???????????????4????????????GridLayoutManager
                     * new GridLayoutManager(getActivity(),6,GridLayoutManager.VERTICAL,false);
                     * ?????????6???????????????????????????????????????????????????????????????6????????????????????????1??????????????????3 ???2 ???????????????????????????3?????????1/2??????????????????????????????2????????????2?????????1/3??????????????????????????????3???
                     * */
                    switch (type) {
                        case StickyHeaderHandler.TYPE_STICKY_VIEW:
                            // title???????????????
                            return gridManager.getSpanCount();
                        case TYPE_CONTENT:
                            // ???????????????2???
                            return 3;
                        default:
                            //????????????2???
                            return 3;
                    }
                }
            });
        }
    }

    private class ViewTitleHolder extends BaseBindingHolder<ArticlesBean, ItemNavigationTitleBinding> {

        ViewTitleHolder(ViewGroup context, int layoutId) {
            super(context, layoutId);
        }

        @Override
        protected void onBindingView(BaseBindingHolder holder, ArticlesBean dataBean, int position) {
            if (dataBean != null) {
                binding.setBean(dataBean);
                if (position == 0) {
                    binding.viewLine.setVisibility(View.GONE);
                } else {
                    binding.viewLine.setVisibility(View.GONE);
                }
            }
        }
    }

    private class ViewContentHolder extends BaseBindingHolder<ArticlesBean, ItemNavigationContentBinding> {

        ViewContentHolder(ViewGroup context, int layoutId) {
            super(context, layoutId);
        }

        @Override
        protected void onBindingView(BaseBindingHolder holder, ArticlesBean dataBean, int position) {
            if (dataBean != null) {
                binding.setBean(dataBean);
                binding.tvNaviTag.setTextColor(CommonUtils.randomColor());
            }
        }
    }
}
