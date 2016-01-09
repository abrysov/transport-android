/**
 * Created by abrysov
 */
package com.sqiwy.transport.api;

import com.sqiwy.transport.data.Currency;

import java.util.List;

public class GetCurrencyResponse extends BaseResponse{
    public List<Currency> currency;

    public List<Currency> getCurrency() {
        return currency;
    }

    public void setCurrency(List<Currency> currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "GetCurrencyResponse{" +
                "currency=" + currency +
                '}';
    }
}
