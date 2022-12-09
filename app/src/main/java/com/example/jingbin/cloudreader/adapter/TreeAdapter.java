package com.example.jingbin.cloudreader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.jingbin.cloudreader.R;
import com.example.jingbin.cloudreader.app.Constants;
import com.example.jingbin.cloudreader.bean.wanandroid.TreeItemBean;
import com.example.jingbin.cloudreader.bean.wanandroid.WxarticleItemBean;
import com.example.jingbin.cloudreader.ui.wan.child.CategoryDetailActivity;
import com.example.jingbin.cloudreader.utils.DataUtil;
import com.example.jingbin.cloudreader.utils.SPUtils;
import com.example.jingbin.cloudreader.view.ThinBoldSpan;
import com.google.android.flexbox.FlexboxLayout;

import java.util.LinkedList;
import java.util.Queue;

import me.jingbin.bymvvm.utils.CommonUtils;
import me.jingbin.library.adapter.BaseByViewHolder;
import me.jingbin.library.adapter.BaseRecyclerAdapter;

/**
 * Update by jingbin on 2020/1/03.
 */

public class TreeAdapter extends BaseRecyclerAdapter<TreeItemBean> {

    private LayoutInflater mInflater = null;
    private Queue<TextView> mFlexItemTextViewCaches = new LinkedList<>();
    private boolean isSelect = false;
    private int selectedPosition = -1;   // TODO 什么作用
    private final Context context;

    public TreeAdapter(Context context) {
        super(R.layout.item_tree);   // 父类有两个构造方法
        this.context = context;
    }

    @Override
    protected void bindView(BaseByViewHolder<TreeItemBean> holder, TreeItemBean dataBean, int position) {
        if (dataBean != null) {
            TextView tvTreeTitle = holder.getView(R.id.tv_tree_title);  // 标题
            FlexboxLayout flTree = holder.getView(R.id.fl_tree);        // 流式布局
            String name = DataUtil.getHtmlString(dataBean.getName());  // TODO problem
            if (isSelect) {  // TODO   这个是两个界面切换标志   发现页内容定制
                flTree.setVisibility(View.GONE);
                if (selectedPosition == position) {
                    name = name + "     ★★★";  // 只有一个子项名字改变
                    tvTreeTitle.setTextColor(CommonUtils.getColor(context, R.color.colorTheme));
                } else {
                    tvTreeTitle.setTextColor(CommonUtils.getColor(context, R.color.colorContent));
                }
            } else {  // 选择类别
                tvTreeTitle.setTextColor(CommonUtils.getColor(context, R.color.colorContent));
                flTree.setVisibility(View.VISIBLE);
                for (int i = 0; i < dataBean.getChildren().size(); i++) {
                    WxarticleItemBean childItem = dataBean.getChildren().get(i);
                    TextView child = createOrGetCacheFlexItemTextView(flTree);
                    child.setText(DataUtil.getHtmlString(childItem.getName()));  // 设置流式布局TextView内容
                    // 把点击获取的内容在CategoryDetailActivity中展示
                    child.setOnClickListener(v -> CategoryDetailActivity.start(v.getContext(), childItem.getId(), dataBean));
                    flTree.addView(child);   // 添加到FlexboxLayout中
                }
            }
            tvTreeTitle.setText(ThinBoldSpan.getDefaultSpanString(tvTreeTitle.getContext(), name));  // 设置标题内容，两个界面标题共用
        }
    }

    /**
     * 复用需要有相同的BaseByViewHolder，且HeaderView部分获取不到FlexboxLayout，需要容错
     */
    @Override
    public void onViewRecycled(@NonNull BaseByViewHolder<TreeItemBean> holder) {
        super.onViewRecycled(holder);
        FlexboxLayout fbl = holder.getView(R.id.fl_tree);
        if (fbl != null) {
            for (int i = 0; i < fbl.getChildCount(); i++) {
                mFlexItemTextViewCaches.offer((TextView) fbl.getChildAt(i));   // 队列添加元素
            }
            fbl.removeAllViews();  // TODO problem
        }
    }

    // 队列里取出头，为空就创建（但是好像没加入队列），不为空就返回
    private TextView createOrGetCacheFlexItemTextView(FlexboxLayout fbl) {
        TextView tv = mFlexItemTextViewCaches.poll();
        if (tv != null) {
            return tv;
        }
        if (mInflater == null) {
            mInflater = LayoutInflater.from(fbl.getContext());
        }
        return (TextView) mInflater.inflate(R.layout.layout_tree_tag, fbl, false);  //文字背景圆形图， 但是好像没加入队列
    }

    public void setSelect(boolean isSelect) {
        this.isSelect = isSelect;
        if (isSelect) {
            selectedPosition = SPUtils.getInt(Constants.FIND_POSITION, -1);
        }
    }

    public boolean isSelect() {
        return isSelect;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }
}
