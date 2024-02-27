package com.styzf.autogendo.setting;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * @author styzf
 * @date 2024/1/12 1:11
 */
@State(
        name = "com.styzf.autogendo.setting.GenSettingsState",
        storages = @Storage("GenSettingsState.xml")
)
public class GenSettingsState implements PersistentStateComponent<GenSettingsState> {
    /**
     * 生成方法，包含要生成的字段正则
     */
    @NotNull
    public transient Pattern includePattern = Pattern.compile("");
    /**
     * 生成方法，不包含要生成的字段正则
     */
    @NotNull
    public transient Pattern excludePattern = Pattern.compile("");
    /**
     * 是否生成前置判空方法
     */
    public transient boolean judgeIsNull = false;
    
    @NotNull
    public static GenSettingsState getInstance() {
        GenSettingsState service = ApplicationManager.getApplication().getService(GenSettingsState.class);
        if (service == null) {
            return new GenSettingsState();
        }
        return service;
    }
    
    @Nullable
    @Override
    public GenSettingsState getState() {
        return this;
    }
    
    @Override
    public void loadState(@NotNull GenSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
    
    public String getIncludePattern() {
        return includePattern.pattern();
    }
    
    public void setIncludePattern(@NotNull String includePattern) {
        this.includePattern = Pattern.compile(includePattern);
    }
    
    public String getExcludePattern() {
        return excludePattern.pattern();
    }
    
    public void setExcludePattern(@NotNull String excludePattern) {
        this.excludePattern = Pattern.compile(excludePattern);
    }
    
    public boolean isJudgeIsNull() {
        return judgeIsNull;
    }
    
    public void setJudgeIsNull(boolean judgeIsNull) {
        this.judgeIsNull = judgeIsNull;
    }
}
