package com.finance.service;

import com.finance.config.JwtUtil;
import com.finance.dto.LoginRequest;
import com.finance.dto.PasswordUpdateRequest;
import com.finance.dto.ProfileUpdateRequest;
import com.finance.dto.RegisterRequest;
import com.finance.entity.User;
import com.finance.repository.UserRepository;
import com.finance.common.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务 - 注册、登录
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    public Map<String, Object> register(RegisterRequest request) {
        // 检查用户名和邮箱是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());

        userRepository.save(user);

        // 注册成功后直接生成 Token
        Map<String, Object> result = profileResult(user);
        result.put("token", jwtUtil.generateToken(user.getId(), user.getUsername()));
        return result;
    }

    /**
     * 用户登录
     */
    public Map<String, Object> login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseGet(() -> userRepository.findByEmail(request.getUsername())
                        .orElseThrow(() -> new RuntimeException("用户不存在")));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("email", user.getEmail());
        result.put("phone", user.getPhone());
        result.put("avatar", user.getAvatar());
        return result;
    }

    /**
     * 获取当前用户资料
     */
    public Map<String, Object> getProfile() {
        User user = currentUser();
        return profileResult(user);
    }

    /**
     * 更新当前用户资料
     */
    public Map<String, Object> updateProfile(ProfileUpdateRequest request) {
        User user = currentUser();
        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAvatar(request.getAvatar());
        userRepository.save(user);

        Map<String, Object> result = profileResult(user);
        result.put("token", jwtUtil.generateToken(user.getId(), user.getUsername()));
        return result;
    }

    /**
     * 修改当前用户密码
     */
    public void updatePassword(PasswordUpdateRequest request) {
        User user = currentUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("当前密码错误");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("新密码不能与当前密码相同");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private User currentUser() {
        return userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    private Map<String, Object> profileResult(User user) {
        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("email", user.getEmail());
        result.put("phone", user.getPhone());
        result.put("avatar", user.getAvatar());
        return result;
    }
}
