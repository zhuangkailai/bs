package com.pub.dao;

import com.pub.model.RolesVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolesMapper {

    RolesVO getTopRoleVO();


    void deleteTopRoleAuth(@Param("topRoleId") String topRoleId);


    void resetTopRoleAuth(@Param("topRoleId") String topRoleId);


    void deleteRoleAuthForAddMenu(@Param("menuids") List<String> menuids);


    List<String> getSonRoleIDsForChangeParentRoleAuth(@Param("roleid") String roleid);
}