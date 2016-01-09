/**
 * Created by abrysov
 */
package com.sqiwy.transport.api;

public class PingResponse extends BaseResponse {
	
    public static final String STATE_NO_ROUTE = "no_route";
    public static final String STATE_ON_ROUTE = "on_route";
    
	private String state;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
