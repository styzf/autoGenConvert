package com.styzf.autogendo;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiFile;
import com.styzf.autogendo.setting.ShowBundle;
import org.jetbrains.annotations.NotNull;

/**
 * @author styzf
 * @date 2023/11/29 21:03
 */
public class Po2DoAction extends AnAction {
    
    public Po2DoAction() {
        super();
        setEnabledInModalContext(true);
        setInjectedContext(true);
    }
    
    @Override
    public void actionPerformed(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) {
            return;
        }
        
        String className = psiFile.getName();
        if (! className.endsWith("DO.java")) {
            return;
        }
        
        String path = psiFile.getVirtualFile().getPath();
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(ShowBundle.message("po.2.do"));
    }
}
