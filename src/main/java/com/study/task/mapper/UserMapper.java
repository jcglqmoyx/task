package com.study.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.task.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
