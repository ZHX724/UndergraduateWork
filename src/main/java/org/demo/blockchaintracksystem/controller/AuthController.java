package org.demo.blockchaintracksystem.controller;

import jakarta.servlet.http.HttpSession;
import org.demo.blockchaintracksystem.entity.User;
import org.demo.blockchaintracksystem.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:5173", "http://192.168.1.7:5173"}, allowCredentials = "true")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserMapper userMapper;

    public AuthController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session) {
        User user = userMapper.findByUsername(username);
        if (user == null) return "用户不存在";
        if (!user.getPasswordHash().equals(password)) return "密码错误";


        session.setAttribute("uid", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", user.getRole());

        return "登录成功";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "已退出";
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpSession session) {
        Map<String, Object> m = new HashMap<>();
        Object uid = session.getAttribute("uid");
        if (uid == null) {
            m.put("login", false);
            return m;
        }
        m.put("login", true);
        m.put("userId", uid);
        m.put("username", session.getAttribute("username"));
        m.put("role", session.getAttribute("role"));
        return m;
    }
}
