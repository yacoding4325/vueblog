package com.markerhub.common.lang;

import lombok.Data;

import java.io.Serializable;

@Data
public class  Result implements Serializable {

    private String code;//200正常，非200异常

    private String msg;

    private Object data;

    public static Result success (Object data) {

        Result m = new Result();
        m.setCode("0");
        m.setData(data);
        m.setMsg("操作成功0");
        return m;
    }

    public static Result success (String mess, Object data) {

        Result m = new Result();
        m.setCode("0");
        m.setData(data);
        m.setMsg(mess);
        return m;

    }

    public static Result fail (String mess) {

        Result m = new Result();
        m.setCode("-1");
        m.setData(null);
        m.setMsg(mess);
        return m;

    }

    public static Result fail (int code,String mess,Object data) {

        Result m = new Result();
        m.setCode("-1");
        m.setData(data);
        m.setMsg(mess);
        return m;

    }
}
