package com.pub.impl;

import com.pub.service.SysMenuService;
import com.pub.dao.SysMenuMapper;
import com.pub.model.SysMenuVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

 @Service
@Transactional
public class SysMenuServiceImpl implements SysMenuService {
    private final SysMenuMapper sysMenuMapper;

    @Autowired
    public SysMenuServiceImpl(SysMenuMapper sysMenuMapper) {
        this.sysMenuMapper = sysMenuMapper;
    }

    @Override
    public SysMenuVO getMenuVOByMenuCode(String menuCode) {
        return sysMenuMapper.getMenuVOByMenuCode(menuCode);
    }


}
