package com.example.secondkill.dao;


import com.example.secondkill.entity.SeckillOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SeckillOrderDao {

    void insert(SeckillOrder seckillOrder);

}
