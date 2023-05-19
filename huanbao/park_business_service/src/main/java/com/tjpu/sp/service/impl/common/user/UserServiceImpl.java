package com.tjpu.sp.service.impl.common.user;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.dao.common.UserMapper;
import com.tjpu.sp.service.common.micro.JnaServiceMicroService;
import com.tjpu.sp.service.common.user.UserService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Autowired
    private JnaServiceMicroService jnaServiceMicroService;

    /**
     * @author: zhangzhenchao
     * @date: 2020/1/10 15:27
     * @Description: 获取部门用户树形结构
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getUserDepartmentTree() {
        //获取用户和部门信息
        List<Map<String, Object>> Departments = userMapper.getDepartments();
        List<Map<String, Object>> organizationTree = new ArrayList<>();
        if (Departments.size() > 0) {
            for (Map<String, Object> map : Departments) {
                if ((map.get("parentId") == null || map.get("parentId").equals("")) && map.get("flag").equals("1")) {
                    String organizationId = map.get("id").toString();
                    Map<String, Object> topOrganization = new HashMap<>();
                    topOrganization.put("name", map.get("name"));   //部门名称
                    topOrganization.put("flag", map.get("flag"));   //部门名称
                    List<Map<String, Object>> maps = formatParentData(Departments, organizationId);    //递归获取下面子节点
                    if (maps.size() > 0) {
                        organizationTree.addAll(maps);
                    }
                }
            }
        }
        return organizationTree;
    }


    private List<Map<String, Object>> formatParentData(List<Map<String, Object>> organizationVO, String parentId) {
        List<Map<String, Object>> organizationTree = new ArrayList<>();
        for (Map<String, Object> vo : organizationVO) {
            if (vo.get("parentId") != null && vo.get("parentId").equals(parentId)) {
                String organizationId = vo.get("id").toString();
                Map<String, Object> parentMap = new HashMap<>();
                parentMap.put("name", vo.get("name"));
                parentMap.put("flag", vo.get("flag"));
                parentMap.put("id", vo.get("id"));
                if (vo.get("flag").equals("2")) {
                    parentMap.put("phone", vo.get("phone"));
                }
                final List<Map<String, Object>> menuMapInfo = formatParentData(organizationVO, organizationId);
                if (menuMapInfo.size() > 0) {
                    parentMap.put("children", menuMapInfo);
                }
                if (vo.get("flag").equals("1")) {//当部门下存在用户时  插入
                    if (menuMapInfo.size() > 0) {
                        organizationTree.add(parentMap);
                    }
                }else{
                    organizationTree.add(parentMap);
                }
            }

        }
        return organizationTree;
    }

    /**
     * @author: xsm
     * @date: 2020/04/29 0009 上午11:27
     * @Description: 根据部门ID获取相关用户信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getUserInfosByDepartmentId(Map<String, Object> paramMap) {
        return userMapper.getUserInfosByDepartmentId(paramMap);
    }


    /**
     * @author: chengzq
     * @date: 2020/5/7 0007 下午 3:15
     * @Description: 获取所有用户信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllUserInfo() {
        return userMapper.getAllUserInfo();
    }

    @Override
    public void updatePassword(Map<String, Object> paramMap) {

        int isHave = userMapper.updatePassword(paramMap);
        if (isHave > 0) {
            String password = paramMap.get("userRemark").toString();
            //发送通知
            JSONObject sendObject = new JSONObject();
            String messageContent = "您的账号【public】密码已修改为【" + password + "】，有效时间24小时，请查收。";
            sendObject.put("message", messageContent);
            List<String> usernames = Arrays.asList("李培_雪迪龙","黄勇","杨龙");
            for (String username : usernames) {
                sendObject.put("username", username);
                try {
                    //推送消息到微信好友
                    jnaServiceMicroService.sendUserMessage(sendObject);
                    Thread.sleep(1500);
                    System.out.println("密码更新" + DataFormatUtil.getDateYMDH(new Date()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Map<String, Object> getUserInfoByAccount(String userAccount) {
        return userMapper.getUserInfoByAccount(userAccount);
    }

    /**
     * @author: chengzq
     * @date: 2020/12/14 0014 下午 4:36
     * @Description:  获取企业法人，环保负责人，通讯录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getMailListInfo(Map<String, Object> paramMap) {
        return userMapper.getMailListInfo(paramMap);
    }


    /**
     * @author: chengzq
     * @date: 2020/12/14 0014 下午 5:01
     * @Description: 获取用户通讯录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPinYinUserMailListInfo(Map<String, Object> paramMap) {
        return userMapper.getPinYinUserMailListInfo(paramMap);
    }

    @Override
    public List<Map<String, Object>> getDepartmentUserMailListInfo(Map<String, Object> paramMap) {
        return userMapper.getDepartmentUserMailListInfo(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2021/1/25 16:59
     * @Description: 获取运维人员信息树
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getDevOpsUserDepartmentTree() {
        //获取运维人员及部门信息
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("isdevopspeople",1);
        List<Map<String, Object>> Departments = userMapper.getDevOpsUserDepartments(paramMap);
        List<Map<String, Object>> organizationTree = new ArrayList<>();
        if (Departments.size() > 0) {
            for (Map<String, Object> map : Departments) {
                if ((map.get("parentId") == null || map.get("parentId").equals("")) && map.get("flag").equals("1")) {
                    String organizationId = map.get("id").toString();
                    Map<String, Object> topOrganization = new HashMap<>();
                    topOrganization.put("name", map.get("name"));   //部门名称
                    topOrganization.put("flag", map.get("flag"));   //部门名称
                    List<Map<String, Object>> maps = formatDevOpsParentData(Departments, organizationId);    //递归获取下面子节点
                    if (maps.size() > 0) {
                        organizationTree.addAll(maps);
                    }
                }
            }
        }
        return organizationTree;
    }

    @Override
    public List<Map<String, Object>> getAllDepartmentUserMailListInfo(Map<String, Object> paramMap) {
        return userMapper.getAllDepartmentUserMailListInfo(paramMap);
    }

    @Override
    public List<Map<String, Object>> getTaskAssignUserData(Map<String,Object> param) {
        return userMapper.getTaskAssignUserData(param);
    }

    @Override
    public Map<String, Object> getUserInfoByUserAccount(String assignuseraccount) {
        return userMapper.getUserInfoByUserAccount(assignuseraccount);
    }

    /**
     * @author: xsm
     * @date: 2021/9/10 8:50
     * @Description: 根据菜单Code获取拥有该菜单权限的用户信息
     * @param:
     * @return:
     */
    @Override
    public List<String> getAllUserIdsByMenuCode(String menucode) {
        List<String> userids = new ArrayList<>();
        List<Map<String, Object>> listdata = userMapper.getAllUserInfoByMenuCode(menucode);
        if (listdata!=null&&listdata.size()>0){
            userids = listdata.stream().flatMap(m -> ((List<Map<String, Object>>) m).stream()).filter(m -> m.get("User_ID") != null).map(m -> m.get("User_ID").toString()).collect(Collectors.toList());
        }
        return userids;
    }

    private List<Map<String, Object>> formatDevOpsParentData(List<Map<String, Object>> organizationVO, String parentId) {
        List<Map<String, Object>> organizationTree = new ArrayList<>();
        for (Map<String, Object> vo : organizationVO) {
            if (vo.get("parentId") != null && vo.get("parentId").equals(parentId)) {
                String organizationId = vo.get("id").toString();
                Map<String, Object> parentMap = new HashMap<>();
                parentMap.put("name", vo.get("name"));
                parentMap.put("flag", vo.get("flag"));
                parentMap.put("id", vo.get("id"));
                if (vo.get("flag").equals("2")) {
                    parentMap.put("phone", vo.get("phone"));
                }
                final List<Map<String, Object>> menuMapInfo = formatParentData(organizationVO, organizationId);
                if (menuMapInfo.size() > 0) {
                    parentMap.put("children", menuMapInfo);
                }
                if (vo.get("flag").equals("1")) {
                    if (menuMapInfo.size() > 0) {
                        organizationTree.add(parentMap);
                    }
                }else{
                    organizationTree.add(parentMap);
                }
            }

        }
        return organizationTree;
    }
}
