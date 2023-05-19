package com.pub.dao;

import com.pub.model.SysMenuVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SysMenuMapper {

    SysMenuVO getMenuVOByMenuCode(@Param("menucode") String menuCode);


    List<SysMenuVO> getMenuNameByMenuid(Map<String, Object> params1);

}