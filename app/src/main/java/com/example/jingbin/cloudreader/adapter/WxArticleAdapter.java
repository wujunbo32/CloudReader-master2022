package com.example.jingbin.cloudreader.adapter;

import android.content.Context;

import com.example.jingbin.cloudreader.R;
import com.example.jingbin.cloudreader.bean.wanandroid.WxarticleItemBean;
import com.example.jingbin.cloudreader.databinding.ItemWxarticleBinding;

import me.jingbin.bymvvm.adapter.BaseBindingAdapter;
import me.jingbin.bymvvm.adapter.BaseBindingHolder;
import me.jingbin.bymvvm.utils.CommonUtils;

/**
 * Created by jingbin on 2019/9/29.
 * 发现 recyclerView 设置 Adapter
 */

public class WxArticleAdapter extends BaseBindingAdapter<WxarticleItemBean, ItemWxarticleBinding> {

    private int id;
    private int selectPosition = 0;
    private int lastPosition = 0;
    private final Context context;

    public WxArticleAdapter(Context context) {
        super(R.layout.item_wxarticle);  // 子项
        this.context = context;
    }

    @Override
    protected void bindView(BaseBindingHolder holder, WxarticleItemBean dataBean, ItemWxarticleBinding binding, int position) {
        if (dataBean != null) {

            if (dataBean.getId() == id) {  //选中状态改变
                binding.tvTitle.setTextColor(CommonUtils.getColor(context, R.color.colorTheme));
                binding.viewLine.setBackgroundColor(CommonUtils.getColor(context, R.color.colorTheme));
            } else {
                binding.tvTitle.setTextColor(CommonUtils.getColor(context, R.color.select_navi_text));
                binding.viewLine.setBackgroundColor(CommonUtils.getColor(context, R.color.colorSubtitle));
            }
            binding.setBean(dataBean);
            binding.clWxarticle.setOnClickListener(v -> {  // clWxarticle 用子项ConstraintLayout布局来响应
                if (selectPosition != position) {
                    lastPosition = selectPosition;  // position实时选中位置和保存的selectPosition不一样，lastPosition更新

                    id = dataBean.getId();   // 这个id是用来防错位加载？？？TODO problem
                    selectPosition = position;

                    notifyItemChanged(lastPosition);   // 更新两个
                    notifyItemChanged(selectPosition);

                    if (listener != null) {
                        listener.onSelected(position);  // 在WanFindFragment.java中实现接口
                    }
                }
            });
        }
    }

    private OnSelectListener listener;

    public void setOnSelectListener(OnSelectListener listener) {
        this.listener = listener;
    }

    public interface OnSelectListener {
        void onSelected(int position);
    }

    public void setId(int id) {
        this.id = id;
    }
}
