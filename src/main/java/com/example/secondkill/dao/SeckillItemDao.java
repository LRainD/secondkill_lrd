package com.example.secondkill.dao;


import com.example.secondkill.entity.SeckillItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SeckillItemDao {

    List<SeckillItem> getAll();

    SeckillItem get(int id);

    void updateStock(int seckillId);
}
