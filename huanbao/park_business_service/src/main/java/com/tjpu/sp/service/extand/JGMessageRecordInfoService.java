package com.tjpu.sp.service.extand;


import com.tjpu.sp.model.extand.JGMessageRecordInfoVO;

public interface JGMessageRecordInfoService {
    void sendMessageAndAddData(JGMessageRecordInfoVO jgMessageRecordInfoVO,String pollutionid);
}
