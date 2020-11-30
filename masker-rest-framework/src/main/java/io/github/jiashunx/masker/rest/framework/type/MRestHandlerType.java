package io.github.jiashunx.masker.rest.framework.type;

public enum MRestHandlerType {
    /**
     * 输入参数req resp, 无返回值.
     */
    InputReqResp_NoRet,
    /**
     * 输入参数req resp, 有返回值.
     */
    InputReqResp_Ret,
    /**
     * 输入参数req, 无返回值.
     */
    InputReq_NoRet,
    /**
     * 输入参数req, 有返回值.
     */
    InputReq_Ret,
    /**
     * 无输入参数, 无返回值.
     */
    NoInput_NoRet,
    /**
     * 无输入参数, 有返回值.
     */
    NoInput_Ret;

}
