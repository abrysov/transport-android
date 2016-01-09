/**
 * Created by abrysov
 */
package com.sqiwy.transport.util;

import com.squareup.otto.Bus;

public final class EventBus {
    private static Bus sBus = new Bus();

    private EventBus() {
    }

    public static void registerReceiver(Object receiver) {
        try {
            sBus.register(receiver);
        } catch (Exception e) {
            // Ignore the exception to prevent a crash, if this receiver was already registered.
        }
    }

    public static void unregisterReceiver(Object receiver) {
        try {
            sBus.unregister(receiver);
        } catch (Exception e) {
            // Ignore the exception to prevent a crash, if this receiver was already unregistered.
        }
    }

    public static void postEvent(Object event) {
        sBus.post(event);
    }
}
