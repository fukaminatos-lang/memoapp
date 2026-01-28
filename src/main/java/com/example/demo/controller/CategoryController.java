package com.example.demo.controller;

import java.security.Principal;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.Account;
import com.example.demo.model.Category;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.CategoryRepository;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired private CategoryRepository categoryRepository;
    @Autowired private AccountRepository accountRepository;

    private Account getLoginAccount(Principal principal) {
        return accountRepository.findByUsername(principal.getName()).get();
    }

    // カテゴリ一覧画面の表示
    @GetMapping
    public String list(Model model, Principal principal) {
        Account loginAccount = getLoginAccount(principal);
        List<Category> categories = categoryRepository.findByAccount(loginAccount);
        
        model.addAttribute("categories", categories);
        return "category/categorylist"; // templates/category/categorylist.html を指す
    }

    // カテゴリの保存
    @PostMapping("/save")
    public String save(@RequestParam String name, @RequestParam String color, Principal principal) {
        Account loginAccount = getLoginAccount(principal);
        
        Category category = new Category();
        category.setName(name);
        category.setColor(color);
        category.setAccount(loginAccount);
        
        categoryRepository.save(category);
        return "redirect:/categories";
    }

    // カテゴリの削除
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal) {
        Category category = categoryRepository.findById(id).orElseThrow();
        
        // 自分のカテゴリかチェック
        if (category.getAccount().getId().equals(getLoginAccount(principal).getId())) {
            categoryRepository.delete(category);
        }
        return "redirect:/categories";
    }
    
    @PostMapping("/addQuick")
    public String addQuick(@RequestParam String name, @RequestParam String color, Principal principal, HttpServletRequest request) {
        Account loginAccount = getLoginAccount(principal);
        
        Category category = new Category();
        category.setName(name);
        category.setColor(color);
        category.setAccount(loginAccount);
        categoryRepository.save(category);
        
        // 直前のページ（メモ作成・編集画面）に戻る
        String referer = request.getHeader("Referer");
        return "redirect:" + referer;
    }
}