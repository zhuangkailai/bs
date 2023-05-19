package com.tjpu.sp.dao.extand;

import com.tjpu.sp.model.extand.TextMessageVO;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface TextMessageMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(TextMessageVO record);

    int insertSelective(TextMessageVO record);

    TextMessageVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(TextMessageVO record);

    int updateByPrimaryKey(TextMessageVO record);

    List<Map<String, Object>> getTextMessageListData(JSONObject paramMap);

    void batchInsert(List<TextMessageVO> listobj);
}