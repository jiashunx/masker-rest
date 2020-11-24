package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.model.MRestServerThreadModel;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class SharedObjects {

    private SharedObjects() {}

    private static final ThreadLocal<MRestServerThreadModel> SERVER_THREAD_MODEL = new ThreadLocal<>();

    public static MRestServerThreadModel getServerThreadModel() {
        return SERVER_THREAD_MODEL.get();
    }

    public static void resetServerThreadModel(MRestServerThreadModel threadModel) {
        SERVER_THREAD_MODEL.set(Objects.requireNonNull(threadModel).assertNotNull());
    }

    public static void clearServerThreadModel() {
        SERVER_THREAD_MODEL.remove();
    }

}
