package com.tjpu.sp.aop;

import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.common.anticontrol.NotResubmit;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import net.sf.json.JSONObject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * @Description: 接口重复请求处理
 * @Param:  
 * @return:  
 * @Author: lip
 * @Date: 2022/9/22 8:59
 */ 
@Aspect
@Component
public class LockMethodAOP {
    private static final Logger log = LoggerFactory.getLogger(LockMethodAOP.class);

    /**
     * @Description:  
     * @Param:  
     * @return:  
     * @Author: lip
     * @Date: 2022/11/10 11:21
     */ 
    @Around("execution(public * *(..)) && @annotation(com.tjpu.sp.common.anticontrol.NotResubmit)")
    public Object interceptor(ProceedingJoinPoint pjp) throws Throwable {
        // 获取到这个注解
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        NotResubmit lock = method.getAnnotation(NotResubmit.class);
        final String lockKey = generateKey(pjp);
        // 上锁
        final boolean success = RedisTemplateUtil.RLock(lockKey,lock.delaySeconds());
        if (!success) {
            // 这里也可以改为自己项目自定义的异常抛出
            return AuthUtil.reSubmit("请求频繁");
        }
        return pjp.proceed();
    }

    private String generateKey(ProceedingJoinPoint pjp) {

        final HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        StringBuilder sb = new StringBuilder();

        if (request.getHeader("token") != null) {
            String redisKey = request.getHeader("token");
            sb.append(redisKey);
        }
        Signature signature = pjp.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        sb.append(pjp.getTarget().getClass().getName())//类名
                .append(method.getName());//方法名
        for (Object o : pjp.getArgs()) {
            sb.append(o.toString());
        }
        return DigestUtils.md5DigestAsHex(sb.toString().getBytes(Charset.defaultCharset()));
    }
}