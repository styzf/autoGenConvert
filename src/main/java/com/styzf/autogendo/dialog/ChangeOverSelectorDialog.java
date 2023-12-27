package com.styzf.autogendo.dialog;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.components.JBViewport;
import com.styzf.autogendo.gen.GenSrc2Target;

import javax.swing.*;
import java.awt.event.*;

public class ChangeOverSelectorDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonGen;
    private JButton buttonCancel;
    private JTextField textFieldSrc;
    private JTextField textFieldTarget;
    private JLabel labelPo;
    private JLabel labelDto;
    private AnActionEvent e;
    
    public ChangeOverSelectorDialog(AnActionEvent e) {
        super(new DummyFrame("选择生成的类"));
        this.e = e;
        JBViewport jbViewport = new JBViewport();
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonGen);
        setLocationRelativeTo(null);
        // 允许操作上级窗口
        setModal(false);
        
        buttonGen.addActionListener(new ActionListener() {
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
    }
    
    private void onOK() {
        String srcName = textFieldSrc.getText();
        String targetName = textFieldTarget.getText();
        new GenSrc2Target().gen(srcName, targetName, e);
        getOwner().dispose();
        dispose();
    }
    
    private void onCancel() {
        getOwner().dispose();
        dispose();
    }
}
