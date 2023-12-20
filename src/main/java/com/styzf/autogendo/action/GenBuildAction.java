package com.styzf.autogendo.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTreeUtil;
import com.styzf.autogendo.setting.ShowBundle;
import com.styzf.autogendo.util.TypeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.styzf.autogendo.constant.Constant.GEN_BUILD;

/**
 * @author styzf
 * @date 2023/12/3 14:23
 */
public class GenBuildAction extends AnAction {
    
    private static Set<String> igField = new HashSet<>();
    
    static {
        igField.add("status");
        igField.add("changedFields");
        igField.add("deletedChildObjects");
    }
    
    public GenBuildAction() {
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
        genMethod(project, psiClass, psiFile);
    }
    
    private static void genMethod(Project project, @NotNull PsiClass psiClass, @NotNull PsiFile psiFile) {
        PsiField[] allFields = psiClass.getAllFields();
        if (ArrayUtil.isEmpty(allFields)) {
            return;
        }
        
        List<String> idNameList = Arrays.stream(allFields)
                .filter(field -> ArrayUtil.isNotEmpty(field.getAnnotations()))
                .map(PsiField::getName)
                .toList();
        
        var className = psiClass.getName();
        var classPo = StrUtil.replaceLast(className, "DO", "PO");
        var filedPo = StrUtil.lowerFirst(classPo);
        var methodStr = "public " + className + " build" + className + "() {" + classPo + " " + filedPo + ";";
        var builderName = StrUtil.lowerFirst(className)  + "Builder";
        methodStr += className + ".Builder " + builderName + " = new " + className
                + ".Builder(";
        for (String idName : idNameList) {
            methodStr += filedPo + ".get" + StrUtil.upperFirst(idName) + "(), ";
        }
        methodStr = StrUtil.replaceLast(methodStr, ", ", "");
        methodStr += ");";
        
        methodStr += builderName;
        for (PsiField field : allFields) {
            String fieldName = field.getName();
            if (igField.contains(fieldName) || fieldName.startsWith("KEY_")) {
                continue;
            }
            
            PsiType[] superTypes = field.getType().getSuperTypes();
            var isDo = TypeUtil.isDO(field.getType())
                    || (ArrayUtil.isNotEmpty(superTypes) && TypeUtil.isDO(superTypes[0]))
                    || field.getType().getCanonicalText().startsWith("DO");
            if (! isDo) {
                methodStr += "." + field.getName() + "("
                        + filedPo + ".get" + StrUtil.upperFirst(field.getName()) + "())\n";
            }
        }
        methodStr = StrUtil.replaceLast(methodStr, "\n", "");
        methodStr += ";";
        methodStr += "\nreturn " + builderName + ".build();";
        methodStr += "}";
        
        PsiElementFactory elementFactory = PsiElementFactory.getInstance(project);
        PsiMethod method = elementFactory.createMethodFromText(methodStr, psiClass);
        
        WriteCommandAction.runWriteCommandAction(project, (Runnable) () -> psiClass.add(method));
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (isDo(psiFile)) {
            e.getPresentation().setText(ShowBundle.message(GEN_BUILD));
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
