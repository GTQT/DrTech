package com.drppp.drtech.Client.drone;

import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;
import com.drppp.drtech.common.drone.program.model.DroneArea;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;

import java.util.function.Supplier;

/** Compact live X/Z and X/Y projections of the exact bounded area used by the runtime. */
public final class DroneAreaPreviewWidget extends Widget<DroneAreaPreviewWidget> {

    private final Supplier<DroneArea> areaSupplier;
    private final Supplier<String> statusSupplier;

    public DroneAreaPreviewWidget(Supplier<DroneArea> areaSupplier, Supplier<String> statusSupplier) {
        this.areaSupplier = areaSupplier;
        this.statusSupplier = statusSupplier;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        int width = getArea().w();
        int height = getArea().h();
        GuiDraw.drawRect(0, 0, width, height, 0xFF111820);
        GuiDraw.drawBorderInsideXYWH(0, 0, width, height, 0xFF526477);
        DroneArea area = areaSupplier.get();
        if (area == null || !area.isWithinRuntimeLimits() || area.getVolume() <= 0) {
            GuiDraw.drawText(statusSupplier.get(), 4, 5, 0.75F, 0xFFCF9B76, false);
            return;
        }
        int gap = 6;
        int paneWidth = (width - gap - 4) / 2;
        int paneHeight = height - 14;
        GuiDraw.drawText(I18n.format("drtech.drone.programmer.area_top"), 3, 3,
                0.65F, 0xFFAAB7C5, false);
        GuiDraw.drawText(I18n.format("drtech.drone.programmer.area_side"), paneWidth + gap + 2, 3,
                0.65F, 0xFFAAB7C5, false);
        drawProjection(area, 2, 11, paneWidth, paneHeight, true);
        drawProjection(area, paneWidth + gap + 2, 11, paneWidth, paneHeight, false);
    }

    private static void drawProjection(DroneArea area, int x, int y, int width, int height, boolean top) {
        int sourceWidth = Math.max(1, area.getSizeX());
        int sourceHeight = Math.max(1, top ? area.getSizeZ() : area.getSizeY());
        int cell = Math.max(1, Math.min(width / sourceWidth, height / sourceHeight));
        int drawWidth = sourceWidth * cell;
        int drawHeight = sourceHeight * cell;
        int left = x + Math.max(0, (width - drawWidth) / 2);
        int topY = y + Math.max(0, (height - drawHeight) / 2);
        GuiDraw.drawBorderInsideXYWH(left, topY, drawWidth, drawHeight, 0xFF354454);
        for (int index = 0; index < area.getVolume(); index++) {
            BlockPos position = area.positionAt(index);
            int px = position.getX() - area.getMin().getX();
            int py = top ? position.getZ() - area.getMin().getZ()
                    : area.getMax().getY() - position.getY();
            GuiDraw.drawRect(left + px * cell, topY + py * cell, cell, cell,
                    top ? 0xFF55BDE5 : 0xFF7DD49C);
        }
    }
}
