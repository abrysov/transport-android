/**
 * Created by abrysov
 */
package com.sqiwy.transport.controller;

public class Event {

	public enum Type {
		FORCING,
		NON_FORCING
	}
	
	public enum Source {
		GEO,
		SCHEDULE
	}
	
	protected Source source = Source.SCHEDULE;
	protected Type type = Type.NON_FORCING;
	protected long timestamp;
	protected Object data;
	
	@Override
	public String toString() {
		return "Event [type=" + type + ", timestamp=" + timestamp + ", data="
				+ data + "]";
	}
	
	
}
