package com.example.demo.controller;

import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.Account;
import com.example.demo.repository.AccountRepository;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired private AccountRepository repository;
    @Autowired private PasswordEncoder passwordEncoder;

    // 変更画面の表示
    @GetMapping("/password")
    public String passwordForm() {
        return "account/password";
    }

    // 変更処理
    @PostMapping("/password")
    public String updatePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String newPasswordConfirm,
            Principal principal, Model model) {

        Account account = repository.findByUsername(principal.getName()).get();

        // 現在のパスワードが正しいかチェック
        if (!passwordEncoder.matches(currentPassword, account.getPassword())) {
            model.addAttribute("error", "現在のパスワードが正しくありません。");
            return "account/password";
        }

        // 新しいパスワードが一致するかチェック
        if (!newPassword.equals(newPasswordConfirm)) {
            model.addAttribute("error", "新しいパスワードが一致しません。");
            return "account/password";
        }

        // 保存
        account.setPassword(passwordEncoder.encode(newPassword));
        repository.save(account);

        model.addAttribute("success", "パスワードを変更しました。");
        return "account/password";
    }
    
    @PostMapping("/delete")
    public String deleteAccount(@RequestParam String password, 
                                Principal principal, 
                                HttpServletRequest request) throws Exception {
        
        Account account = repository.findByUsername(principal.getName()).get();

        if (!passwordEncoder.matches(password, account.getPassword())) {
            // パスワードが違った場合、一覧画面（/memos）にエラーを付けて戻す
            return "redirect:/memos?error=delete_failed";
        }

        repository.delete(account);
        request.logout();

        return "redirect:/login?delete_success";
    }
}