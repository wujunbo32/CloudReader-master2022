package com.example.jingbin.cloudreader.ui.wan

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.jingbin.cloudreader.R
import com.example.jingbin.cloudreader.bean.wanandroid.TreeItemBean
import com.example.jingbin.cloudreader.databinding.FragmentContentBinding
import com.example.jingbin.cloudreader.ui.wan.child.CategoryArticleFragment
import com.example.jingbin.cloudreader.view.CommonTabPagerAdapter
import com.example.jingbin.cloudreader.view.CommonTabPagerAdapter.TabPagerListener
import com.example.jingbin.cloudreader.viewmodel.wan.TreeViewModel
import com.google.android.material.tabs.TabLayout
import me.jingbin.bymvvm.base.BaseFragment
import java.util.*

/**
 * wan 最右边的项目分类页
 */
class WanProjectFragment : BaseFragment<TreeViewModel, FragmentContentBinding>() {

    private var mIsFirst = true

    override fun setContent(): Int = R.layout.fragment_content

    override fun onResume() {
        super.onResume()
        if (mIsFirst) {
            showLoading()
            getTree()
            mIsFirst = false
        }
    }

    private fun getTree() {
        viewModel.tree.observe(this, Observer { treeBean ->
            if (treeBean != null && treeBean.data != null && treeBean.data.size > 0) {
                showContentView()
                var position = 0
                for (index in treeBean.data.indices) {
                    if ("开源项目主Tab" == treeBean.data[index].name || 293 == treeBean.data[index].id) {  // TODO 什么作用
                        position = index;
                        break
                    }
                }
                treeBean.data[position]?.children?.size.let {
                    initData(treeBean.data[position])   // 是根据这个位置初始化的
                }
            } else {
                showError()
            }
        })
    }

    private fun initData(treeItemBean: TreeItemBean) {
        val mTitleList: MutableList<String> = ArrayList()
        for (item in treeItemBean.children) {
            mTitleList.add(item.name)  // 最里面一层，名字用来设置tab
        }
        val myAdapter = CommonTabPagerAdapter(childFragmentManager, mTitleList)
        myAdapter.listener = object : TabPagerListener {   // 看不懂 problem TODO  匿名内部类
            override fun getFragment(position: Int): Fragment? {
                // 这里应该是共用一个fragment
                return CategoryArticleFragment.newInstance(treeItemBean.children[position].id, treeItemBean.children[position].name, true)
            }
        }

        bindingView.tabGank.tabMode = TabLayout.MODE_SCROLLABLE   // 设置滚动模式
        bindingView.vpGank.offscreenPageLimit = 2   // 不设置的话默认是1
        bindingView.vpGank.adapter = myAdapter
        myAdapter.notifyDataSetChanged()
        bindingView.tabGank.setupWithViewPager(bindingView.vpGank)  // 绑定

        // 设置初始位置
        bindingView.vpGank.currentItem = 0
    }

    override fun onRefresh() {
        getTree()
    }
}