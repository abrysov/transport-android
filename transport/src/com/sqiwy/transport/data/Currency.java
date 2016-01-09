package com.sqiwy.transport.data;

public class Currency {

    private String code; // like "USD"
    private String name;
    private String sell;
    private String buy;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSell() {
        return sell;
    }

    public void setSell(String sell) {
        this.sell = sell;
    }

    public String getBuy() {
        return buy;
    }

    public void setBuy(String buy) {
        this.buy = buy;
    }

    @Override
    public String toString() {
        return "Currency{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", sell='" + sell + '\'' +
                ", buy='" + buy + '\'' +
                '}';
    }
}
