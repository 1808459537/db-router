package com.zht.dbrouter;

import com.zht.dbrouter.annotation.DBRouter;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;


@Aspect
public class DBRouterJoinPoint {
    private Logger logger = LoggerFactory.getLogger(DBRouterJoinPoint.class);

    @Autowired
    private DBRouterConfig dbRouterConfig;

    @Pointcut("@annotation(com.zht.dbrouter.annotation.DBRouter)")
    public void aopPoint(){}


    @Around("aopPoint() && @annotation(dbRouter)")
    public Object doRouter(ProceedingJoinPoint jp , DBRouter dbRouter) throws Throwable{
        String dbKey = dbRouter.key();
        if (StringUtils.isBlank(dbKey)) throw new RuntimeException("annotation DBRouter key is null！");

        // 计算路由
        String dbKeyAttr = getAttrValue(dbKey, jp.getArgs());
        int size = dbRouterConfig.getDbCount() * dbRouterConfig.getTbCount();  // 2 * 4  = 8
        // 扰动函数
        int idx = (size - 1) & (dbKeyAttr.hashCode() ^ (dbKeyAttr.hashCode() >>> 16)); // 这个是计算的总的位置 比如7表 ， 但是每个库只有4表

        int dbIdx = idx / dbRouterConfig.getTbCount() + 1; // 计算数据库的索引 ， 7表就在2库 ， 需要 + 1；
        int tbIdx = idx - dbRouterConfig.getTbCount() * (dbIdx - 1); // 总数 - 前面库的数量 = 当前库的位置 ： 8 - 4 = 3 ，7表在第二个数据库的 3 号表  这个默认有0表名

        DBContextHolder.setDBKey(String.format("%02d", dbIdx)); // 格式化 ： 如果 dbIdx 是 1，格式化后为 "01"
        DBContextHolder.setTBKey(String.format("%02d", tbIdx));


        logger.info("数据库路由 method：{} dbIdx：{} tbIdx：{}", getMethod(jp).getName(), dbIdx, tbIdx);

        try {
            return jp.proceed();

        } finally {
            DBContextHolder.clearDBKey();
            DBContextHolder.clearTBKey();
        }
    }


    private Method getMethod(JoinPoint jp) throws NoSuchMethodException {
        Signature sig = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature) sig;
        return jp.getTarget().getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
    }



    public String getAttrValue(String attr, Object[] args) {
        String filedValue = null;
        for (Object arg : args) {
            try {
                if (StringUtils.isNotBlank(filedValue)) break;
                filedValue = BeanUtils.getProperty(arg, attr);
            } catch (Exception e) {
                logger.error("获取路由属性值失败 attr：{}", attr, e);
            }
        }
        return filedValue;
    }


}
