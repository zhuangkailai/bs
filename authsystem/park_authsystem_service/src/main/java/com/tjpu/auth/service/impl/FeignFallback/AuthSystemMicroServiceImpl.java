package com.tjpu.auth.service.impl.FeignFallback;


import com.tjpu.auth.service.micro.AuthSystemMicroService;
import com.tjpu.pk.common.utils.AuthUtil;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class AuthSystemMicroServiceImpl implements AuthSystemMicroService {

    @Override
    public Object getAuthDataByParam(JSONObject authParam) {
        return AuthUtil.returnObject("fail",  "");
    }

    @Override
    public Object sendAllClientMessage(JSONObject jsonObject) {
        return AuthUtil.returnObject("fail",  "");
    }

    @Override
    public Object getListByParam(String authParam) {
        return AuthUtil.returnObject("fail",  "");
    }

    @Override
    public Object getListData(String microParam) {
        return AuthUtil.returnObject("fail",  "");
    }

    @Override
    public Object getDetail(String microParam) {
        return AuthUtil.returnObject("fail",  "");
    }

    @Override
    public Object doAddMethod(String microParam) {
        return AuthUtil.returnObject("fail",  "");
    }

    @Override
    public Object doEditMethod(String microParam) {
        return AuthUtil.returnObject("fail",  "");
    }

    @Override
    public Object deleteMethod(String microParam) {
        return AuthUtil.returnObject("fail",  "");
    }

    @Override
    public Object isTableDataHaveInfo(String microParam) {
        return null;
    }

    @Override
    public Object getQueryCriteriaData(String microParam) {
        return AuthUtil.returnObject("fail",  "");
    }

    @Override
    public Object getUserButtonAuthInMenu(String microParam) {
        return AuthUtil.returnObject("fail",  "");
    }

    @Override
    public Object getTableTitle(String microParam) {
        return AuthUtil.returnObject("fail",  "");
    }

    @Override
    public byte[] getHSSFWorkbook(String microParam) {
        return null;
    }

    @Override
    public Object getAddPageInfo(String microParam) {
        return AuthUtil.returnObject("fail",  "");
    }

    @Override
    public Object goUpdatePage(String microParam) {
        return AuthUtil.returnObject("fail",  "");
    }

}
