package com.drppp.drtech.Client.drone;

import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IDraggable;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.ButtonWidget;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.util.ResourceLocation;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;

/** Node-library button that keeps click behavior while supporting drag-to-canvas creation. */
public final class DroneNodeLibraryButtonWidget extends ButtonWidget<DroneNodeLibraryButtonWidget>
        implements IDraggable {

    private final Area movingArea = new Area();
    private boolean moving;
    private boolean drawDragPreview;
    private int realX;
    private int realY;
    private int relativeClickX;
    private int relativeClickY;
    private int dragStartX;
    private int dragStartY;
    private Predicate<IWidget> dropTarget = widget -> false;
    private Consumer<int[]> dropHandler = position -> { };
    private Runnable clickHandler = () -> { };
    private boolean validDrop;
    private Supplier<ResourceLocation> nodeTypeSupplier = () -> null;

    public DroneNodeLibraryButtonWidget nodeType(Supplier<ResourceLocation> nodeTypeSupplier) {
        this.nodeTypeSupplier = nodeTypeSupplier == null ? () -> null : nodeTypeSupplier;
        return this;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.draw(context, widgetTheme);
        ResourceLocation nodeType = nodeTypeSupplier.get();
        if (nodeType != null) DroneNodeIconTextures.draw(nodeType, 2, 2, 10);
    }

    public DroneNodeLibraryButtonWidget dropTarget(Predicate<IWidget> dropTarget) {
        this.dropTarget = dropTarget == null ? widget -> false : dropTarget;
        return this;
    }

    public DroneNodeLibraryButtonWidget onDropped(Consumer<int[]> dropHandler) {
        this.dropHandler = dropHandler == null ? position -> { } : dropHandler;
        return this;
    }

    public DroneNodeLibraryButtonWidget onLibraryClicked(Runnable clickHandler) {
        this.clickHandler = clickHandler == null ? () -> { } : clickHandler;
        return this;
    }

    @Override
    public void drawMovingState(ModularGuiContext context, float partialTicks) {
        if (!drawDragPreview) return;
        WidgetTree.drawTree(this, context, true, true);
    }

    @Override
    public boolean onDragStart(int mouseButton) {
        if (mouseButton != 0) return false;
        realX = getContext().transformX(0, 0) - getParentArea().x;
        realY = getContext().transformY(0, 0) - getParentArea().y;
        movingArea.setBounds(realX, realY, getArea().w(), getArea().h());
        relativeClickX = getContext().getAbsMouseX() - realX;
        relativeClickY = getContext().getAbsMouseY() - realY;
        dragStartX = getContext().getAbsMouseX();
        dragStartY = getContext().getAbsMouseY();
        validDrop = false;
        drawDragPreview = false;
        return true;
    }

    @Override
    public void onDragEnd(boolean successful) {
        if (successful && validDrop) {
            dropHandler.accept(new int[] { getContext().getAbsMouseX(), getContext().getAbsMouseY() });
        } else if (isClickGesture(dragStartX, dragStartY,
                getContext().getAbsMouseX(), getContext().getAbsMouseY())) {
            playClickSound();
            clickHandler.run();
        }
        validDrop = false;
        drawDragPreview = false;
        movingArea.setBounds(getArea().x, getArea().y, getArea().w(), getArea().h());
    }

    @Override
    public void onDrag(int mouseButton, long timeSinceLastClick) {
        drawDragPreview = !isClickGesture(dragStartX, dragStartY,
                getContext().getAbsMouseX(), getContext().getAbsMouseY());
        movingArea.x = getContext().getAbsMouseX() - relativeClickX;
        movingArea.y = getContext().getAbsMouseY() - relativeClickY;
    }

    @Override
    public boolean canDropHere(int x, int y, @Nullable IWidget widget) {
        validDrop = widget != null && dropTarget.test(widget);
        return validDrop;
    }

    @Nullable
    @Override
    public Area getMovingArea() {
        return movingArea;
    }

    @Override
    public boolean isMoving() {
        return moving;
    }

    @Override
    public void setMoving(boolean moving) {
        this.moving = moving;
        setEnabled(!moving);
    }

    @Override
    public void transform(IViewportStack stack) {
        super.transform(stack);
        // ModularUI marks the widget as moving immediately on mouse-down, including a plain
        // click. Applying the drag transform before the pointer crosses our click threshold
        // produces one frame at the GUI origin. Keep the library entry in place until this is
        // an actual drag; drawMovingState follows the same flag.
        if (moving && drawDragPreview) {
            stack.translate(-getArea().rx, -getArea().ry, 0);
            stack.translate(-realX, -realY, 0);
            stack.translate(movingArea.x, movingArea.y, 0);
        }
    }

    static boolean isClickGesture(int startX, int startY, int endX, int endY) {
        return Math.abs(endX - startX) <= 3 && Math.abs(endY - startY) <= 3;
    }
}
