package com.styzf.autogendo.dialog;

import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class PoChooseDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTabbedPane tabbedPane;
    private JComboBox<String> comboBox;
    private JTextPane textPane;
    
    private AnActionEvent e;
    private List<String> poClassNameList;
    private List<String> doClassNameList;
    
    public PoChooseDialog(AnActionEvent e, List<String> poClassNameList, List<String> doClassNameList) {
        this.e = e;
        this.poClassNameList = poClassNameList;
        this.doClassNameList = doClassNameList;
        
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        
        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
        
        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        
        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        
        for (int i = 0; i < this.doClassNameList.size(); i++) {
            var doClassName = doClassNameList.get(i);
            if (i == 0) {
                tabbedPane.setName(doClassName);
            } else {
                tabbedPane.addTab(doClassName, new JTabbedPane());
            }
        }
        
        for (String poClassName : poClassNameList) {
            comboBox.addItem(poClassName);
        }
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long when = e.getWhen();
                Object source = e.getSource();
            }
        });
        comboBox.updateUI();
    }
    
    private void onOK() {
        // add your code here
        dispose();
    }
    
    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
