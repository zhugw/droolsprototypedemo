package com.tn.demo;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhuguowei on 3/20/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext-drools.xml"})
public class KSessionScopeTest {
    private Logger logger = LoggerFactory.getLogger(KSessionScopeTest.class);
    @Autowired
    private ApplicationContext applicationContext;
    @Test
    public void test_normal_bean_prototype() throws InterruptedException {
        final int nThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        CountDownLatch countDownLatch = new CountDownLatch(nThreads);
        ConcurrentHashMap.KeySetView<Object, Boolean> fooSet = ConcurrentHashMap.newKeySet();
        for (int i = 0; i < nThreads; i++) {
            executorService.submit(()->{
                Object foo = applicationContext.getBean("foo");
                boolean add = fooSet.add(System.identityHashCode(foo));
                if(!add){
                    logger.warn("The bean: {} is repeated",foo);
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();

        Assert.assertEquals(nThreads,fooSet.size());
    }
    @Test
    public void test_ksession_prototype_repeated() throws InterruptedException {
        final int nThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        CountDownLatch countDownLatch = new CountDownLatch(nThreads);
        ConcurrentHashMap.KeySetView<Object, Boolean> ksessionSet = ConcurrentHashMap.newKeySet();
        for (int i = 0; i < nThreads; i++) {
            executorService.submit(()->{
                KieSession ksession = (KieSession) applicationContext.getBean("ksession");
                boolean add = ksessionSet.add(System.identityHashCode(ksession));
                if(!add){
                    logger.warn("The ksession: {} is repeated",ksession);
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        Assert.assertTrue(ksessionSet.size()<nThreads);
    }

}
