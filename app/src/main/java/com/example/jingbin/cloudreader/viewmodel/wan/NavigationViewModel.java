package com.example.jingbin.cloudreader.viewmodel.wan;

import android.app.Application;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;

import me.jingbin.bymvvm.base.BaseViewModel;
import com.example.jingbin.cloudreader.bean.wanandroid.ArticlesBean;
import com.example.jingbin.cloudreader.bean.wanandroid.NaviJsonBean;
import com.example.jingbin.cloudreader.http.HttpClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author jingbin
 * @data 2018/12/3
 * @Description wanandroid导航数据的ViewModel
 */

public class NavigationViewModel extends BaseViewModel {

    // content数据
    private final MutableLiveData<List<ArticlesBean>> data = new MutableLiveData<>();
    // title数据
    private final MutableLiveData<List<NaviJsonBean.DataBean>> dataTitle = new MutableLiveData<>();
    // content 的 title position  外面的i对应的 titlePositions.get(i)
    private final MutableLiveData<List<Integer>> titlePositions = new MutableLiveData<>();
    // 用来滑动定位 第一个Integer为内容的position，第二个Integer为对应的分类position
    private HashMap<Integer, Integer> hashMap;

    public MutableLiveData<List<ArticlesBean>> getData() {
        return data;
    }

    public MutableLiveData<List<NaviJsonBean.DataBean>> getDataTitle() {
        return dataTitle;
    }

    public MutableLiveData<List<Integer>> getTitlePositions() {
        return titlePositions;
    }

    public HashMap<Integer, Integer> getHashMap() {
        return hashMap;
    }

    public NavigationViewModel(@NonNull Application application) {
        super(application);
        hashMap = new HashMap<>();
    }

    public void getNavigationJson() {
        Disposable subscribe = HttpClient.Builder.getWanAndroidServer().getNaviJson()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<NaviJsonBean>() {
                    @Override
                    public void accept(NaviJsonBean naviJsonBean) throws Exception {  //NaviJsonBean数据总集合
                        if (naviJsonBean != null
                                && naviJsonBean.getData() != null
                                && naviJsonBean.getData().size() > 0) {  // 左边RV的标题数据判断

                            // title
                            dataTitle.setValue(naviJsonBean.getData()); // 标题
                            // content
                            ArrayList<ArticlesBean> list = new ArrayList<>();  //ArticlesBean  右边RV标题对应内容
                            // content部分对应分类的position
                            ArrayList<Integer> positions = new ArrayList<>();  // 记录对应标题内容的子项的数目
                            for (int i = 0; i < naviJsonBean.getData().size(); i++) {  // 遍历标题
                                NaviJsonBean.DataBean dataBean = naviJsonBean.getData().get(i);  // 得到具体位置的标题数据集
                                ArticlesBean bean = new ArticlesBean();  // 标题对应内容，但是好像下面只初始化了自己添加的字段标题
                                bean.setNavigationName(dataBean.getName());  // 把dataBean的标题提取出来，放到内容ArticlesBean中自己添加的标题字段中保存
                                positions.add(list.size());  // 什么作用 TODO problem  应该是对应内容中每个子项的数目
                                if (i != 0) {
                                    // 最后一个item可能有一个或两个
                                    hashMap.put(list.size() - 1, i - 1);  // 不懂 TODO problem
                                    hashMap.put(list.size() - 2, i - 1);
                                }
                                hashMap.put(list.size(), i);
                                list.add(bean); // 添加标题 但ArticlesBean 其它字段没有值
                                list.addAll(dataBean.getArticles()); // 添加上面标题对应的内容（但标题内容为空） ；就形成了右边RV的数据表现形式
                            }
                            data.setValue(list);   // 内容数据设置，包含了自定义个标题字段设置
                            titlePositions.setValue(positions);  // 是个数吗 ？ TODO problem
                        } else {
                            data.setValue(null);
                            dataTitle.setValue(null);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        data.setValue(null);
                        dataTitle.setValue(null);
                    }
                });
        addDisposable(subscribe);
    }
}
