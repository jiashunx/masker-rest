package io.github.jiashunx.masker.rest.framework.type;

public enum MRestHandlerType {
    /**
     * 无返回值, 无输入参数.
     */
    NoRet_Void,
    /**
     * 无返回值, 输入参数req.
     */
    NoRet_Req,
    /**
     * 无返回值, 输入参数req resp.
     */
    NoRet_ReqResp,
    /**
     * 有返回值, 输入参数req resp.
     */
    Ret_ReqResp,
    /**
     * 有返回值, 无输入参数.
     */
    Ret_Void;
}
