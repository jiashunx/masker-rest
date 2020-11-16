package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.type.MRestNettyThreadType;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * thread factory for masker-rest framework.
 * @author jiashunx
 */
public class MRestThreadFactory implements ThreadFactory {

    private final ThreadGroup threadGroup;
    private final String namePrefix;
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    public MRestThreadFactory(MRestNettyThreadType threadType, int listenPort) {
        SecurityManager s = System.getSecurityManager();
        this.threadGroup = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.namePrefix = String.format("restpool-%s-%d-thread-", threadType.name(), listenPort);
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(threadGroup, r, namePrefix + threadNumber.getAndIncrement(), 0);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }

}
