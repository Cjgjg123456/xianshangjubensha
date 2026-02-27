package org.example.jubensha.service.impl;

import org.example.jubensha.dto.LoginRequest;
import org.example.jubensha.dto.RegisterRequest;
import org.example.jubensha.entity.User;
import org.example.jubensha.mapper.UserMapper;
import org.example.jubensha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 构造器注入，解决字段注入警告
    @Autowired
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public boolean updateUserProfile(User user) {
        return userMapper.updateUserProfile(user) > 0;
    }

    @Override
    public boolean updateUserProfile(User user, MultipartFile avatar) {
        try {
            // 处理头像上传
            if (avatar != null && !avatar.isEmpty()) {
                // 生成唯一文件名
                String fileName = "avatar_" + System.currentTimeMillis() + "_" + avatar.getOriginalFilename();
                // 保存路径（实际项目中应该配置为外部路径）
                String uploadDir = "D:/develop/newproject/jubensha - 1/src/main/resources/static/images/";
                java.io.File dir = new java.io.File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                // 保存文件
                java.io.File dest = new java.io.File(uploadDir + fileName);
                avatar.transferTo(dest);
                // 设置头像URL
                user.setAvatarUrl("/images/" + fileName);
            }
            // 更新用户资料
            return userMapper.updateUserProfile(user) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public User login(LoginRequest loginRequest) {
        // 1. 根据用户名查用户
        User user = userMapper.selectByUsername(loginRequest.getUsername());
        if (user == null) {
            return null; // 用户名不存在
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return null; // 密码错误
        }

        // 3. 脱敏返回
        user.setPassword(null);
        return user;
    }

    @Override
    public User register(RegisterRequest registerRequest) {
        // 1. 校验两次密码是否一致
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new RuntimeException("两次密码输入不一致");
        }

        // 2. 校验用户名是否已存在
        User existUser = userMapper.selectByUsername(registerRequest.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已被注册");
        }

        // 3. 校验手机号是否已存在
        User existPhoneUser = userMapper.selectByPhone(registerRequest.getPhone());
        if (existPhoneUser != null) {
            throw new RuntimeException("手机号已被注册");
        }

        // 4. 校验验证码（测试用固定值，后续替换为短信逻辑）
        if (!"495237".equals(registerRequest.getCode())) {
            throw new RuntimeException("验证码错误");
        }

        // 5. 密码加密
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());

        // 6. 构建用户对象（修正：时间类型用LocalDateTime，和实体类匹配）
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(encodedPassword);
        user.setPhone(registerRequest.getPhone());
        user.setNickname(registerRequest.getUsername()); // 解决setNickname报错
        user.setGender(0);
        user.setUserLevel(1);
        user.setUid("A" + System.currentTimeMillis());
        user.setCreateTime(LocalDateTime.now()); // 类型匹配，解决时间报错
        user.setUpdateTime(LocalDateTime.now());
        user.setIsDeleted(0);

        // 7. 插入数据库
        int insertResult = userMapper.insertUser(user);
        if (insertResult > 0) {
            user.setPassword(null);
            return user;
        } else {
            throw new RuntimeException("注册失败，请重试");
        }
    }
}