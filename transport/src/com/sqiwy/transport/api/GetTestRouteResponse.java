/**
 * Created by abrysov
 */
package com.sqiwy.transport.api;

import com.google.gson.annotations.SerializedName;
import com.sqiwy.transport.data.Route;

import java.util.List;

public class GetTestRouteResponse extends BaseResponse {

    // "current_direction": "direct",
    @SerializedName("current_direction")
    public String currentDirection;

    public String guid;
    public String number;

    @SerializedName("test_route")
    public List<Route> routes;
}
