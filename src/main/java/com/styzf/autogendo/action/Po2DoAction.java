package com.styzf.autogendo.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.styzf.autogendo.dialog.PoChooseDialog;
import com.styzf.autogendo.setting.ShowBundle;
import com.styzf.autogendo.util.TypeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.styzf.autogendo.constant.Constant.JAVA;
import static com.styzf.autogendo.constant.Constant.PO;

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
        find(e);
    }
    
    private void find(AnActionEvent e) {
        Project project = e.getProject();
        @NotNull String[] allFilenames = FilenameIndex.getAllFilenames(project);
        List<@NotNull String> poClassNameList = Arrays.stream(allFilenames)
                .filter(fileName -> fileName.endsWith(PO + JAVA))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(poClassNameList)) {
            Messages.showMessageDialog("项目中没有找到PO对象", "你没有对象", Messages.getInformationIcon());
            return;
        }
        
        PoChooseDialog dialog = new PoChooseDialog(e, poClassNameList, CollUtil.newArrayList("TestDO"));
        dialog.pack();
        dialog.setVisible(true);
    }
    
    private void writerPo2Do(AnActionEvent e) {
        Project project = e.getProject();
        
        DataContext dataContext = e.getDataContext();
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (isNotDo(psiFile)) {
            return;
        }
        
        PsiClass psiClass = PsiTreeUtil.getChildOfType(psiFile, PsiClass.class);
        if (ObjectUtil.isNull(psiClass)) {
            return;
        }
        PsiField[] allFields = psiClass.getAllFields();
        if (ArrayUtil.isEmpty(allFields)) {
            return;
        }
        
        String className = psiClass.getName();
        String classPo = StrUtil.replaceLast(className, "DO", "PO");
        String filedPo = StrUtil.lowerFirst(classPo);
        String methodStr = "public " + className + "(" + classPo + " " + filedPo + ") {";
        for (PsiField field : allFields) {
            if (TypeUtil.isNoDO(field.getType())) {
                // TODO 需要写回数据，至于写回哪个对象待确定
                methodStr += "this." + field.getName() + " = "
                        + filedPo + ".get" + StrUtil.upperFirst(field.getName()) + "();";
            }
        }
        methodStr += "}";
        PsiElementFactory elementFactory = PsiElementFactory.SERVICE.getInstance(project);
        PsiMethod method = elementFactory.createMethodFromText(methodStr, psiClass);
        
        WriteCommandAction.runWriteCommandAction(project, new Runnable() {
            @Override
            public void run() {
                psiClass.add(method);
            }
        });
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (isDo(psiFile)) {
            e.getPresentation().setText(ShowBundle.message(null));
        }
        e.getPresentation().setEnabledAndVisible(false);
    }
    
    private boolean isNotDo(PsiFile psiFile) {
        return ! isDo(psiFile);
    }
    
    private boolean isDo(PsiFile psiFile) {
        if (psiFile == null) {
            return false;
        }
        return psiFile.getName().endsWith("DO.java");
    }
}
