package com.wbx.proj.dao;

import org.springframework.stereotype.Repository;

@Repository
public class WbxDaoImpl implements WbxDao{
    @Override
    public String findHello() {
        return "Hello ssm!";
    }
}
