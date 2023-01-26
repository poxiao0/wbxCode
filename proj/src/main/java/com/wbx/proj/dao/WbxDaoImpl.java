package com.wbx.proj.dao;

import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Repository
public class WbxDaoImpl implements WbxDao{
    @Override
    public String findHello() {
        return "Hello ssm!";
    }
}
