/**
 * Created by abrysov
 */
package com.sqiwy.transport.controller.event;

import com.sqiwy.transport.controller.Event;
import com.sqiwy.transport.controller.Event.Source;

public class EventExitBusStopGeofence extends Event {

	public EventExitBusStopGeofence() {
		type = Event.Type.NON_FORCING;
		source = Source.GEO;
	}
}
