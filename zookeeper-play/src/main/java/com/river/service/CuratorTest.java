package com.river.service;


import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

/**
 * curator 分布式锁
 */
@Slf4j
public class CuratorTest {

    private CuratorFramework client;

    @Before
    public void init() {
        this.client = CuratorFrameworkFactory.newClient("120.78.215.216:2181", 3000, 3000, new ExponentialBackoffRetry(3000, 3));
        this.client.start();
    }


    public void tet(String path) throws Exception {

        NodeCache dataWatch = new NodeCache(client, path);
        dataWatch.getListenable().addListener(() -> {
            log.info(String.format("Current watched node's path is -> %s", dataWatch.getCurrentData().getPath()));
            log.info("data is ->" + new String(dataWatch.getCurrentData().getData()));
        });
        try {
            dataWatch.start();
        } catch (Exception e) {
        }
        log.info("注册监听器");

        Thread.sleep(5000);

        //client.setData().forPath(path, "data3".getBytes());
        // Thread.sleep(2000);
    }

    //锁节点路径
    private final static String DISTRIBUTE_LOCK_PRE = "/locker/curator/distribute_lock_%s";

    /**
     * 获取分布式锁对象
     * @param key
     * @return
     */
    public InterProcessMutex getLockInstance(String key) {
        return new InterProcessMutex(client, String.format(DISTRIBUTE_LOCK_PRE, key));
    }

    /**
     * 测试分布式锁
     *
     * 提交2个线程模拟两个服务节点
     * @throws Exception
     */
    @Test
    public void testDistributeLock() throws Exception {
        CompletableFuture<Void> voidCompletableFuture1 = CompletableFuture.runAsync(() -> {
            InterProcessMutex mutex = getLockInstance("testLock");
            try {
                log.info("尝试获取锁");
                mutex.acquire();
                log.info("获取锁成功");
                System.out.println("锁被持有时，存在的节点有-> "+client.getChildren().forPath(String.format(DISTRIBUTE_LOCK_PRE, "testLock")));
                //模拟任务执行
                Thread.sleep(5000);
                mutex.release();
                log.info("释放锁对象");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
            InterProcessMutex mutex = getLockInstance("testLock");
            try {
                log.info("尝试获取锁");
                //保证让上面的线程先持有锁，当前线程等待锁
                Thread.sleep(2000);
                mutex.acquire();
                System.out.println("锁被释放后，当前线程持有锁时，存在的节点有-> "+client.getChildren().forPath(String.format(DISTRIBUTE_LOCK_PRE, "testLock")));
                log.info("获取锁成功");
                mutex.release();
                log.info("释放锁对象");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        CompletableFuture<Void> voidCompletableFuture3 = CompletableFuture.runAsync(() -> {
            try {
                //等待3s，使得当前处于第一个线程持有锁，另外一个线程在等待锁释放的场景
                Thread.sleep(3000);
                System.out.println("当锁被持有，有线程等待锁释放的时候，存在的节点有-> "+client.getChildren().forPath(String.format(DISTRIBUTE_LOCK_PRE, "testLock")));

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        CompletableFuture.allOf(voidCompletableFuture, voidCompletableFuture1, voidCompletableFuture3).join();
    }
    /**
     *
     *
     *
     1.首先在分布式锁节点curator下面创建临时顺序节点（CreateMode.EPHEMERAL_SEQUENTIAL），此创建方法为递归；
     2.获取所节点curator下面所有的节点
     3.判断当前创建的节点是否是所有节点中序号最小的？
     如果是，则说明当前线程获取锁；
     否则，获取比当前创建节点小的节点，并调用exist（），同时对其注册监听器；
     4.当被监听的节点删除了，再次判断当前线程创建的节点是否是最小的，直到获取锁为止；
     5.执行任务，完毕后释放锁；触发其他等待锁线程重复执行步骤4


    1. zookeeper实现的分布式锁是公平锁，而redis分布式锁时非公平的
        zookeeper的分布式锁是基于创建临时序列节点，通过判断当前创建的节点序号是否是最小的来决定是否获取锁，
     因此锁是被顺序获取的；
        redis的分布式锁是基于共享变量实现的，基于redis的setnx()，通过对键key_x设置一个随机数及expiredTime；
     其他线程在锁被释放时都可以尝试获取锁，执行setnx（）方法；

     2.锁持有者崩溃问题
        zookeeper分布式锁创建的是临时节点，当锁持有客户端崩溃时，则此客户端无法维持与zookeeper的心跳，
     该客户端创建的临时节点会被删除，此时其他客户端可以获取到锁；
        redis分布式锁当客户端崩溃时，锁不会被释放，直至锁超时；

     3. 锁服务提供者崩溃问题
        当然，当zookeeper和redis都是单点部署时，不论怎样都会有问题；生产环境是不可能出现单点部署的；
        zookeeper集群数据同步，在leader接收到请求时，将事物加入事物队列，并广播给follower；follower接收
     到事物后加入事物队列，并发送leader准备提交事物的通知；leader接收到半数通知后，提交事物，
     并向follower发送提交事物请求，此时follower提交事物；返回请求处理结果；同时zookeeper集群数据同步完成；
     假设leader宕机，新的leader被选举出来后，也会存在之前leader锁拥有的信息；不会产生问题；
        redis集群，master接收到请求后返回处理结果，redis节点主从复制是异步执行的；
     当master宕机之后，新任master有可能并不拥有分布式锁的信息，非锁持有对象会有机会同时获取该锁执行任务，
     导致本该串行执行的2个线程存在并行的风险；

     4.消耗问题
        zookeeper的获取锁是基于监听实现的，当锁被释放时，会触发该节点的监听者判断当前线程所创建的节点
     是否是最小的序号，若是则获取锁，否则继续监听比该节点需要小的节点；
        redis分布式锁的获取是基于不断地尝试获取锁而实现的，因此需要不断地尝试，开销较zookeeper分布式锁相比较大；

     5.可重入锁
        zookeeper分布式锁与redis分布式锁均是可重入锁
*/






}
