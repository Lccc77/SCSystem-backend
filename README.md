# SCSystem-backend
Group-7 for Software-Practice-Week



## **==TODO：==**

 1. 搭建 Redis 一主二从架构，保障项目高可用，在优惠券秒杀环节缓存能够承接高并发，同时设置主从 读写分离，两个从节点 readONLY 且能达到对 **读请求 负载均衡** 效果；（哨兵集群要太麻烦咱就不监控了，hh主要是怎么达到 **读请求** 对两个从节点 做出 **轮询** 效果阿，求指教啊 熠兄，若是实现了可以用 test 里的一个函数跑一下看看控制台打印出的日志 是不是读请求对  从节点 挨个访问的，，md文件里有代码位置截图；
  
 2. 在项目启动时创建 一个 **ThreadLocal 线程池**，在与客户端建立服务连接后从池中取用 ThreadLocal 减少当场创建开销（因为每个客户端登录成功后都需要将其 个人 信息 保存至 ThreadLocal当中，，，，，该线程池如何 **维护** 且能够 **安全使用**
