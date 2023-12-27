package com.styzf.autogendo.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.psi.PsiFile;
import com.styzf.autogendo.dialog.ChangeOverSelectorDialog;
import com.styzf.autogendo.setting.ShowBundle;
import org.jetbrains.annotations.NotNull;

import static com.styzf.autogendo.constant.Constant.CHANGE_OVER_SELECTOR;
import static com.styzf.autogendo.constant.Constant.GEN_BUILD;

/**
 * @author styzf
 * @date 2023/12/27 21:05
 */
public class ChangeOverSelectorAction extends AnAction {
    
    public ChangeOverSelectorAction() {
        super();
        setEnabledInModalContext(true);
        setInjectedContext(true);
    }
    
    @Override
    public void actionPerformed(AnActionEvent e) {
        ChangeOverSelectorDialog changeOverSelectorDialog = new ChangeOverSelectorDialog(e);
        changeOverSelectorDialog.pack();
        changeOverSelectorDialog.setVisible(true);
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(ShowBundle.message(CHANGE_OVER_SELECTOR));
    }
    
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
