package com.github.dedinc.asminjector.utils;

import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

public class RunnableGenerator {
    private final String remappedInjectionClassName;
    private final String remappedInjectionMethodName;

    public RunnableGenerator(String remappedInjectionClassName, String remappedInjectionMethodName) {
        this.remappedInjectionClassName = remappedInjectionClassName;
        this.remappedInjectionMethodName = remappedInjectionMethodName;
    }

    public byte[] generate() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES); // Use COMPUTE_FRAMES for better stack map generation
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, remappedInjectionClassName + "Runnable", null, "java/lang/Object", new String[] { "java/lang/Runnable" });

        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null); // Use the correct method descriptor
            mv.visitCode();
            mv.visitMethodInsn(INVOKESTATIC, remappedInjectionClassName, remappedInjectionMethodName, "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 1);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }
}