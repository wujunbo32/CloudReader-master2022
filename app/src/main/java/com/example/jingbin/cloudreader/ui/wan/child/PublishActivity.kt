package com.example.jingbin.cloudreader.ui.wan.child

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import com.example.jingbin.cloudreader.R
import com.example.jingbin.cloudreader.app.App
import com.example.jingbin.cloudreader.app.RxCodeConstants
import com.example.jingbin.cloudreader.data.UserUtil
import com.example.jingbin.cloudreader.databinding.ActivityPublishBinding
import com.example.jingbin.cloudreader.ui.LoadingActivity
import com.example.jingbin.cloudreader.ui.MainActivity
import com.example.jingbin.cloudreader.ui.WebViewActivity
import com.example.jingbin.cloudreader.utils.BaseTools
import com.example.jingbin.cloudreader.utils.ToastUtil
import com.example.jingbin.cloudreader.view.MyTextWatch
import com.example.jingbin.cloudreader.viewmodel.wan.PublishViewModel
import me.jingbin.bymvvm.base.BaseActivity
import me.jingbin.bymvvm.rxbus.RxBus
import me.jingbin.bymvvm.rxbus.RxBusBaseMessage

/**
 * 分享文章
 * Created by jingbin on 2021/1/17.
 */
class PublishActivity : BaseActivity<PublishViewModel, ActivityPublishBinding>() {
//    var title:String = "" //和布局控件ID相同的话会覆盖布局控件ID
    private var clipContent: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish)
        title = "分享文章"
        showContentView()    // 显示内容
        bindingView.viewModel = viewModel   // 即传入的PublishViewModel
        initView()
        handleShortcuts()
    }

    private fun initView() {
        bindingView.etTitle.postDelayed({
            bindingView.etTitle.requestFocus()   // 自动获取焦点
            BaseTools.showSoftKeyBoard(this, bindingView.etTitle)   //自动获取焦点不会弹出软键盘
        }, 150)
        // 监听文本变化
        bindingView.etTitle.addTextChangedListener(object : MyTextWatch() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val title = bindingView.etTitle.text.toString().trim()
                viewModel.handleIcon(title, clipContent, 1, bindingView.ivUp)  // 就是改变图标逻辑处理，输入时变成X符号
            }
        })
        bindingView.etLink.addTextChangedListener(object : MyTextWatch() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val link = bindingView.etLink.text.toString().trim()
                viewModel.handleIcon(link, clipContent, 2, bindingView.ivDown)
            }
        })
        bindingView.ivUp.setOnClickListener {
            val title = bindingView.etTitle.text.toString().trim()   // 进入界面一开始为null，执行粘贴；这时图标变成×，再点击（相当于点击×）获取title不为空，,执行删除逻辑
            if (title.isNotBlank()) {
                bindingView.etTitle.text.clear()
            } else {    // handleIcon（）只是图标显示逻辑，具体删除/复制操作在这里
                bindingView.etTitle.text = Editable.Factory.getInstance().newEditable(clipContent)
                bindingView.etTitle.setSelection(clipContent.length)
            }
        }
        bindingView.ivDown.setOnClickListener {
            val link = bindingView.etLink.text.toString().trim()
            if (link.isNotBlank()) {
                bindingView.etLink.text.clear()
            } else {
                bindingView.etLink.text = Editable.Factory.getInstance().newEditable(clipContent)
                bindingView.etLink.setSelection(clipContent.length)
            }
        }
    }

    /**分享按钮*/
    fun publish(view: View) {
        if (!MainActivity.isLaunch && !UserUtil.isLogin(this)) {
            return
        }
        viewModel.pushArticle().observe(this, Observer {
            if (it != null && it) {  // TODO 没看懂
                // 成功
                ToastUtil.showToast("分享成功")
                this@PublishActivity.finish()// TODO
                if (!MainActivity.isLaunch) {
                    MainActivity.start(this)
                }
                // TODO  RxBus
                RxBus.getDefault().post(RxCodeConstants.LOGIN, true)
                RxBus.getDefault().post(RxCodeConstants.REFRESH_SQUARE_DATA, RxBusBaseMessage())
            }
        })
    }

    override fun onResume() {
        super.onResume()
        bindingView.etLink.postDelayed({
            clipContent = BaseTools.getClipContent()   // 剪切板内容
            if (clipContent.isNotBlank() && "null" != clipContent) {

                val title = bindingView.etTitle.text.toString().trim()
                if (title.isBlank()) {
                    if (!clipContent.contains("http")) {
                        viewModel.isShowTitleIv.set(true)
                        bindingView.ivUp.setImageResource(R.drawable.icon_paste)
                    } else {
                        viewModel.isShowTitleIv.set(false)
                    }
                }

                val link = bindingView.etLink.text.toString().trim()
                if (link.isBlank()) {
                    if (clipContent.contains("http")) {   // 必须先有http复制才会显示图标
                        viewModel.isShowLinkIv.set(true)
                        bindingView.ivDown.setImageResource(R.drawable.icon_paste)
                    } else {
                        viewModel.isShowLinkIv.set(false)
                    }
                }
            }
        }, 150)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_web, menu)// TODO 为什么直接用
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.actionbar_web) {
            WebViewActivity.loadUrl(this, "https://www.wanandroid.com/user_article/add", "分享文章")
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // xml事件响应方法
    fun openLink(view: View) {
        if (!viewModel.link.get().isNullOrBlank()) {  // 得到地址
            WebViewActivity.loadUrl(view.context, viewModel.link.get(), "加载中...")
        } else {
            ToastUtil.showToast("请输入链接")
        }
    }

    /**
     * 处理快捷方式进来
     */
    private fun handleShortcuts() {   // 正常情况下应该是Launch启动isLauch为true
        if (!MainActivity.isLaunch) {
            App.isShortcuts = true    // 具体作用没看懂， TODO problem
            findViewById<Toolbar>(R.id.tool_bar).setNavigationOnClickListener {   // 这里是处理哪一个响应 TODO problem
                BaseTools.handleFinish(this)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        BaseTools.handleFinish(this)
    }

    companion object {
        fun start(context: Activity) {
            val intent = Intent(context, PublishActivity::class.java)
            context.startActivity(intent)
//            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(context, view, CommonUtils.getString(R.string.transition_publish_bt))
//            ActivityCompat.startActivity(context, intent, options.toBundle())
        }
    }
}