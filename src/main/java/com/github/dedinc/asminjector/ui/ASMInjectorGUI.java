package com.github.dedinc.asminjector.ui;

import com.formdev.flatlaf.FlatLaf;
import com.github.dedinc.asminjector.utils.JarModifier;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ASMInjectorGUI extends JFrame {
    private JarModifier jarModifier;
    private JComboBox<String> classComboBox;
    private JComboBox<String> methodComboBox;
    private JButton loadJarButton;
    private JButton embedButton;
    private JTextField injectionClassName;
    private JTextField injectionMethodName;
    private JCheckBox runInThreadCheckBox;

    public ASMInjectorGUI(JarModifier jarModifier) {
        this.jarModifier = jarModifier;
        setTitle("ASMInjector - Jar Modifier");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        initUI();
        setLocationRelativeTo(null);
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;

        loadJarButton = new JButton("Load JAR");
        loadJarButton.addActionListener(this::loadJarActionPerformed);
        add(loadJarButton, gbc);

        gbc.gridy++;
        classComboBox = new JComboBox<>();
        classComboBox.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXX");
        classComboBox.addActionListener(this::loadMethodsActionPerformed);
        add(new JLabel("Select Class:"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 5, 5, 5); // Remove top inset for combobox
        add(classComboBox, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(5, 5, 5, 5); // Reset insets
        methodComboBox = new JComboBox<>();
        methodComboBox.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXX");
        add(new JLabel("Select Method:"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 5, 5, 5); // Remove top inset for combobox
        add(methodComboBox, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(5, 5, 5, 5); // Reset insets
        add(new JLabel("Injection Class Name:"), gbc);

        gbc.gridy++;
        injectionClassName = new JTextField("me.test.Inject");
        add(injectionClassName, gbc);

        gbc.gridy++;
        add(new JLabel("Injection Method Name:"), gbc);

        gbc.gridy++;
        injectionMethodName = new JTextField("injectedMethod");
        add(injectionMethodName, gbc);

        gbc.gridy++;
        runInThreadCheckBox = new JCheckBox("Run in Thread");
        add(runInThreadCheckBox, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 5, 5, 5); // Add top inset for button
        embedButton = new JButton("Embed Code");
        embedButton.addActionListener(this::embedCodeActionPerformed);
        add(embedButton, gbc);

        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setTheme("com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMoonlightIJTheme");
    }

    private void loadJarActionPerformed(ActionEvent event) {
        jarModifier.loadJar(this);
        updateClassComboBox();
    }

    private void updateClassComboBox() {
        classComboBox.removeAllItems();
        JarFile jarFile = jarModifier.getJarFile();
        if (jarFile != null) {
            Enumeration<JarEntry> entries = jarFile.entries();
            String longestPath = "";
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName().replaceAll("/", ".").replace(".class", "");
                    classComboBox.addItem(className);
                    if (className.length() > longestPath.length()) {
                        longestPath = className;
                    }
                }
            }
            classComboBox.setPrototypeDisplayValue(longestPath + "XXXXXXXXXXXXXXXXXXXX");
        }
    }

    private void loadMethodsActionPerformed(ActionEvent event) {
        String selectedClass = (String) classComboBox.getSelectedItem();
        jarModifier.loadMethods(selectedClass);
        updateMethodComboBox();
    }

    private void updateMethodComboBox() {
        methodComboBox.removeAllItems();
        for (String method : jarModifier.getMethods()) {
            methodComboBox.addItem(method);
        }
    }

    private void embedCodeActionPerformed(ActionEvent event) {
        String selectedClass = (String) classComboBox.getSelectedItem();
        String selectedMethod = (String) methodComboBox.getSelectedItem();
        String injectionClass = injectionClassName.getText();
        String injectionMethodName = this.injectionMethodName.getText();
        boolean runInThread = runInThreadCheckBox.isSelected();

        jarModifier.embedCode(selectedClass, selectedMethod, injectionClass, injectionMethodName, runInThread);
    }

    public static void setTheme(String className) {
        try {
            UIManager.setLookAndFeel(className);
            FlatLaf.updateUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}