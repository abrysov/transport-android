/**
 * Created by abrysov
 */
package com.sqiwy.transport.api;

public class ErrorResponse {
    public int errorcode;
    public String errormsg;
    public String errordata;

    @Override
    public String toString() {
        return "ErrorResponse [errorcode = " + errorcode + ", errormsg = " + errormsg
                + ", errordata = " + errordata + "]";
    }
}
