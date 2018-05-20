package cn.edu.jxnu.seckill.access

import scala.language.implicitConversions
import org.springframework.stereotype.Service
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import cn.edu.jxnu.seckill.service.SeckillUserService
import cn.edu.jxnu.seckill.redis.RedisService
import org.springframework.beans.factory.annotation.Autowired
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.web.method.HandlerMethod
import cn.edu.jxnu.seckill.result.CodeMsg
import cn.edu.jxnu.seckill.redis.key.AccessKey
import com.alibaba.fastjson.JSON
import cn.edu.jxnu.seckill.result.Result
import cn.edu.jxnu.seckill.service.SeckillUserService
import cn.edu.jxnu.seckill.service.SeckillUserService
import org.apache.commons.lang3.StringUtils
import cn.edu.jxnu.seckill.domain.SeckillUser
import cn.edu.jxnu.seckill.service.SeckillUserService
import cn.edu.jxnu.seckill.service.SeckillUserService

/**
 * 访问拦截器去
 *
 * Java
 *
 * @author 梦境迷离.
 * @time 2018年5月20日
 * @version v1.0
 */
@Service
class AccessInterceptor @Autowired() (userService: SeckillUserService, redisService: RedisService)
    extends HandlerInterceptorAdapter {

    override def preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object): Boolean =
        {
            if (handler.isInstanceOf[HandlerMethod]) {
                val user = getUser(request, response)
                UserContext.setUser(user)
                val hm = handler.asInstanceOf[HandlerMethod]
                val accessLimit = hm.getMethodAnnotation(classOf[AccessLimit])
                if (accessLimit == null)
                    true
                val seconds = accessLimit.seconds()
                val maxCount = accessLimit.maxCount()
                val needLogin = accessLimit.needLogin()
                var key = request.getRequestURI()
                if (needLogin) {
                    if (user == null) {
                        render(response, CodeMsg.SESSION_ERROR)
                        false
                    }
                    key += "_" + user.getId()
                } else {
                    //do nothing
                }
                val ak = AccessKey.withExpire(seconds)
                val count = redisService.get(ak, key, classOf[Integer])
                if (count == null) {
                    redisService.set(ak, key, 1)
                } else if (count < maxCount) {
                    redisService.incr(ak, key)
                } else {
                    render(response, CodeMsg.ACCESS_LIMIT_REACHED)
                    false
                }
            }
            true
        }

    private def render(response: HttpServletResponse, cm: CodeMsg) {
        response.setContentType("application/jsoncharset=UTF-8")
        val out = response.getOutputStream()
        val str = JSON.toJSONString(Result.error(cm), true)
        out.write(str.getBytes("UTF-8"))
        out.flush()
        out.close()
    }

    private def getUser(request: HttpServletRequest, response: HttpServletResponse): SeckillUser = {
        var token: Any = null
        val paramToken = request.getParameter(SeckillUserService.COOKI_NAME_TOKEN)
        val cookieToken = getCookieValue(request, SeckillUserService.COOKI_NAME_TOKEN)
        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken))
            null
        if (StringUtils.isEmpty(paramToken))
            token = cookieToken
        else
            token = paramToken
        userService.getByToken(response, token.asInstanceOf[String])
    }

    private def getCookieValue(request: HttpServletRequest, cookiName: String): String = {
        val cookies = request.getCookies()
        if (cookies == null || cookies.length <= 0)
            null
        for (cookie <- cookies) {
            if (cookie.getName().equals(cookiName))
                cookie.getValue()
        }
        null
    }

}