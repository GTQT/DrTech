package com.drppp.drtech.client.drone;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;
import net.minecraft.client.resources.I18n;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Inspector 2.0 choice control: paged dropdown for enums and a six-face selector for directions. */
public final class DronePropertyChoiceWidget extends Widget<DronePropertyChoiceWidget> implements Interactable {

    private static final int ROW_HEIGHT = 14;
    private static final int VISIBLE_ROWS = 5;

    private final Supplier<List<String>> choices;
    private final Supplier<String> current;
    private final Supplier<Boolean> direction;
    private final Consumer<String> selection;
    private boolean expanded;
    private int firstVisible;

    public DronePropertyChoiceWidget(Supplier<List<String>> choices, Supplier<String> current,
            Supplier<Boolean> direction, Consumer<String> selection) {
        this.choices = choices;
        this.current = current;
        this.direction = direction;
        this.selection = selection;
        background();
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        List<String> values = safeChoices();
        if (direction.get()) drawDirectionSelector(values, context.getMouseX(), context.getMouseY());
        else drawDropdown(values, context.getMouseX(), context.getMouseY());
    }

    private void drawDropdown(List<String> values, int mouseX, int mouseY) {
        GuiDraw.drawRect(0, 0, getArea().w(), 16, 0xFF222B36);
        GuiDraw.drawBorderInsideXYWH(0, 0, getArea().w(), 16, 0xFF718398);
        GuiDraw.drawText(display(current.get()), 5, 4, 0.7F, 0xFFE7EDF5, false);
        GuiDraw.drawText(expanded ? "▲" : "▼", getArea().w() - 13, 4, 0.65F, 0xFF9ED2FF, false);
        if (!expanded) return;
        int count = Math.min(VISIBLE_ROWS, Math.max(0, values.size() - firstVisible));
        GuiDraw.drawRect(0, 17, getArea().w(), count * ROW_HEIGHT + 2, 0xF01B222C);
        GuiDraw.drawBorderInsideXYWH(0, 17, getArea().w(), count * ROW_HEIGHT + 2, 0xFF718398);
        for (int row = 0; row < count; row++) {
            int y = 18 + row * ROW_HEIGHT;
            String value = values.get(firstVisible + row);
            boolean hovered = mouseX >= 0 && mouseX < getArea().w() && mouseY >= y && mouseY < y + ROW_HEIGHT;
            boolean selected = value.equals(current.get());
            if (hovered || selected) GuiDraw.drawRect(1, y, getArea().w() - 2, ROW_HEIGHT,
                    selected ? 0xFF345C78 : 0xFF334252);
            GuiDraw.drawText((selected ? "● " : "  ") + display(value), 5, y + 3, 0.65F,
                    selected ? 0xFFBCE3FF : 0xFFE0E6ED, false);
        }
        if (values.size() > VISIBLE_ROWS) {
            GuiDraw.drawText((firstVisible + 1) + "-" + (firstVisible + count) + "/" + values.size(),
                    getArea().w() - 40, 4, 0.5F, 0xFF9DAAB8, false);
        }
    }

    private void drawDirectionSelector(List<String> values, int mouseX, int mouseY) {
        expanded = false;
        drawDirectionButton("AUTO", 0, 0, getArea().w(), 16, values, mouseX, mouseY);
        String[] faces = {"UP", "DOWN", "NORTH", "SOUTH", "WEST", "EAST"};
        int gap = 2;
        int buttonWidth = (getArea().w() - gap * 2) / 3;
        for (int index = 0; index < faces.length; index++) {
            int column = index % 3;
            int row = index / 3;
            drawDirectionButton(faces[index], column * (buttonWidth + gap), 19 + row * 20,
                    buttonWidth, 18, values, mouseX, mouseY);
        }
    }

    private void drawDirectionButton(String value, int x, int y, int width, int height, List<String> values,
            int mouseX, int mouseY) {
        boolean allowed = values.contains(value);
        boolean selected = value.equals(current.get());
        boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        int background = !allowed ? 0xFF282D34 : selected ? 0xFF356B8E : hovered ? 0xFF3A4B5E : 0xFF252F3A;
        GuiDraw.drawRect(x, y, width, height, background);
        GuiDraw.drawBorderInsideXYWH(x, y, width, height, selected ? 0xFFA9E1FF : 0xFF65778B);
        String text = directionSymbol(value) + " " + display(value);
        GuiDraw.drawText(text, x + 4, y + 5, 0.58F, allowed ? 0xFFE7EDF5 : 0xFF68717B, false);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (mouseButton != 0) return Result.IGNORE;
        int mouseX = getContext().getMouseX();
        int mouseY = getContext().getMouseY();
        List<String> values = safeChoices();
        if (direction.get()) {
            String choice = directionAt(mouseX, mouseY);
            if (choice != null && values.contains(choice)) selection.accept(choice);
            return choice == null ? Result.IGNORE : Result.SUCCESS;
        }
        if (mouseY >= 0 && mouseY < 16) {
            expanded = !expanded;
            ensureCurrentVisible(values);
            return Result.SUCCESS;
        }
        if (expanded && mouseY >= 18) {
            int index = firstVisible + (mouseY - 18) / ROW_HEIGHT;
            if (index >= 0 && index < values.size() && (mouseY - 18) / ROW_HEIGHT < VISIBLE_ROWS) {
                selection.accept(values.get(index));
                expanded = false;
                return Result.SUCCESS;
            }
        }
        expanded = false;
        return Result.IGNORE;
    }

    @Override
    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
        if (direction.get() || !expanded) return false;
        List<String> values = safeChoices();
        int maximum = Math.max(0, values.size() - VISIBLE_ROWS);
        firstVisible = Math.max(0, Math.min(maximum, firstVisible + (scrollDirection.modifier > 0 ? -1 : 1)));
        return true;
    }

    private void ensureCurrentVisible(List<String> values) {
        int index = values.indexOf(current.get());
        if (index < 0) return;
        if (index < firstVisible) firstVisible = index;
        if (index >= firstVisible + VISIBLE_ROWS) firstVisible = index - VISIBLE_ROWS + 1;
    }

    private List<String> safeChoices() {
        List<String> values = choices.get();
        return values == null ? Collections.emptyList() : values;
    }

    private static String directionAt(int x, int y) {
        if (y >= 0 && y < 16) return "AUTO";
        if (y < 19 || y >= 57 || x < 0) return null;
        int row = (y - 19) / 20;
        int column = x * 3 / 134;
        if (column < 0 || column > 2 || row < 0 || row > 1) return null;
        return new String[] {"UP", "DOWN", "NORTH", "SOUTH", "WEST", "EAST"}[row * 3 + column];
    }

    private static String directionSymbol(String value) {
        if ("UP".equals(value)) return "↑";
        if ("DOWN".equals(value)) return "↓";
        if ("NORTH".equals(value)) return "N";
        if ("SOUTH".equals(value)) return "S";
        if ("WEST".equals(value)) return "W";
        if ("EAST".equals(value)) return "E";
        return "◎";
    }

    private static String display(String value) {
        if (value == null || value.isEmpty()) return I18n.format("drtech.drone.property.choice.unset");
        String key = "drtech.drone.value." + value.toLowerCase(java.util.Locale.ROOT);
        return I18n.hasKey(key) ? I18n.format(key) : value;
    }
}
