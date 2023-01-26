package com.wbx.proj.service;

import com.wbx.proj.dao.WbxDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WbxServiceImpl implements WbxService{

    @Autowired
    private WbxDao wbxDao;

    @Override
    public String findHello() {
        return wbxDao.findHello();
    }
}
