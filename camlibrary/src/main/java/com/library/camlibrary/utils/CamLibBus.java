package com.library.camlibrary.utils;

import android.support.annotation.RestrictTo;

/**
 * Global class for Event Bus using Rx Java.
 * Single instance of {@link RxEventBus} for the application.
 * <p>
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class CamLibBus {
    private static RxEventBus bus;

    public static RxEventBus getBus() {
        if (bus == null) {
            bus = new RxEventBus();
        }
        return bus;
    }

    public static void complete() {
        getBus().complete();
        bus = null;
    }
}
