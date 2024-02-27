package com.styzf.autogendo.setting;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.styzf.autogendo.util.ShowBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author styzf
 * @date 2024/1/12 1:09
 */
public class GenSettingsComponent {
    private final JPanel myMainPanel;
    public GenSettingsComponent() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addComponent(commonPanel(), 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }
    
    public JPanel getPanel() {
        return myMainPanel;
    }
    protected final JBTextField includePattern = new JBTextField();
    protected final JBTextField excludePattern = new JBTextField();
    private final JBCheckBox judgeIsNullCheckBox = new JBCheckBox(ShowBundle.message("judge.null"));
    
    @NotNull
    protected JPanel commonPanel() {
        return FormBuilder.createFormBuilder()
                .addComponent(patternPanel(), 1)
                .getPanel();
    }
    
    @NotNull
    protected JPanel patternPanel() {
        FormBuilder builder = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel(ShowBundle.message("sign.include.regexp")), includePattern, 1, true)
                .addLabeledComponent(new JBLabel(ShowBundle.message("sign.exclude.regexp")), excludePattern, 1, true)
                .addComponent(judgeIsNullCheckBox);
        return builder.getPanel();
    }
    
    @NotNull
    public String getIncludePattern() {
        return includePattern.getText();
    }
    
    public void setIncludePattern(@NotNull String newText) {
        includePattern.setText(newText);
    }
    
    @NotNull
    public String getExcludePattern() {
        return excludePattern.getText();
    }
    
    public void setExcludePattern(@NotNull String newText) {
        excludePattern.setText(newText);
    }
    
    @NotNull
    public JComponent getPreferredFocusedComponent() {
        return includePattern;
    }
    public boolean getJudgeIsNull() {
        return judgeIsNullCheckBox.isSelected();
    }
    public void setJudgeIsNull(boolean judgeIsNull) {
        judgeIsNullCheckBox.setSelected(judgeIsNull);
    }
}
