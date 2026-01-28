package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.model.Account;
import com.example.demo.repository.AccountRepository;

@Controller
public class AuthController {

    @Autowired
    private AccountRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("account", new Account());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute Account account, Model model) {
        // ユーザー名の重複チェック
        if (repository.findByUsername(account.getUsername()).isPresent()) {
            model.addAttribute("error", "そのユーザー名は既に使用されています。");
            return "register";
        }

        // パスワードの一致チェック
        // Account.javaに追加した passwordConfirm と password を比較します
        if (account.getPasswordConfirm() == null || 
            !account.getPassword().equals(account.getPasswordConfirm())) {
            
            model.addAttribute("passwordError", "パスワードが一致しません。");
            return "register";
        }

        // 暗号化して保存
        String encodedPassword = passwordEncoder.encode(account.getPassword());
        account.setPassword(encodedPassword);
        account.setRole("ROLE_USER");

        repository.save(account);
        
        return "redirect:/login?register_success";
    }
}