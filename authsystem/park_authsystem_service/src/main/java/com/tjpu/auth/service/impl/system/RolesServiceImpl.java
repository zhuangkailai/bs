package com.tjpu.auth.service.impl.system;

import com.tjpu.auth.dao.system.RolesMapper;
import com.tjpu.auth.model.system.RolesVO;
import com.tjpu.auth.service.system.RolesService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: zzc
 * @date: 2018/6/2 19:03
 * @Description:角色 service 实现类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@Service
@Transactional
public class RolesServiceImpl implements RolesService {
    private final RolesMapper rolesMapper;

    @Autowired
    public RolesServiceImpl(RolesMapper rolesMapper) {
        this.rolesMapper = rolesMapper;
    }

    /**
     * @author: zzc
     * @date: 2018/6/21 13:50
     * @Description: 获取角色树
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getRoleTreeData() {
        //获取所有的角色信息
        final List<RolesVO> roleTreeData = rolesMapper.getRoleTreeData();
        if (roleTreeData.size() > 0) {
            List<Map<String, Object>> roleTree = new ArrayList<>();
            Iterator<RolesVO> iterator = roleTreeData.iterator();
            while (iterator.hasNext()) {
                RolesVO treeDatum = iterator.next();
                //判断是否为一级节点 当父ID等于本身ID或者父ID为空时为一级节点
                if (StringUtils.isBlank(treeDatum.getParentId()) ||treeDatum.getParentId().equals(treeDatum.getRolesId())) {
                    String rolesId = treeDatum.getRolesId();
                    Map<String, Object> topRole = new LinkedHashMap<>();
                    topRole.put("rolesId", rolesId);
                    topRole.put("rolesName", treeDatum.getRolesName());
                    topRole.put("AllowEdit", treeDatum.getAllowEdit() == null || treeDatum.getAllowEdit() == 1);
                    topRole.put("AllowDelete", treeDatum.getAllowDelete() == null || treeDatum.getAllowDelete() == 1);
                    topRole.put("parentId", treeDatum.getParentId());
                    topRole.put("flag", "topRole");
                    iterator.remove();
                    List<Map<String, Object>> maps = formatParentData(roleTreeData, rolesId);
                    if (maps.size() > 0) {
                        topRole.put("children", maps);
                    }
                    roleTree.add(topRole);
                }
            }
            return roleTree;
        } else {
            return null;
        }
    }

     /**
       * @Author: zhangzc
       * @Date: 2018/9/27 12:45
       * @Description: 根据角色id集合获取角色拥有的功能权限
       * @UpdateUser:
       * @UpdateDate:
       * @UpdateDescription:
       * @Param:
       * @Return:
       */
    @Override
    public List<String> getRoleAuthByRoleIDs(List<String> roleids) {
        List<String> resultList = new ArrayList<>();
        List<Map<String, Object>> authData = rolesMapper.getRoleAuthByRoleIDs(roleids);
        if (authData.size() > 0) {
            getMenuButtonFormat(authData, resultList);
            resultList = resultList.stream().distinct().collect(Collectors.toList());
        }
        return resultList;
    }

    /**
     * @Author: zhangzc
     * @Date: 2018/9/27 12:45
     * @Description: 根据角色id获取角色拥有的功能权限
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public List<String> getRoleAuthByRoleID(String id) {
        List<String> resultList = new ArrayList<>();
        List<Map<String, Object>> authData = rolesMapper.getRoleAuthByRoleID(id);
        if (authData.size() > 0) {
            getMenuButtonFormat(authData, resultList);
            resultList = resultList.stream().distinct().collect(Collectors.toList());
        }
        return resultList;
    }

    /**
     * @author: zhangzc
     * @date: 2018/6/6 18:33
     * @Description: 按钮菜单数据格式化
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void getMenuButtonFormat(List<Map<String, Object>> roleAuthByParam, List<String> resultList) {
        for (Map<String, Object> objectMap : roleAuthByParam) {
            if (objectMap.get("Menu_Id") != null && objectMap.get("Button_Id") != null) {
                resultList.add(objectMap.get("Menu_Id").toString() + "_" + objectMap.get("Button_Id").toString());
            } else if (objectMap.get("Menu_Id") != null && objectMap.get("Button_Id") == null) {
                resultList.add(objectMap.get("Menu_Id").toString());
            }
        }
    }



    /**
     * @author: zzc
     * @date: 2018/6/21 16:31
     * @Description: 角色树结构组合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> formatParentData(List<RolesVO> rolesVO, String parentId) {
        List<Map<String, Object>> roleTree = new ArrayList<>();
        for (RolesVO vo : rolesVO) {
            if (vo.getParentId().equals(parentId)) {
                String rolesId = vo.getRolesId();
                Map<String, Object> parentMap = new LinkedHashMap<>();
                parentMap.put("rolesId", rolesId);
                parentMap.put("rolesName", vo.getRolesName());
                parentMap.put("AllowDelete", vo.getAllowDelete() == null || vo.getAllowDelete() == 1);
                parentMap.put("AllowEdit", vo.getAllowEdit()  == null || vo.getAllowEdit() == 1);
                parentMap.put("parentId", vo.getParentId());
                if (formatParentData(rolesVO, vo.getRolesId()).size() > 0) {
                    parentMap.put("children", formatParentData(rolesVO, rolesId));
                }
                roleTree.add(parentMap);
            }

        }
        return roleTree;
    }

}
