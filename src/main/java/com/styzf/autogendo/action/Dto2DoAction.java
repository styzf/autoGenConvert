package com.styzf.autogendo.action;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import com.styzf.autogendo.setting.ShowBundle;
import com.styzf.autogendo.util.TypeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.styzf.autogendo.constant.Constant.DTO2DO;

/**
 * @author styzf
 * @date 2023/12/3 14:23
 */
public class Dto2DoAction extends AnAction {
    
    public Dto2DoAction() {
        super();
        setEnabledInModalContext(true);
        setInjectedContext(true);
    }
    
    @Override
    public void actionPerformed(AnActionEvent e) {
        var project = e.getProject();
        
        var psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (isNotDo(psiFile)) {
            return;
        }
        
        PsiClass psiClass = PsiTreeUtil.getChildOfType(psiFile, PsiClass.class);
        assert psiClass != null;
        genMethod(project, psiClass);
    }
    
    private static void genMethod(Project project, @NotNull PsiClass psiClass) {
        PsiField[] allFields = psiClass.getAllFields();
        if (ArrayUtil.isEmpty(allFields)) {
            return;
        }
        
        var className = psiClass.getName();
        var classDto = StrUtil.replaceLast(className, "DO", "DTO");
        var filedDto = StrUtil.lowerFirst(classDto);
        var methodStr = "public " + className + "(" + classDto + " " + filedDto + ") {";
        for (PsiField field : allFields) {
            PsiType[] superTypes = field.getType().getSuperTypes();
            var isDo = TypeUtil.isDO(field.getType())
                    || (ArrayUtil.isNotEmpty(superTypes) && TypeUtil.isDO(superTypes[0]))
                    || field.getType().getCanonicalText().startsWith("DO");
            if (! isDo) {
                methodStr += "this." + field.getName() + " = "
                        + filedDto + ".get" + StrUtil.upperFirst(field.getName()) + "();";
                continue;
            }
            
            // 如果是DO的处理逻辑
            PsiClass fieldPsiClass = JavaPsiFacade.getInstance(project)
                    .findClass(field.getType().getCanonicalText(), GlobalSearchScope.projectScope(project));
            if (Objects.isNull(fieldPsiClass)) {
                fieldPsiClass = JavaPsiFacade.getInstance(project)
                        .findClass(superTypes[0].getCanonicalText(), GlobalSearchScope.projectScope(project));
                if (Objects.isNull(fieldPsiClass)) continue;
            }
            
            Query<PsiClass> subClass = ClassInheritorsSearch.search(fieldPsiClass);
            for (PsiClass subPsiClass : subClass) {
                assert subPsiClass != null;
                methodStr += "this." + field.getName() + " = "
                        + "new " + subPsiClass.getName() +
                        "(" + filedDto + ".get" + StrUtil.upperFirst(field.getName()) + "());";
                genMethod(project, subPsiClass);
            }
        }
        methodStr += "}";
        
        PsiElementFactory elementFactory = PsiElementFactory.getInstance(project);
        PsiMethod method = elementFactory.createMethodFromText(methodStr, psiClass);
        
        WriteCommandAction.runWriteCommandAction(project, (Runnable) () -> psiClass.add(method));
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (isDo(psiFile)) {
            e.getPresentation().setText(ShowBundle.message(DTO2DO));
        } else {
            e.getPresentation().setEnabledAndVisible(false);
        }
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
    
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
