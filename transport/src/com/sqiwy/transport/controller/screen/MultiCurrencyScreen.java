/**
 * Created by abrysov
 */
package com.sqiwy.transport.controller.screen;

import com.sqiwy.transport.controller.Event;
import com.sqiwy.transport.controller.Screen;
import com.sqiwy.transport.controller.event.EventEnterAdGeofence;
import com.sqiwy.transport.controller.event.EventEnterBusStopGeofence;

public class MultiCurrencyScreen extends Screen {

    public MultiCurrencyScreen(int id, long minShowTime, float targetShowCoefficient) {
        super(id, minShowTime, targetShowCoefficient);
    }

    @Override
    public boolean isEligibleFor(Event event) {
    	if (event instanceof EventEnterAdGeofence || event instanceof EventEnterBusStopGeofence) {
        	return false;
        } else {
        	return true;
        }
    }

    @Override
    public boolean isWithOutTimeLimit(Event event) {
        return event instanceof EventEnterBusStopGeofence;
    }
}
