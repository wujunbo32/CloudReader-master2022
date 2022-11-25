package com.example.jingbin.cloudreader.ui.wan.child

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.Observer
import com.example.jingbin.cloudreader.R
import com.example.jingbin.cloudreader.adapter.WanAndroidAdapter
import com.example.jingbin.cloudreader.app.RxCodeConstants
import com.example.jingbin.cloudreader.data.UserUtil
import com.example.jingbin.cloudreader.databinding.FragmentSquareBinding
import com.example.jingbin.cloudreader.utils.RefreshHelper
import com.example.jingbin.cloudreader.viewmodel.wan.WanCenterViewModel
import io.reactivex.functions.Consumer
import me.jingbin.bymvvm.base.BaseFragment
import me.jingbin.bymvvm.rxbus.RxBus
import me.jingbin.bymvvm.rxbus.RxBusBaseMessage

/**
 * 广场
 */
class SquareFragment : BaseFragment<WanCenterViewModel, FragmentSquareBinding>() {

    private var isFirst = true
    private lateinit var activity: Activity
    private lateinit var mAdapter: WanAndroidAdapter

    override fun setContent(): Int = R.layout.fragment_square

    companion object {
        fun newInstance(): SquareFragment {
            return SquareFragment()
        }
    }

    /*周期方法*/
    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity

    }
    /*周期方法*/
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.mPage = 0
    }
    /*周期方法*/
    override fun onResume() {
        super.onResume()
        if (isFirst) {
            showLoading()  //显示加载中状态
            initRv()  // 上下拉 悬浮按钮
            getWendaData()  // 获取广场列表数据
            initRxBus()
            isFirst = false    // 只执行一次
        }
    }

    private fun initRv() {
        RefreshHelper.initLinear(bindingView.xrvAndroid, true, 1)
        mAdapter = WanAndroidAdapter(activity)   // 通用的列表子项的操作
        mAdapter.isNoShowChapterName = true    // 不显示类别信息
        mAdapter.isNoImage = false    // 列表中是否显示图片
        bindingView.xrvAndroid.adapter = mAdapter   // TODO 为什么可以 .adapter
        bindingView.xrvAndroid.setOnRefreshListener {
            viewModel.mPage = 0     // 下拉刷新都是加载最新的
            getWendaData()
        }
        bindingView.xrvAndroid.setOnLoadMoreListener(true) { // 下拉更多
            ++viewModel.mPage
            getWendaData()
        }
        bindingView.tvPublish.setOnClickListener {   //分享悬浮按钮
            if (UserUtil.isLogin(activity)) {
                PublishActivity.start(activity)
            }
        }
    }

    /*获取广场列表数据*/
    private fun getWendaData() {
        viewModel.getUserArticleList().observe(this, Observer {
            bindingView.xrvAndroid.isRefreshing = false
            if (it != null && it.isNotEmpty()) {   //有数据 it 代表 List<ArticlesBean>
                showContentView()
                if (viewModel.mPage == 0) {
                    mAdapter.setNewData(it)
                } else {
                    mAdapter.addData(it)
                    bindingView.xrvAndroid.loadMoreComplete()
                }

            } else {
                if (viewModel.mPage == 0) {  // 不为null,但没有数据
                    if (it != null) {
                        showEmptyView("没找到广场里的内容~")
                    } else {
                        showError()
                        if (viewModel.mPage > 1) viewModel.mPage--
                    }
                } else {
                    bindingView.xrvAndroid.loadMoreEnd()   // TODO
                }
            }
        })
    }

    override fun onRefresh() {
        getWendaData()
    }

    private fun initRxBus() {
        val subscribe = RxBus.getDefault().toObservable(RxCodeConstants.REFRESH_SQUARE_DATA, RxBusBaseMessage::class.java)
                .subscribe(Consumer {
                    showLoading()
                    viewModel.mPage = 0
                    getWendaData()
                })
        addSubscription(subscribe)
    }
}