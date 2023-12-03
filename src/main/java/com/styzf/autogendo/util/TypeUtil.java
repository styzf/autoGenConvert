package com.styzf.autogendo.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.intellij.psi.PsiType;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author styzf
 * @date 2023/11/30 23:29
 */
public class TypeUtil {
    public static boolean isNoDO(PsiType type) {
        if (ObjectUtil.isNull(type)) {
            return false;
        }
        String typeStr = type.toString();
        typeStr = typeStr.substring(typeStr.indexOf(":"));
        
        return !typeStr.endsWith("DO");
    }

    public static boolean isDO(PsiType type) {
        return ! isNoDO(type);
    }
}
