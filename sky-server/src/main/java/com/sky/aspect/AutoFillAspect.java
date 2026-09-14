package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/*
 * @Description: 自定义切面，实现公共字段填充
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /*
     * 定义切点，拦截所有Mapper层的所有方法，并且方法上带有@AutoFill注解
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {

    }
    /*
     * 前置通知
     * 在切点方法执行前进行数据填充
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行数据填充");
        //获取当前被拦截的方法参数，即实体对象
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = methodSignature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

         Object[] args =joinPoint.getArgs();
         if (args == null || args.length == 0) {
             return;
         }
         Object object = args[0];
         log.info("对对象进行数据填充：{}",object);
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
         switch (operationType) {
             case INSERT:
                 //填充创建时间、创建人
                 try {
                     Method setCreateTime = object.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
                     Method setUpdateTime = object.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER,LocalDateTime.class);
                     Method setCreateUser = object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,Long.class);
                     Method setUpdateUser = object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);

                     setCreateTime.invoke(object,now);
                     setUpdateTime.invoke(object,now);
                     setCreateUser.invoke(object,currentId);
                     setUpdateUser.invoke(object,currentId);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }


                 break;
             case UPDATE:
                 //填充更新时间、更新人
                 try {
                     Method setUpdateTime = object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                     Method setUpdateUser = object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
                     setUpdateTime.invoke(object,now);
                     setUpdateUser.invoke(object,currentId);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 break;
         }
    }
}
