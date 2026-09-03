package com.drppp.drtech.client.drone;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.widget.Widget;

import java.util.function.IntConsumer;

/** Invisible mouse-wheel target behind the diagnostic row buttons. */
public final class DroneDiagnosticScrollWidget extends Widget<DroneDiagnosticScrollWidget> implements Interactable {

    private final IntConsumer scrollHandler;

    public DroneDiagnosticScrollWidget(IntConsumer scrollHandler) {
        this.scrollHandler = scrollHandler;
    }

    @Override
    public boolean onMouseScroll(UpOrDown direction, int amount) {
        scrollHandler.accept(direction.modifier > 0 ? -1 : 1);
        return true;
    }
}
