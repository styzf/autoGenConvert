package com.styzf.autogendo.dialog;


import javax.swing.*;

/**
 * 虚假的框，为了让选择框在任务栏可以显示
 * @author styzf
 * @date 2023/12/28 0:38
 */
public class DummyFrame extends JFrame {
    DummyFrame(String title) {
        super(title);
        setUndecorated(true);
        setVisible(true);
        setLocationRelativeTo(null);
    }
}
