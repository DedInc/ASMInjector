package com.github.dedinc.asminjector.utils;

import com.github.dedinc.asminjector.utils.IOUtils;
import com.github.dedinc.asminjector.utils.RunnableGenerator;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import javax.swing.*;

public class JarModifier {
    private File selectedJar;
    private JarFile jarFile;
    private List<String> methods;

    public void loadJar(Component parent) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            selectedJar = fileChooser.getSelectedFile();
            try {
                jarFile = new JarFile(selectedJar);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadMethods(String selectedClass) {
        methods = new ArrayList<>();
        if (selectedClass != null && jarFile != null) {
            try {
                JarEntry entry = jarFile.getJarEntry(selectedClass.replace('.', '/') + ".class");
                if (entry != null) {
                    ClassReader cr = new ClassReader(jarFile.getInputStream(entry));
                    cr.accept(new ClassVisitor(Opcodes.ASM9) {
                        @Override
                        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                            methods.add(name + descriptor);
                            return super.visitMethod(access, name, descriptor, signature, exceptions);
                        }
                    }, 0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void embedCode(String selectedClass, String selectedMethodWithDesc, String injectionClassName, String injectionMethodName, boolean runInThread) {
        if (selectedJar == null || jarFile == null) {
            JOptionPane.showMessageDialog(null, "Please load a JAR file first.");
            return;
        }

        if (selectedClass == null || selectedMethodWithDesc == null || injectionClassName.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please select a class, a method, and enter an injection class name.");
            return;
        }

        String selectedMethod = selectedMethodWithDesc.substring(0, selectedMethodWithDesc.indexOf('('));
        String methodDescriptor = selectedMethodWithDesc.substring(selectedMethodWithDesc.indexOf('('));

        try {
            File outputFile = new File(selectedJar.getParent(), selectedJar.getName().replace(".jar", "_modified.jar"));
            try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(outputFile));
                 InputStream injectionStream = getClass().getResourceAsStream("/Injection.class")) {

                ClassReader injectionReader = new ClassReader(injectionStream);
                ClassNode injectionNode = new ClassNode();
                injectionReader.accept(injectionNode, ClassReader.EXPAND_FRAMES);

                ClassNode remappedInjectionNode = new ClassNode();
                Map<String, String> remapping = new HashMap<>();
                String remappedInjectionClassName = injectionClassName.replace(".", "/");
                String remappedInjectionMethodName = injectionMethodName;
                remapping.put("Injection/injectMethod()V", remappedInjectionClassName + "/" + remappedInjectionMethodName + "()V");
                SimpleRemapper remapper = new SimpleRemapper(remapping);
                ClassRemapper classRemapper = new ClassRemapper(remappedInjectionNode, remapper);
                injectionNode.accept(classRemapper);

                remappedInjectionNode.name = remappedInjectionClassName;

                MethodNode injectionMethod = null;
                for (Object method : remappedInjectionNode.methods) {
                    MethodNode methodNode = (MethodNode) method;
                    if (methodNode.name.equals("injectMethod") && methodNode.desc.equals("()V")) {
                        injectionMethod = methodNode;
                        break;
                    }
                }
                if (injectionMethod != null) {
                    injectionMethod.name = remappedInjectionMethodName;
                }

                remapping.remove("Injection");
                remapping.remove("Injection/injectMethod()V");

                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    jos.putNextEntry(new ZipEntry(entry.getName()));
                    byte[] classBytes = IOUtils.toByteArray(jarFile.getInputStream(entry));

                    if (entry.getName().equals(selectedClass.replace('.', '/') + ".class")) {
                        ClassReader classReader = new ClassReader(classBytes);
                        ClassWriter cw = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
                        classReader.accept(new ClassVisitor(Opcodes.ASM9, cw) {
                            @Override
                            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                                if (name.equals(selectedMethod) && descriptor.equals(methodDescriptor)) {
                                    return new AdviceAdapter(Opcodes.ASM9, mv, access, name, descriptor) {
                                        @Override
                                        protected void onMethodEnter() {
                                            if (runInThread) {
                                                mv.visitTypeInsn(Opcodes.NEW, remappedInjectionClassName + "Runnable");
                                                mv.visitInsn(Opcodes.DUP);
                                                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, remappedInjectionClassName + "Runnable", "<init>", "()V", false);
                                                mv.visitVarInsn(Opcodes.ASTORE, 1);

                                                mv.visitTypeInsn(Opcodes.NEW, "java/lang/Thread");
                                                mv.visitInsn(Opcodes.DUP);
                                                mv.visitVarInsn(Opcodes.ALOAD, 1);
                                                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Thread", "<init>", "(Ljava/lang/Runnable;)V", false);
                                                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Thread", "start", "()V", false);
                                            } else {
                                                mv.visitMethodInsn(Opcodes.INVOKESTATIC, remappedInjectionClassName, remappedInjectionMethodName, "()V", false);
                                            }
                                        }
                                    };
                                }
                                return mv;
                            }
                        }, ClassReader.EXPAND_FRAMES);
                        classBytes = cw.toByteArray();
                    }

                    jos.write(classBytes);
                    jos.closeEntry();
                }

                ClassWriter remappedInjectionWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                remappedInjectionNode.accept(remappedInjectionWriter);
                byte[] remappedInjectionClassBytes = remappedInjectionWriter.toByteArray();

                jos.putNextEntry(new ZipEntry(remappedInjectionClassName + ".class"));
                jos.write(remappedInjectionClassBytes);
                jos.closeEntry();

                if (runInThread) {
                    RunnableGenerator runnableGenerator = new RunnableGenerator(remappedInjectionClassName, remappedInjectionMethodName);
                    byte[] runnableClassBytes = runnableGenerator.generate();
                    jos.putNextEntry(new ZipEntry(remappedInjectionClassName + "Runnable.class"));
                    jos.write(runnableClassBytes);
                    jos.closeEntry();
                }

                JOptionPane.showMessageDialog(null, "Code embedded successfully! Output: " + outputFile.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage());
        }
    }

    public JarFile getJarFile() {
        return jarFile;
    }

    public List<String> getMethods() {
        return methods;
    }
}