package me.jingbin.bymvvm.room;

/**
 * @author jingbin
 * @data 2018/4/19
 * @Description
 * TODO 什么意思
 */

public class Injection {

    public static UserDataBaseSource get() {
        UserDataBase database = UserDataBase.getDatabase();
        return UserDataBaseSource.getInstance(new AppExecutors(), database.waitDao());
    }

}
