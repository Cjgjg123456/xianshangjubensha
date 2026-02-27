package org.example.jubensha.controller;

import jakarta.validation.Valid;
import org.example.jubensha.common.Result;
import org.example.jubensha.dto.LoginRequest;
import org.example.jubensha.dto.RegisterRequest;
import org.example.jubensha.entity.User;
import org.example.jubensha.entity.UserStatistics;
import org.example.jubensha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/user")
@CrossOrigin // 简化写法，兼容所有Spring Boot版本
public class UserController {

    // 【修正】构造器注入，解决「不建议使用字段注入」警告
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 登录接口（修正：入参用LoginRequest，调用service的login方法只传1个参数）
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody @Valid LoginRequest loginRequest) {
        try {
            User user = userService.login(loginRequest); // 只传1个参数，解决「实参个数错误」
            if (user == null) {
                return Result.fail("用户名或密码错误");
            }
            return Result.success(user);
        } catch (Exception e) {
            return Result.fail("登录失败：" + e.getMessage());
        }
    }

    /**
     * 注册接口
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody @Valid RegisterRequest registerRequest) {
        try {
            User user = userService.register(registerRequest);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("注册失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户资料接口
     */
    @GetMapping("/profile")
    public Result<User> getProfile(@RequestParam String username) {
        try {
            if (username == null || username.trim().isEmpty()) {
                return Result.fail("用户名不能为空");
            }
            User user = userService.getUserByUsername(username);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            user.setPassword(null); // 脱敏，不返回密码
            return Result.success(user);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户资料
     */
    @PostMapping("/profile")
    public Result<Boolean> updateProfile(
            @RequestParam("username") String username,
            @RequestParam("nickname") String nickname,
            @RequestParam("realName") String realName,
            @RequestParam(value = "gender", required = false) Integer gender,
            @RequestParam(value = "hobbyType", required = false) String hobbyType,
            @RequestParam(value = "birthday", required = false) String birthday,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "profile", required = false) String profile,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setNickname(nickname);
            user.setRealName(realName);
            user.setGender(gender);
            user.setHobbyType(hobbyType);
            if (birthday != null && !birthday.isEmpty()) {
                user.setBirthday(LocalDate.parse(birthday));
            }
            user.setCity(city);
            user.setProfile(profile);

            boolean success = userService.updateUserProfile(user, avatar);
            return success ? Result.success(true) : Result.fail("更新资料失败");
        } catch (Exception e) {
            return Result.fail("更新用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户统计数据
     */
    @GetMapping("/statistics")
    public Result<UserStatistics> getUserStatistics(@RequestParam String username) {
        try {
            UserStatistics stat = new UserStatistics();
            stat.setPlayRecordCount(0);
            stat.setFollowCount(0);
            stat.setHistoryCount(0);
            stat.setCommentCount(0);
            stat.setCreationCount(0);
            return Result.success(stat);
        } catch (Exception e) {
            return Result.fail("获取统计数据失败");
        }
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success(null);
    }
}