package cn.edu.jxnu.seckill.controller

import cn.edu.jxnu.seckill.domain.SeckillUser
import cn.edu.jxnu.seckill.redis.RedisService
import cn.edu.jxnu.seckill.result.Result
import cn.edu.jxnu.seckill.service.SeckillUserService
import io.swagger.annotations.{Api, ApiImplicitParam, ApiOperation}
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}

/**
 * 用户控制器
 *
 * 压测专用
 * @author 梦境迷离.
 * @time 2018年5月21日
 * @version v1.0
 */
@RestController
@RequestMapping(Array("/user"))
@Api(value = "测试用户controller", tags = { Array("测试用户接口") })
class UserController @Autowired() (userService: SeckillUserService,
    redisService: RedisService) {

    private final val log = LoggerFactory.getLogger(classOf[UserController])

    /**
     * QPS:366.6 1000 * 10
     */
    @ApiOperation(value = "测试用户", notes = { "测试用户" })
    @ApiImplicitParam(name     = "seckillUser", value = "SeckillUser", required = true, dataType = "SeckillUser")
    @GetMapping(Array("/info"))
    def info(model: Model, user: SeckillUser): Result[SeckillUser] = {

        log.info("用户user：" + user.toString())
        Result.success(user)
    }

}