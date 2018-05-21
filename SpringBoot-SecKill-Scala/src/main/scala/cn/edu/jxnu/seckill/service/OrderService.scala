package cn.edu.jxnu.seckill.service

import org.springframework.stereotype.Service
import cn.edu.jxnu.seckill.dao.OrderDao
import cn.edu.jxnu.seckill.redis.RedisService
import org.springframework.beans.factory.annotation.Autowired
import cn.edu.jxnu.seckill.domain.SeckillOrder
import cn.edu.jxnu.seckill.redis.key.OrderKey
import org.springframework.transaction.annotation.Transactional
import cn.edu.jxnu.seckill.domain.SeckillUser
import cn.edu.jxnu.seckill.vo.GoodsVo
import cn.edu.jxnu.seckill.domain.OrderInfo
import java.util.Date
import scala.language.implicitConversions

/**
 * 订单服务层
 *
 * @author 梦境迷离.
 * @time 2018年5月19日
 * @version v1.0
 */
@Service
class OrderService @Autowired() (orderDao: OrderDao,
    redisService: RedisService) {

    def getSeckillOrderByUserIdGoodsId(userId: Long, goodsId: Long): SeckillOrder = {
        redisService.get(OrderKey.getSeckillOrderByUidGid, "" + userId + "_" + goodsId, classOf[SeckillOrder])
    }

    @Transactional
    def createOrder(user: SeckillUser, goods: GoodsVo): OrderInfo = {
        val orderInfo = new OrderInfo()
        orderInfo.setCreateDate(new Date())
        orderInfo.setDeliveryAddrId(0L)
        orderInfo.setGoodsCount(1)
        orderInfo.setGoodsId(goods.getId())
        orderInfo.setGoodsName(goods.getGoodsName())
        orderInfo.setGoodsPrice(goods.getSeckillPrice())
        orderInfo.setOrderChannel(1)
        orderInfo.setStatus(0)
        orderInfo.setUserId(user.getId())
        orderDao.insert(orderInfo)
        val seckillOrder = new SeckillOrder()
        seckillOrder.setGoodsId(goods.getId())
        seckillOrder.setOrderId(orderInfo.getId())
        seckillOrder.setUserId(user.getId())
        orderDao.insertSeckillOrder(seckillOrder)
        redisService.set(OrderKey.getSeckillOrderByUidGid, "" + user.getId() + "_" + goods.getId(), seckillOrder)
        orderInfo
    }

    def getOrderById(orderId: Long): OrderInfo = {
        orderDao.getOrderById(orderId)
    }

    def deleteOrders() {
        orderDao.deleteOrders()
        orderDao.deleteSeckillaOrders()
    }

}