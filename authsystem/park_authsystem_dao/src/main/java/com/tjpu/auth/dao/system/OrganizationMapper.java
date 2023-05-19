package com.tjpu.auth.dao.system;

import java.util.List;

import com.tjpu.auth.model.system.OrganizationVO;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationMapper {
    /**
     * @author: zhangzc
     * @date: 2018/7/4 15:48
     * @Description: 获取部门树所有的部门信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
	List<OrganizationVO> getOrganizationTreeData();
}