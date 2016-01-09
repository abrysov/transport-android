/**
 * Created by abrysov
 */
package com.sqiwy.transport.controller.event;

import com.sqiwy.transport.controller.Event;
import com.sqiwy.transport.controller.Event.Source;

public class EventEnterBusStopGeofence extends Event {

	public EventEnterBusStopGeofence() {
		type = Event.Type.FORCING;
		source = Source.GEO;
	}
}
