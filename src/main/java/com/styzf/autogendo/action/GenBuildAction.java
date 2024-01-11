package com.styzf.autogendo.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.java.stubs.index.JavaShortClassNameIndex;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.styzf.autogendo.util.ShowBundle;
import com.styzf.autogendo.util.TypeUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.styzf.autogendo.constant.Constant.GEN_BUILD;

/**
 * @author styzf
 * @date 2023/12/3 14:23
 */
@Deprecated
public class GenBuildAction extends AnAction {
    
    private static final String IMPORT = "import ";
    private static final String DOT = ".";
    private static final String SEMICOLON = ";";
    private static final String PKG_CONVERTER = "converter";
    private static final String PKG_REPOSITORY = "repository";
    private static final String CONVERTER_JAVA_SUFFIX = "Converter.java";
    private static final String CONVERTER_SUFFIX = "Converter";
    private static final String DO = "DO";
    private static final String PO = "PO";
    
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
        WriteCommandAction.runWriteCommandAction(project, (Runnable) () -> genClass(project, psiClass, psiFile));
    }
    
    private static void genClass(Project project, @NotNull PsiClass psiClass, @NotNull PsiFile psiFile) {
        PsiField[] allFields = psiClass.getAllFields();
        if (ArrayUtil.isEmpty(allFields)) {
            return;
        }
        
        var doClassName = psiClass.getName();
        var poClassName = StrUtil.replaceLast(doClassName, DO, PO);
        var filedPo = StrUtil.lowerFirst(poClassName);
        var filedDo = StrUtil.lowerFirst(doClassName);
        
        PsiDirectory repository = psiFile.getParent().findSubdirectory(PKG_REPOSITORY);
        if (ObjectUtil.isNull(repository)) {
            repository = psiFile.getParent().getParent().findSubdirectory(PKG_REPOSITORY);
            if (ObjectUtil.isNull(repository)) {
                Messages.showMessageDialog("没有找到仓储层的包", "你没有包", Messages.getInformationIcon());
                return;
            }
        }
        PsiDirectory convert = repository.findSubdirectory(PKG_CONVERTER);
        if (ObjectUtil.isNull(convert)) {
            convert = repository.createSubdirectory(PKG_CONVERTER);
        }
        var fileName = doClassName + CONVERTER_JAVA_SUFFIX;
        PsiFile convertFile = convert.findFile(fileName);
        if (ObjectUtil.isNull(convertFile)) {
            convertFile = convert.createFile(fileName);
        }
        
        String path = convert.toString().replace("PsiDirectory:", "");
        // 相对路径，packageStr获取到的包路径
        String projectPath = path.substring(project.getBasePath().length());
        String packageStr = projectPath.substring(projectPath.indexOf("\\src\\main\\java\\") + 15).replaceAll("\\\\", ".");
        
        String doClassPackageName = ((PsiJavaFileImpl) psiClass.getContainingFile()).getPackageName();
        
        File file = FileUtil.file(path + FileUtil.FILE_SEPARATOR + fileName);
        List<String> lines = new LinkedList<>();
        lines.add("package " + packageStr + ";");
        lines.add("");
        lines.add(IMPORT + doClassPackageName + DOT + doClassName + SEMICOLON);
        
        Collection<PsiClass> poList = JavaShortClassNameIndex.getInstance()
                .get(poClassName, project, GlobalSearchScope.projectScope(project));
        PsiClass poClass = null;
        if (CollUtil.isNotEmpty(poList)) {
            for (PsiClass poPsiClass : poList) {
                poClass = poPsiClass;
                String poPackageName = ((PsiJavaFileImpl) poPsiClass.getContainingFile()).getPackageName();
                lines.add(IMPORT + poPackageName + DOT + poClassName + SEMICOLON);
            }
        }
        
        String dtoClassName = StrUtil.replaceLast(doClassName, DO, "");
        Collection<PsiClass> dtoList = JavaShortClassNameIndex.getInstance()
                .get(dtoClassName, project, GlobalSearchScope.projectScope(project));
        PsiClass dtoClass = null;
        if (CollUtil.isNotEmpty(dtoList)) {
            for (PsiClass dtoPsiClass : dtoList) {
                dtoClass = dtoPsiClass;
                String dtoPackageName = ((PsiJavaFileImpl) dtoPsiClass.getContainingFile()).getPackageName();
                lines.add(IMPORT + dtoPackageName + DOT + dtoClassName + SEMICOLON);
            }
        }
        
        lines.add("");
        lines.add("public class " + doClassName + CONVERTER_SUFFIX + " {");
        lines.add("    private " + doClassName + CONVERTER_SUFFIX + "(){}");
        lines.add("    public static " + doClassName + " po2Do(" + poClassName + " " + filedPo + ") {");
        addLines(lines, true, filedPo, doClassName, allFields, poClass);
        lines.add("    }");
        lines.add("");
        lines.add("    public static " + poClassName + " do2Po(" + doClassName + " " + filedDo + ") {");
        addLines(lines, false, filedDo, poClassName, allFields, poClass);
        lines.add("    }");
        lines.add("");
        
//        if (ObjectUtil.isNotNull(dtoClass)) {
            lines.add("    public static " + dtoClassName + " do2oDto(" + doClassName + " " + filedDo + ") {");
//        addLines(lines, false, filedDo, poClassName, allFields, poClass);
            add2DtoLines(lines, dtoClassName, doClassName, filedDo, allFields, dtoClass);
            lines.add("    }");
//        }
        lines.add("}");
        
        FileUtil.writeLines(lines, file, Charset.defaultCharset());
    }
    
    private static void add2DtoLines(List<String> lines, String dtoClassName, String doClassName, String filedDo, PsiField[] allFields, PsiClass dtoClass) {
        String dtoClassFiledName = StrUtil.lowerFirst(dtoClassName);
        lines.add("        " + dtoClassName + " " + dtoClassFiledName + " = new " + dtoClassName + "();");
//        PsiField[] dtoAllFields = dtoClass.getAllFields();
        // todo
        for (PsiField dtoField : allFields) {
            var fieldName = dtoField.getName();
            if (igField.contains(fieldName)
                    || fieldName.startsWith("KEY_")) {
                continue;
            }
            fieldName = StrUtil.upperFirst(fieldName);
            lines.add("        " + dtoClassFiledName + DOT + "set" + fieldName
                    + "(" + filedDo + ".get" + fieldName + "());");
        }
        
        lines.add("        return " + dtoClassFiledName + ";");
    }
    
    private static void addLines(List<String> lines, boolean isPo2Do, String srcName, String tarName, PsiField[] allFields, PsiClass poClass) {
        var builderName = StrUtil.lowerFirst(tarName)  + "Builder";
        List<String> idNameList = new ArrayList<>();
        if (isPo2Do) {
            idNameList = Arrays.stream(allFields)
                    .filter(field -> ArrayUtil.isNotEmpty(field.getAnnotations()))
                    .map(PsiField::getName)
                    .toList();
            var line = "        " + tarName + DOT + "Builder " + builderName + " = new " + tarName
                    + DOT + "Builder(";
            for (String idName : idNameList) {
                line += srcName + ".get" + StrUtil.upperFirst(idName) + "(), ";
            }
            if (CollUtil.isNotEmpty(idNameList)) {
                line = StrUtil.replaceLast(line, ", ", "");
            }
            line += ");";
            lines.add(line);
        } else {
            PsiField[] allPoFields = PsiField.EMPTY_ARRAY;
            if (ObjectUtil.isNotNull(poClass)) {
                allPoFields = poClass.getAllFields();
            }
            idNameList = Arrays.stream(allPoFields)
                    .filter(field -> ArrayUtil.isNotEmpty(field.getAnnotations()))
                    .map(PsiField::getName)
                    .toList();
            var line = "        " + tarName + DOT + "Builder " + builderName + " = new " + tarName + DOT + "Builder(";
            for (String idName : idNameList) {
                line += srcName + ".get" + StrUtil.upperFirst(idName) + "(), ";
            }
            if (CollUtil.isNotEmpty(idNameList)) {
                line = StrUtil.replaceLast(line, ", ", "");
            }
            line += ");";
            lines.add(line);
        }
        
        boolean isOne = true;
        for (int i = 0; i < allFields.length; i++) {
            PsiField field = allFields[i];
            String fieldName = field.getName();
            if (igField.contains(fieldName)
                    || fieldName.startsWith("KEY_")
                    || idNameList.contains(fieldName)) {
                continue;
            }
            
            PsiType[] superTypes = field.getType().getSuperTypes();
            var isDo = TypeUtil.isDO(field.getType())
                    || (ArrayUtil.isNotEmpty(superTypes) && TypeUtil.isDO(superTypes[0]))
                    || field.getType().getCanonicalText().startsWith("DO");
            if (isDo) {
                continue;
            }
            if (isOne) {
                isOne = false;
                lines.add("        " + builderName + DOT + fieldName + "(" + srcName + ".get" + StrUtil.upperFirst(fieldName) + "())");
                continue;
            }
            lines.add("                " + DOT + fieldName + "(" + srcName + ".get" + StrUtil.upperFirst(fieldName) + "())");
        }
        var last = lines.get(lines.size() - 1);
        last += ";";
        lines.remove(lines.size() - 1);
        lines.add(last);
        lines.add("");
        lines.add("        return " + builderName + DOT + "build();");
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
