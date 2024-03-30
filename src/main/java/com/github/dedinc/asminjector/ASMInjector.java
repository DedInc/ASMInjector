package com.github.dedinc.asminjector;

import com.formdev.flatlaf.util.SystemInfo;
import com.github.dedinc.asminjector.ui.ASMInjectorGUI;
import com.github.dedinc.asminjector.utils.JarModifier;
import javax.swing.*;

public class ASMInjector {
    public static void main(String[] args) {
        if (SystemInfo.isMacOS) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", "ASMInjector");
            System.setProperty("apple.awt.application.appearance", "system");
        }

        if (SystemInfo.isLinux) {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
        }

        SwingUtilities.invokeLater(() -> {
            JarModifier jarModifier = new JarModifier();
            ASMInjectorGUI gui = new ASMInjectorGUI(jarModifier);
            gui.setVisible(true);
        });
    }
}