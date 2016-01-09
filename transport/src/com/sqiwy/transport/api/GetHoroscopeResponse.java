/**
 * Created by abrysov
 */
package com.sqiwy.transport.api;

import com.sqiwy.transport.data.Horoscope;

import java.util.List;

public class GetHoroscopeResponse extends BaseResponse {
    public List<Horoscope> horoscope;
}
