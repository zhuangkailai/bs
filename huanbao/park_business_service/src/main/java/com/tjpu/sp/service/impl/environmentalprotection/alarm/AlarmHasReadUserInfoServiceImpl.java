package com.tjpu.sp.service.impl.environmentalprotection.alarm;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.environmentalprotection.alarm.AlarmHasReadUserInfoMapper;
import com.tjpu.sp.service.environmentalprotection.alarm.AlarmHasReadUserInfoService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class AlarmHasReadUserInfoServiceImpl implements AlarmHasReadUserInfoService {

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;
    @Autowired
    private AlarmHasReadUserInfoMapper alarmHasReadUserInfoMapper;

    /**
     * @author: lip
     * @date: 2019/7/16 0016 上午 11:17
     * @Description: 自定义条件添加已读信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void addHasReadAlarmInfoByParams(Map<String, Object> paramMap) {
        String collection = "";
        String usercode = paramMap.get("usercode").toString();
        //判断模块：1表示浓度突变，2表示排放量突变，3表示预警，4表示异常，5表示超限
        Integer remindtype = Integer.parseInt(paramMap.get("remindtype").toString());
        Query query;
        switch (CommonTypeEnum.RemindTypeEnum.getObjectByCode(remindtype)) {
            case ConcentrationChangeEnum:
                collection = "HourData";
                query = getChangQueryByParam(paramMap);
                break;
            case FlowChangeEnum:
                collection = "HourFlowData";
                query = getChangQueryByParam(paramMap);
                break;
            case OverAlarmEnum:
                collection = "OverData";
                query = getQueryByParam(paramMap, collection);
                break;
            case EarlyAlarmEnum:
                collection = "EarlyWarnData";
                query = getQueryByParam(paramMap, collection);
                break;
            case ExceptionAlarmEnum:
                collection = "ExceptionData";
                query = getQueryByParam(paramMap, collection);
                break;
            case WaterNoFlowEnum:
                collection = "ExceptionData";
                query = getQueryByParam(paramMap, collection);
                break;
            default:
                query = null;
                break;
        }
        if (query != null) {
            //List<Document> documents = mongoTemplate.find(query,Document.class,collection);
            Update update = new Update().addToSet("ReadUserIds", usercode);
            mongoTemplate.updateMulti(query, update, collection);
        }
    }

    /**
     * @author: lip
     * @date: 2019/8/19 0019 上午 8:38
     * @Description: 获取浓度突变、排放量突变查询条件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Query getChangQueryByParam(Map<String, Object> paramMap) {
        Query query = new Query();
        List<String> mns = (List<String>) paramMap.get("dgimns");
        query.addCriteria(Criteria.where("DataGatherCode").in(mns));
        String monitortime = paramMap.get("monitortime").toString();
        Date startDate;
        Date endDate;
        if ("hourData".equals(paramMap.get("timetype"))) {
            monitortime = DataFormatUtil.FormatDateOneToOther(monitortime, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH");
            startDate = DataFormatUtil.getDateYMDHMS(monitortime + ":00:00");
            endDate = DataFormatUtil.getDateYMDHMS(monitortime + ":59:59");
        } else {
            startDate = DataFormatUtil.getDateYMDHMS(monitortime + " 00:00:00");
            endDate = DataFormatUtil.getDateYMDHMS(monitortime + " 23:59:59");
        }


        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        return query;
    }

    /**
     * @author: lip
     * @date: 2019/8/17 0017 下午 3:39
     * @Description: 获取预警、数据超限、数据异常查询条件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Query getQueryByParam(Map<String, Object> paramMap, String collection) {
        Query query = new Query();
        List<String> mns = (List<String>) paramMap.get("dgimns");
        query.addCriteria(Criteria.where("DataGatherCode").in(mns));
        Date startDate = null;
        Date endDate = null;
        String monitortime = paramMap.get("monitortime").toString();
        if (paramMap.get("datatype") != null) {
            //query.addCriteria(Criteria.where("DataType").is(paramMap.get("datatype")));
            String datatype = paramMap.get("datatype").toString();
            if (datatype.equals("RealTimeData")) {
                startDate = DataFormatUtil.getDateYMDHMS(monitortime);
                endDate = DataFormatUtil.getDateYMDHMS(monitortime);
            } else if (datatype.equals("MinuteData")) {
                startDate = DataFormatUtil.getDateYMDHMS(monitortime + ":00");
                endDate = DataFormatUtil.getDateYMDHMS(monitortime + ":59");
            } else if (datatype.equals("HourData")) {
                startDate = DataFormatUtil.getDateYMDHMS(monitortime + ":00:00");
                endDate = DataFormatUtil.getDateYMDHMS(monitortime + ":59:59");
            } else if (datatype.equals("DayData")) {
                startDate = DataFormatUtil.getDateYMDHMS(monitortime + " 00:00:00");
                endDate = DataFormatUtil.getDateYMDHMS(monitortime + " 23:59:59");
            }
        } else {
            startDate = DataFormatUtil.getDateYMDHMS(monitortime);
            endDate = DataFormatUtil.getDateYMDHMS(monitortime);
        }
        if (collection.equals("OverData")) {
            query.addCriteria(Criteria.where("OverTime").gte(startDate).lte(endDate));
        } else if (collection.equals("EarlyWarnData")) {
            query.addCriteria(Criteria.where("EarlyWarnTime").gte(startDate).lte(endDate));
        } else if (collection.equals("ExceptionData")) {
            query.addCriteria(Criteria.where("ExceptionTime").gte(startDate).lte(endDate));
        } else {
            query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        }
        if (paramMap.get("pollutantcode") != null) {
            query.addCriteria(Criteria.where("PollutantCode").is(paramMap.get("pollutantcode")));
        }
        return query;
    }
}
