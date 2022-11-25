package com.example.jingbin.cloudreader.view;

import android.text.Html;
import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.jingbin.cloudreader.ui.film.FilmFragment;
import com.example.jingbin.cloudreader.ui.gank.GankFragment;
import com.example.jingbin.cloudreader.ui.wan.WanCenterFragment;
import com.example.jingbin.cloudreader.ui.wan.WanFragment;

import java.util.List;

/**
 * Created by jingbin on 2016/12/6.
 * 这里是给主页 收藏页 使用适配
 */

public class MyFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> mFragment;
    private List<String> mTitleList;
    private SparseArray<Fragment> mRegisteredFragments = new SparseArray<Fragment>();

    /**
     * 主页使用
     */
    public MyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    /**
     * 接收首页传递的标题
     */
    public MyFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragment, List<String> titleList) {
        super(fm);
        this.mFragment = fragment;
        this.mTitleList = titleList;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {   // TODO problem 不懂  instantiateItem、destroyItem、getRegisteredFragment
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        mRegisteredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        mRegisteredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return mRegisteredFragments.get(position);
    }

    /**
     * 应该是创建Fragment的地方
     * https://mp.weixin.qq.com/s/MOWdbI5IREjQP1Px-WJY1Q
     */
    @Override
    public Fragment getItem(int position) {
        if (mFragment != null) {                 // 这里判断了，不为空则是收藏里的fragment   反之是主页里设置fragment
            return mFragment.get(position);
        } else {
            // 首页
            switch (position) {
                case 0:
                    return new WanFragment();
                case 1:
                    return new WanCenterFragment();
                case 2:
                    return new GankFragment();
                default:
                    return new FilmFragment();
            }
        }
    }

    @Override
    public int getCount() {
        return mFragment != null ? mFragment.size() : 3;   // 这里也是区别判断是主页的还是收藏里的
    }

    /**
     * 首页显示title，每日推荐等..
     * 若有问题，移到对应单独页面
     */
    @Override
    public CharSequence getPageTitle(int position) {
        if (mTitleList != null && position < mTitleList.size()) {
            return Html.fromHtml(mTitleList.get(position));
        } else {
            return "";   // 主页没有文字
        }
    }
}
