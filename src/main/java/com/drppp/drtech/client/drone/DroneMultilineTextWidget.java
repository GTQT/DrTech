package com.drppp.drtech.Client.drone;

import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.widgets.textfield.TextEditorWidget;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Bounded multiline editor which commits to a client-side draft when focus leaves the widget. */
public final class DroneMultilineTextWidget extends TextEditorWidget {

    private final Supplier<String> getter;
    private final Consumer<String> setter;
    private final int maxCharacters;
    private final int maxLines;
    private boolean normalizing;

    public DroneMultilineTextWidget(Supplier<String> getter, Consumer<String> setter,
            int maxCharacters, int maxLines) {
        this.getter = getter;
        this.setter = setter;
        this.maxCharacters = Math.max(1, maxCharacters);
        this.maxLines = Math.max(1, maxLines);
        this.handler.setMaxCharacters(this.maxCharacters);
        this.handler.setMaxLines(this.maxLines);
    }

    @Override
    public void onInit() {
        super.onInit();
        replaceText(getter.get());
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isFocused()) {
            String external = sanitize(getter.get(), maxCharacters, maxLines);
            if (!handler.getTextAsString().equals(external)) replaceText(external);
        }
    }

    @Override
    protected void onTextChanged() {
        if (normalizing) return;
        String current = handler.getTextAsString();
        String bounded = sanitize(current, maxCharacters, maxLines);
        if (!current.equals(bounded)) replaceText(bounded);
        setter.accept(bounded);
    }

    @Override
    public void onRemoveFocus(ModularGuiContext context) {
        super.onRemoveFocus(context);
        setter.accept(sanitize(handler.getTextAsString(), maxCharacters, maxLines));
    }

    private void replaceText(String value) {
        normalizing = true;
        String bounded = sanitize(value, maxCharacters, maxLines);
        List<String> lines = bounded.isEmpty() ? Collections.singletonList("")
                : Arrays.asList(bounded.split("\n", -1));
        handler.getText().clear();
        handler.getText().addAll(lines);
        normalizing = false;
    }

    public static String sanitize(String value, int maxCharacters, int maxLines) {
        String normalized = value == null ? "" : value.replace("\r\n", "\n")
                .replace('\r', '\n').replaceAll("§.", "").replace("§", "");
        String[] lines = normalized.split("\n", -1);
        int lineLimit = Math.max(1, maxLines);
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < Math.min(lines.length, lineLimit); index++) {
            if (index > 0) result.append('\n');
            result.append(lines[index]);
            if (result.length() >= Math.max(1, maxCharacters)) break;
        }
        int characterLimit = Math.max(1, maxCharacters);
        return result.length() <= characterLimit ? result.toString() : result.substring(0, characterLimit);
    }
}
