package io.github.jiashunx.masker.rest.framework.enhance;

import io.github.jiashunx.masker.rest.framework.exception.MRestMappingException;
import io.github.jiashunx.masker.rest.framework.servlet.MRestServlet;
import io.github.jiashunx.masker.rest.framework.type.MRestHandlerType;
import io.github.jiashunx.masker.rest.framework.util.FileUtils;
import io.github.jiashunx.masker.rest.framework.util.IOUtils;
import io.github.jiashunx.masker.rest.framework.util.MRestUtils;
import jdk.internal.org.objectweb.asm.*;

import java.io.File;
import java.net.MalformedURLException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jiashunx
 */
public class ServletHandlerClassGenerator implements Opcodes {

    private static final AtomicInteger counter = new AtomicInteger(0);

    public static final String CLASS_IDENTIFIER_REQ = "Lio/github/jiashunx/masker/rest/framework/MRestRequest;";
    public static final String CLASS_IDENTIFIER_RESP = "Lio/github/jiashunx/masker/rest/framework/MRestResponse;";
    public static final String METHOD_DESCRIPTOR_0 = "()V";
    public static final String METHOD_DESCRIPTOR_1 = "(Lio/github/jiashunx/masker/rest/framework/MRestRequest;)V";
    public static final String METHOD_DESCRIPTOR_2 = "(Lio/github/jiashunx/masker/rest/framework/MRestRequest;Lio/github/jiashunx/masker/rest/framework/MRestResponse;)V";

    public static Class<? extends MRestServlet> generateClass(Class<?> servletClass, String methodName, MRestHandlerType handlerType) {
        try {
            return generateClass0(servletClass, methodName, handlerType);
        } catch (Throwable throwable) {
            throw new MRestMappingException("generate mapping handler class failed.", throwable);
        }
    }

    public static Class<? extends MRestServlet> generateClass0(Class<?> servletClass, String methodName, MRestHandlerType handlerType) throws MalformedURLException, ClassNotFoundException {
        String originClassName = servletClass.getName().replace(".", "/");
        String originClassIdentifier = "L" + originClassName + ";";

        String targetName = "MRestServlet$ASM$" + counter.incrementAndGet();
        String targetFileName = targetName + ".java";
        String targetClassName = "io/github/jiashunx/masker/rest/framework/asm/" + targetName;
        String targetClassIdentifier = "L" + targetClassName + ";";

        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit(52, ACC_PUBLIC + ACC_SUPER, targetClassName, null, "java/lang/Object", new String[]{"io/github/jiashunx/masker/rest/framework/servlet/MRestServlet"});

        cw.visitSource(targetFileName, targetName);

        {
            fv = cw.visitField(ACC_PRIVATE, "servlet", originClassIdentifier, null, null);
            fv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(" + originClassIdentifier + ")V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(PUTFIELD, targetClassName, "servlet", originClassIdentifier);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitInsn(RETURN);
            Label l3 = new Label();
            mv.visitLabel(l3);
            mv.visitLocalVariable("this", targetClassIdentifier, null, l0, l3, 0);
            mv.visitLocalVariable("servlet", originClassIdentifier, null, l0, l3, 1);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "service", METHOD_DESCRIPTOR_2, null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, targetClassName, "servlet", originClassIdentifier);
            String methodDescriptor = null;
            switch (handlerType) {
                case InputReq_NoRet:
                    mv.visitVarInsn(ALOAD, 1);
                    methodDescriptor = METHOD_DESCRIPTOR_1;
                    break;
                case InputReqResp_NoRet:
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitVarInsn(ALOAD, 2);
                    methodDescriptor = METHOD_DESCRIPTOR_2;
                    break;
                default:
                    methodDescriptor = METHOD_DESCRIPTOR_0;
                    break;
            }
            mv.visitMethodInsn(INVOKEVIRTUAL, originClassName, methodName, methodDescriptor, false);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitInsn(RETURN);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLocalVariable("this", targetClassIdentifier, null, l0, l2, 0);
            mv.visitLocalVariable("request", CLASS_IDENTIFIER_REQ, null, l0, l2, 1);
            mv.visitLocalVariable("response", CLASS_IDENTIFIER_RESP, null, l0, l2, 2);
            mv.visitMaxs(3, 3);
            mv.visitEnd();
        }
        cw.visitEnd();
        byte[] bytes = cw.toByteArray();
        File classFile = FileUtils.newFile(MRestUtils.getFrameworkTempDirPath() + targetClassName + ".class");
        IOUtils.write(bytes, classFile);
        return (Class<? extends MRestServlet>) ServletHandlerClassLoader
                .getInstance().loadClass(targetClassName.replace("/", "."));
    }

    private static String getClassName(Class<?> klass) {
        return klass.getName().replace(".", "/");
    }

}
