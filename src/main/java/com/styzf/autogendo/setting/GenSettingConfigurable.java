package com.styzf.autogendo.setting;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author styzf
 * @date 2024/1/12 1:09
 */
public class GenSettingConfigurable implements Configurable {
    
    @SuppressWarnings("NotNullFieldNotInitialized")
    @NotNull
    private GenSettingsComponent component;
    
    @NotNull
    @Override
    public String getDisplayName() {
        return "生成转换类配置";
    }
    
    @NotNull
    @Override
    public JComponent getPreferredFocusedComponent() {
        return component.getPreferredFocusedComponent();
    }
    
    @Nullable
    @Override
    public JComponent createComponent() {
        component = new GenSettingsComponent();
        return component.getPanel();
    }
    
    @Override
    public boolean isModified() {
        @NotNull GenSettingsState settings = GenSettingsState.getInstance();
        var modified = false;
        modified |= !component.getIncludePattern().equals(settings.getIncludePattern());
        modified |= !component.getExcludePattern().equals(settings.getExcludePattern());
        modified |= !(component.getJudgeIsNull() == settings.isJudgeIsNull());
        
        return modified;
    }
    
    @Override
    public void apply() {
        @NotNull GenSettingsState settings = GenSettingsState.getInstance();
        settings.setIncludePattern(component.getIncludePattern());
        settings.setExcludePattern(component.getExcludePattern());
        settings.setJudgeIsNull(component.getJudgeIsNull());
    }
    
    @Override
    public void reset() {
        @NotNull GenSettingsState settings = GenSettingsState.getInstance();
        component.setIncludePattern(settings.getIncludePattern());
        component.setExcludePattern(settings.getExcludePattern());
        component.setJudgeIsNull(settings.isJudgeIsNull());
    }
    
    @Override
    public void disposeUIResources() {
        //noinspection ConstantConditions
        component = null;
    }
}
