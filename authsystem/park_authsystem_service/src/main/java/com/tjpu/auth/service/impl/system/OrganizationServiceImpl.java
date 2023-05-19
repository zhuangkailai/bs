package com.tjpu.auth.service.impl.system;

import com.tjpu.auth.dao.system.OrganizationMapper;
import com.tjpu.auth.model.system.OrganizationVO;
import com.tjpu.auth.service.system.OrganizationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


/**
 * @author: xsm
 * @date: 2018/7/6 14:43
 * @Description:部门 service 实现类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version V1.0
 */
@Service
@Transactional
public class OrganizationServiceImpl implements OrganizationService {
    @Autowired
    private  OrganizationMapper organizationMapper;


    /**
     * @author: xsm
     * @date: 2018/7/6 14:41
     * @Description: 获取部门树
     * @updateUser:zzc
     * @updateDate: 2018/7/19 13:35
     * @updateDescription：自身id等于父ID时或者没有父ID时是顶级节点
     * @return:
     */
    @Override
    public List<Map<String, Object>> getOrganizationTreeData() {
        //获取所有的部门信息
        final List<OrganizationVO> organizationTreeData = organizationMapper.getOrganizationTreeData();
        if (organizationTreeData.size() > 0) {
            List<Map<String, Object>> organizationTree = new ArrayList<>();
            Iterator<OrganizationVO> iterator = organizationTreeData.iterator();
            while (iterator.hasNext()) {
                OrganizationVO treeDatum = iterator.next();
                //判断是否为一级节点 当父ID等于本身ID或者父ID为空时为一级节点
                if (StringUtils.isBlank(treeDatum.getParentId())) {
                    String organizationId = treeDatum.getOrganizationId();
                    Map<String, Object> topOrganization = new LinkedHashMap<>();
                    topOrganization.put("organizationId", organizationId);  //部门ID
                    topOrganization.put("organizationName", treeDatum.getOrganizationName());   //部门名称
                    topOrganization.put("AllowEdit", true); //允许修改
                    topOrganization.put("AllowDelete", true);   //允许删除
                    topOrganization.put("parentId", treeDatum.getParentId());   //父ID
                    topOrganization.put("flag", "topOrganization"); //flag 告诉前台为顶级部门
                    iterator.remove();
                    List<Map<String, Object>> maps = formatParentData(organizationTreeData, organizationId);    //递归获取下面子节点
                    if (maps.size() > 0) {
                    	topOrganization.put("children", maps);
                    }
                    organizationTree.add(topOrganization);
                }
            }
            return organizationTree;
        } else {
            return null;
        }
    }

    /**
     * @author: xsm
     * @date: 2018/7/6 14:39
     * @Description: 判断树节点是否有子节点，并返回结果。
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param organizationVO
     * @param parentId
     * @return
     */
    private List<Map<String, Object>> formatParentData(List<OrganizationVO> organizationVO, String parentId) {
        List<Map<String, Object>> organizationTree = new ArrayList<>();
        for (OrganizationVO vo : organizationVO) {
            if (vo.getParentId().equals(parentId)) {
                String organizationId = vo.getOrganizationId();
                Map<String, Object> parentMap = new LinkedHashMap<>();
                parentMap.put("organizationId", organizationId);
                parentMap.put("organizationName", vo.getOrganizationName());
                parentMap.put("AllowEdit", true);
                parentMap.put("AllowDelete", true);
                parentMap.put("parentId", vo.getParentId());
                if (formatParentData(organizationVO, vo.getOrganizationId()).size() > 0) {
                    parentMap.put("children", formatParentData(organizationVO, organizationId));
                }
                organizationTree.add(parentMap);
            }

        }
        return organizationTree;
    }

}
