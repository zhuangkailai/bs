package com.tjpu.sp.service.environmentalprotection.particularpollutants;
import com.tjpu.sp.model.environmentalprotection.particularpollutants.EntGasPollutantVO;
import net.sf.json.JSONObject;
import java.util.List;
import java.util.Map;

public interface EntGasPollutantService {






    void deleteById(String id);

    Map<String, Object> getEditOrViewDataById(String id);

    Map<String, Object> getDataListByParam(JSONObject jsonObject);

    void updateOrAddData(String pollutionId, List<EntGasPollutantVO> entGasPollutantVOS);
}
