package com.example.jingbin.cloudreader.ui.wan

import androidx.fragment.app.Fragment
import com.example.jingbin.cloudreader.R
import com.example.jingbin.cloudreader.databinding.FragmentContentBinding
import com.example.jingbin.cloudreader.ui.wan.child.NavigationFragment
import com.example.jingbin.cloudreader.ui.wan.child.TreeFragment
import com.example.jingbin.cloudreader.ui.wan.child.WanFindFragment
import com.example.jingbin.cloudreader.view.CommonTabPagerAdapter
import me.jingbin.bymvvm.base.BaseFragment
import me.jingbin.bymvvm.base.NoViewModel

/**
 * 中间的tab页
 */
class WanCenterFragment : BaseFragment<NoViewModel, FragmentContentBinding>(), CommonTabPagerAdapter.TabPagerListener {

    private var mIsFirst = true

    override fun setContent(): Int = R.layout.fragment_content

    override fun onResume() {
        super.onResume()
        if (mIsFirst) {
            showLoading()  // 显示加载状态
            val pagerAdapter = CommonTabPagerAdapter(childFragmentManager, listOf("发现", "体系", "导航"))
            pagerAdapter.listener = this
            bindingView.vpGank.adapter = pagerAdapter
            bindingView.vpGank.offscreenPageLimit = 2
            pagerAdapter.notifyDataSetChanged()   // TODO 为啥要更新
            bindingView.tabGank.setupWithViewPager(bindingView.vpGank)
            showContentView()  // 加载完成的状态
            mIsFirst = false
        }
    }

    // 用于CommonTabPagerAdapter绑定 fragment
    override fun getFragment(position: Int): Fragment? =
            when (position) {
                0 -> WanFindFragment.newInstance()  // 发现
                1 -> TreeFragment.newInstance()    // 体系
                2 -> NavigationFragment.newInstance()   // 导航
                else -> NavigationFragment.newInstance()
            }
}