/**
 * Created by abrysov
 */
package com.sqiwy.transport.api;

import com.sqiwy.transport.data.Route;

import java.util.List;

public class GetRouteResponse extends BaseResponse {
    public List<Route> routes;
    public String number;
    public String description;
    public String guid;
    public int version;
    public int map_ratio;
    public int ads_ratio;
    public int content_ratio;
    public int stops_radius;
}
