package com.zht.dbrouter;

import com.zht.dbrouter.annotation.DBRouter;
import org.junit.Test;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;



class DbrouterApplicationTests {
    public static void main(String[] args) {
        System.out.println("hi");
    }

    @Test
    public void test_db_hash() {
        String key = "小傅哥";

        int dbCount = 2, tbCount = 32;
        int size = dbCount * tbCount;
        // 散列
        int idx = (size - 1) & (key.hashCode() ^ (key.hashCode() >>> 16));

        System.out.println(idx);

        int dbIdx = idx / tbCount + 1;
        int tbIdx = idx - tbCount * (dbIdx - 1);

        System.out.println(dbIdx);
        System.out.println(tbIdx);

    }

    @Test
    public void test_str_format() {
        System.out.println(String.format("db%02d", 1));
        System.out.println(String.format("_%02d", 25));
    }

    @Test
    public void test_annotation() throws NoSuchMethodException {
        Class<IUserDao> iUserDaoClass = IUserDao.class;
        Method method = iUserDaoClass.getMethod("insertUser", String.class);

        DBRouter dbRouter = method.getAnnotation(DBRouter.class);

        System.out.println(dbRouter.key());

    }


}
