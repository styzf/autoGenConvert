package com.styzf.autogendo.setting;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;

public class ShowBundle extends DynamicBundle {
    @NonNls
    public static final String BUNDLE = "messages.Po2DoBundle";
    private static final ShowBundle INSTANCE = new ShowBundle();

    private ShowBundle() {
        super(BUNDLE);
    }

    @NotNull
    public static String message(@NotNull String key,
                                 @NotNull Object... params) {
        return INSTANCE.getMessage(key, params);
    }

    @NotNull
    public static Supplier<String> messagePointer(@NotNull String key,
                                                  @NotNull Object... params) {
        return INSTANCE.getLazyMessage(key, params);
    }
}
