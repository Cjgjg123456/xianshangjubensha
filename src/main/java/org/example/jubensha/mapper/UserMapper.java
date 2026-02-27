package org.example.jubensha.mapper;

import org.example.jubensha.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    // 根据用户名查询用户
    User selectByUsername(String username);

    // 根据手机号查询用户
    User selectByPhone(String phone);

    // 插入用户
    int insertUser(User user);

    // 更新用户资料
    int updateUserProfile(User user);
}