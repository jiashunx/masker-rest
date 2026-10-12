package io.github.jiashunx.masker.rest.framework.global;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jiashunx.masker.rest.framework.MRestContext;
import io.github.jiashunx.masker.rest.framework.model.MRestServerThreadModel;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author jiashunx
 */
public class SharedObjects {

    private SharedObjects() {}

    private static final ThreadLocal<MRestServerThreadModel> SERVER_THREAD_MODEL = new ThreadLocal<>();

    public static MRestServerThreadModel getServerThreadModel() {
        return SERVER_THREAD_MODEL.get();
    }

    public static ObjectMapper getObjectMapperFromThreadLocal() {
        ObjectMapper objectMapper = null;
        MRestServerThreadModel threadModel = getServerThreadModel();
        if (threadModel != null) {
            MRestContext restContext = threadModel.getRestContext();
            if (restContext != null) {
                Supplier<ObjectMapper> objectMapperSupplier = restContext.getObjectMapperSupplier();
                if (objectMapperSupplier != null) {
                    objectMapper = objectMapperSupplier.get();
                }
            }
        }
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        return objectMapper;
    }

    public static void resetServerThreadModel(MRestServerThreadModel threadModel) {
        SERVER_THREAD_MODEL.set(Objects.requireNonNull(threadModel).assertNotNull());
    }

    public static void clearServerThreadModel() {
        SERVER_THREAD_MODEL.remove();
    }

    /**
     * 捕获当前线程绑定的线程模型(用于跨线程异步处理时显式传播上下文).
     * @return 当前线程模型, 未绑定时返回null
     */
    public static MRestServerThreadModel captureServerThreadModel() {
        return getServerThreadModel();
    }

    /**
     * 将线程模型绑定到当前线程, 返回绑定前的模型(可能为null)以便调用方恢复.
     * @param threadModel 待绑定模型(为null时清除当前线程绑定)
     * @return 绑定前的线程模型
     */
    public static MRestServerThreadModel setServerThreadModel(MRestServerThreadModel threadModel) {
        MRestServerThreadModel previous = getServerThreadModel();
        if (threadModel == null) {
            clearServerThreadModel();
        } else {
            resetServerThreadModel(threadModel);
        }
        return previous;
    }

    /**
     * 在指定线程模型上下文中执行任务, 结束后恢复原上下文.
     * 用于业务线程池/异步处理时显式传播请求上下文.
     * @param threadModel 线程模型(可为null)
     * @param runnable 待执行任务
     */
    public static void runWithServerThreadModel(MRestServerThreadModel threadModel, Runnable runnable) {
        MRestServerThreadModel previous = setServerThreadModel(threadModel);
        try {
            runnable.run();
        } finally {
            setServerThreadModel(previous);
        }
    }

}
