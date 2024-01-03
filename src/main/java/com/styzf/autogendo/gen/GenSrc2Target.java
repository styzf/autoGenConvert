package com.styzf.autogendo.gen;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.impl.java.stubs.index.JavaFullClassNameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.styzf.autogendo.constant.Constant.BR;
import static com.styzf.autogendo.constant.Constant.BRACKET;
import static com.styzf.autogendo.constant.Constant.BUILD;
import static com.styzf.autogendo.constant.Constant.BUILDER;
import static com.styzf.autogendo.constant.Constant.COMMA;
import static com.styzf.autogendo.constant.Constant.DO;
import static com.styzf.autogendo.constant.Constant.DOT;
import static com.styzf.autogendo.constant.Constant.DTO;
import static com.styzf.autogendo.constant.Constant.EQ;
import static com.styzf.autogendo.constant.Constant.GET;
import static com.styzf.autogendo.constant.Constant.LEFT_BRACE;
import static com.styzf.autogendo.constant.Constant.LEFT_BRACKET;
import static com.styzf.autogendo.constant.Constant.NEW;
import static com.styzf.autogendo.constant.Constant.PO;
import static com.styzf.autogendo.constant.Constant.PRIVATE;
import static com.styzf.autogendo.constant.Constant.RETURN;
import static com.styzf.autogendo.constant.Constant.RIGHT_BRACE;
import static com.styzf.autogendo.constant.Constant.RIGHT_BRACKET;
import static com.styzf.autogendo.constant.Constant.SEMICOLON;
import static com.styzf.autogendo.constant.Constant.SET;
import static com.styzf.autogendo.constant.Constant.SPACE;

/**
 * 生成器
 *
 * @author styzf
 * @date 2023/12/27 22:25
 */
public class GenSrc2Target {
    
    private static final Set<String> IG_FIELD = new HashSet<>();
    
    static {
        IG_FIELD.add("status");
        IG_FIELD.add("changedFields");
        IG_FIELD.add("deletedChildObjects");
    }
    
    /**
     * 生成
     *
     * @param srcName    源类名
     * @param targetName 目标类名
     * @param e          事件
     */
    public void gen(String srcName, String targetName, @NotNull AnActionEvent e) {
        if (StrUtil.isBlank(srcName) || StrUtil.isBlank(targetName)) {
            return;
        }
        Project project = e.getProject();
        assert project != null;
        
        PsiClass srcClass = searchPsiClass(srcName, project);
        if (srcClass == null) return;
        
        PsiClass targetClass = searchPsiClass(targetName, project);
        if (targetClass == null) return;
        
        var psiFile = e.getData(CommonDataKeys.PSI_FILE);
        PsiClass currentClass = PsiTreeUtil.getChildOfType(psiFile, PsiClass.class);
        assert currentClass != null;
        
        var methodStr = genMethodStr(srcClass, targetClass);
        
        PsiElementFactory elementFactory = PsiElementFactory.getInstance(project);
        PsiMethod method = elementFactory.createMethodFromText(methodStr, currentClass);
        WriteCommandAction.runWriteCommandAction(project, (Runnable) () -> currentClass.add(method));
    }
    
    /**
     * 生成方法文本
     *
     * @param srcClass    源类
     * @param targetClass 目标类
     * @return 方法文本
     */
    @NotNull
    private String genMethodStr(PsiClass srcClass, PsiClass targetClass) {
        var srcClassName = srcClass.getName();
        var targetClassName = targetClass.getName();
        var srcClassFiledName = StrUtil.lowerFirst(srcClassName);
        var methodStr = new StringBuilder();
        
        methodStr.append(PRIVATE).append(SPACE).append(targetClassName).append(SPACE);
        
        assert srcClassName != null;
        assert targetClassName != null;
        
        if (isPO(srcClassName)) {
            methodStr.append("po2");
        } else if (isDO(srcClassName)) {
            methodStr.append("do2");
        } else if (isDTO(srcClassName)) {
            methodStr.append("dto2");
        } else {
            methodStr.append("to");
        }
        
        if (isPO(targetClassName)) {
            methodStr.append("Po");
        } else if (isDO(targetClassName)) {
            methodStr.append("Do");
        } else if (isDTO(targetClassName)) {
            methodStr.append("Dto");
        } else {
            methodStr.append(targetClassName);
        }
        
        methodStr.append(LEFT_BRACKET)
                .append(srcClassName).append(SPACE).append(srcClassFiledName)
                .append(RIGHT_BRACKET)
                .append(SPACE).append(LEFT_BRACE);
        if (isBuilder(targetClassName)) {
            genBuilderMethodBody(targetClass, srcClass, methodStr);
        } else {
            genNormalMethodBody(targetClass, srcClass, methodStr);
        }
        methodStr.append(RIGHT_BRACE);
        
        return methodStr.toString();
    }
    
    private void genBuilderMethodBody(PsiClass targetClass, PsiClass srcClass, StringBuilder methodStr) {
        var srcClassName = srcClass.getName();
        var targetClassName = targetClass.getName();
        var srcClassFiledName = StrUtil.lowerFirst(srcClassName);
        var targetClassFiledName = StrUtil.lowerFirst(targetClassName);
        
        PsiField[] allFields = targetClass.getAllFields();
        List<String> idNameList = Arrays.stream(allFields)
                .filter(field -> ArrayUtil.isNotEmpty(field.getAnnotations()))
                .map(PsiField::getName)
                .toList();
        
        methodStr.append(targetClassName).append(DOT).append(BUILDER).append(SPACE)
                .append(targetClassFiledName).append(BUILDER).append(SPACE)
                .append(EQ).append(SPACE)
                .append(NEW).append(SPACE).append(targetClassName).append(DOT).append(BUILDER)
                .append(LEFT_BRACKET);
        
        for (String idName : idNameList) {
            idName = StrUtil.upperFirst(idName);
            methodStr.append(srcClassFiledName).append(DOT).append(GET).append(idName).append(BRACKET)
                    .append(COMMA).append(SPACE);
        }
        if (CollUtil.isNotEmpty(idNameList)) {
            methodStr.deleteCharAt(methodStr.length() - 2);
        }
        methodStr.append(RIGHT_BRACKET).append(SEMICOLON);
        
        int count = 0;
        for (PsiField field : allFields) {
            var fieldName = field.getName();
            if (IG_FIELD.contains(fieldName)
                    || fieldName.startsWith("KEY_")
                    || idNameList.contains(fieldName)) {
                continue;
            }
            
            if (count == 0) {
                methodStr.append(targetClassFiledName).append(BUILDER);
            }
            methodStr.append(DOT).append(fieldName)
                    .append(LEFT_BRACKET)
                    .append(srcClassFiledName)
                    .append(DOT).append(GET).append(StrUtil.upperFirst(fieldName)).append(BRACKET)
                    .append(RIGHT_BRACKET).append(BR);
            
            count++;
        }
        methodStr.delete(methodStr.length() - BR.length(), methodStr.length());
        methodStr.append(count != 0 ? SEMICOLON : StrUtil.EMPTY).append(BR).append(BR)
                .append(RETURN).append(SPACE).append(targetClassFiledName).append(BUILDER)
                .append(DOT).append(BUILD).append(BRACKET).append(SEMICOLON);
    }
    
    /**
     * @param targetClass 目标类
     * @param srcClass    源类
     */
    private void genNormalMethodBody(PsiClass targetClass,
                                     PsiClass srcClass,
                                     StringBuilder methodStr) {
        var srcClassName = srcClass.getName();
        var targetClassName = targetClass.getName();
        var srcClassFiledName = StrUtil.lowerFirst(srcClassName);
        var targetClassFiledName = StrUtil.lowerFirst(targetClassName);
        
        PsiMethod[] constructors = targetClass.getConstructors();
        PsiMethod constructor = constructors[0];
        PsiParameterList parameterList = constructor.getParameterList();
        Set<String> parameterSet = new HashSet<>();
        if (parameterList.isEmpty()) {
            methodStr.append(targetClassName).append(SPACE).append(targetClassFiledName)
                    .append(SPACE).append(EQ).append(SPACE)
                    .append(NEW).append(SPACE).append(targetClassName).append(BRACKET)
                    .append(SEMICOLON);
        } else {
            methodStr.append(targetClassName).append(SPACE).append(targetClassFiledName)
                    .append(SPACE).append(EQ).append(SPACE)
                    .append(NEW).append(SPACE).append(targetClassName).append(LEFT_BRACKET);
            PsiParameter[] parameters = parameterList.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                PsiParameter parameter = parameters[i];
                String parameterName = parameter.getName();
                parameterSet.add(parameterName);
                methodStr.append(srcClassFiledName).append(DOT).append(GET).append(StrUtil.upperFirst(parameterName)).append(BRACKET);
                if (i == parameters.length - 1) {
                    methodStr.append(RIGHT_BRACKET).append(SEMICOLON);
                } else {
                    methodStr.append(COMMA).append(SPACE);
                }
            }
        }
        
        PsiField[] allFields = targetClass.getAllFields();
        for (PsiField field : allFields) {
            var fieldName = field.getName();
            if (IG_FIELD.contains(fieldName)
                    || fieldName.startsWith("KEY_")
                    || parameterSet.contains(fieldName)) {
                continue;
            }
            fieldName = StrUtil.upperFirst(fieldName);
            methodStr.append(targetClassFiledName)
                    .append(DOT).append(SET).append(fieldName)
                    .append(LEFT_BRACKET)
                    .append(srcClassFiledName)
                    .append(DOT).append(GET).append(fieldName).append(BRACKET)
                    .append(RIGHT_BRACKET)
                    .append(SEMICOLON);
        }
        methodStr.append(BR).append(BR)
                .append(RETURN).append(SPACE).append(targetClassFiledName).append(SEMICOLON);
    }
    
    /**
     * 根据类名进行搜索
     *
     * @param className 类名
     * @return 搜索到的类名
     */
    @Nullable
    private static PsiClass searchPsiClass(String className, Project project) {
        Collection<PsiClass> psiClassCollection = JavaFullClassNameIndex.getInstance()
                .get(className, project, GlobalSearchScope.allScope(project));
        if (CollUtil.isEmpty(psiClassCollection)) {
            return null;
        }
        
        return psiClassCollection.stream().toList().get(0);
    }
    
    /**
     * 是否用构造者模式进行创建
     *
     * @param className 类名
     * @return 是否用构造者模式进行创建
     */
    private boolean isBuilder(String className) {
        return isPO(className) || isDO(className);
    }
    
    /**
     * 是否以DO结尾
     *
     * @param className 类名
     * @return 是否以DO结尾
     */
    private boolean isDO(String className) {
        return className.endsWith(DO);
    }
    
    /**
     * 是否以PO结尾
     *
     * @param className 类名
     * @return 是否以PO结尾
     */
    private boolean isPO(String className) {
        return className.endsWith(PO);
    }
    
    /**
     * 是否以DTO结尾
     *
     * @param className 类名
     * @return 是否以DTO结尾
     */
    private boolean isDTO(String className) {
        return className.endsWith(DTO);
    }
}
