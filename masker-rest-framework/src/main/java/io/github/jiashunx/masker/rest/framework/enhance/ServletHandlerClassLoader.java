package io.github.jiashunx.masker.rest.framework.enhance;

import io.github.jiashunx.masker.rest.framework.util.IOUtils;
import io.github.jiashunx.masker.rest.framework.util.MRestUtils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author jiashunx
 */
public class ServletHandlerClassLoader extends URLClassLoader {

    private ServletHandlerClassLoader() {
        super(new URL[]{}, ServletHandlerClassLoader.class.getClassLoader());
    }

    private static class Inner {
        private static final ServletHandlerClassLoader INSTANCE = new ServletHandlerClassLoader();
    }

    public static ServletHandlerClassLoader getInstance() {
        return Inner.INSTANCE;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            String filePath = MRestUtils.getFrameworkTempDirPath() + name.replace(".", "/") + ".class";
            byte[] bytes = IOUtils.readBytes(new File(filePath));
            return defineClass(name, bytes, 0, bytes.length);
        } catch (Throwable throwable) {
            throw new ClassNotFoundException("", throwable);
        }
    }

}
