package com.styzf.autogendo.util;

import cn.hutool.core.collection.CollUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.impl.java.stubs.index.JavaFullClassNameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author styzf
 * @date 2024/2/25 22:42
 */
public class JavaPsiUtils {
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
}
