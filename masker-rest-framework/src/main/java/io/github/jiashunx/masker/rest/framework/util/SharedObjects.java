package io.github.jiashunx.masker.rest.framework.util;

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

}
