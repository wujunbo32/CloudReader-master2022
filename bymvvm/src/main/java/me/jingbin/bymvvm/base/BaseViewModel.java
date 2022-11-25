package me.jingbin.bymvvm.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author jingbin
 * @data 2018/5/28
 */
public class BaseViewModel extends AndroidViewModel {

    private CompositeDisposable mCompositeDisposable;

    public BaseViewModel(@NonNull Application application) {
        super(application);
    }


    protected <T> void execute(Observable<T> observable, Observer<T> observer) {// 什么意思 TODO
        //MainViewModel中有观察者方法实现
        observable  // 被观察者
                .throttleFirst(500, TimeUnit.MILLISECONDS)  // throttleFirst :在某段时间内，只发送该段时间内第1次事件(假如一个按钮1秒内点了3次 ,第一次显示,后2次不显示)
                .subscribeOn(Schedulers.io())  // // 订阅触发后，在子线程中进行请求
                .observeOn(AndroidSchedulers.mainThread()) // 在UI线程中暗中观察并及时消费
                .subscribe(observer);  // // 默认最先调用复写的 onSubscribe（）
    }

    protected void addDisposable(Disposable disposable) {
        if (this.mCompositeDisposable == null) {
            this.mCompositeDisposable = new CompositeDisposable();
        }
        this.mCompositeDisposable.add(disposable);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (this.mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            this.mCompositeDisposable.clear();
        }
    }
}
