package com.example.demo.controller;

import java.security.Principal;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.Account;
import com.example.demo.model.Category;
import com.example.demo.model.Memo;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.MemoRepository;

@Controller
@RequestMapping("/memos")
public class MemoController {

    @Autowired private MemoRepository repository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private CategoryRepository categoryRepository;

    // ログイン中のユーザー(Account)を取得する共通メソッド
    private Account getLoginAccount(Principal principal) {
        return accountRepository.findByUsername(principal.getName()).get();
    }
    
    @GetMapping
    public String list(@RequestParam(name = "keyword", required = false) String keyword,
                       @RequestParam(name = "categoryId", required = false) Long categoryId, // 追加
                       @RequestParam(name = "sort", defaultValue = "desc") String sortDirection,
                       Model model, Principal principal) {
        
        Account loginAccount = getLoginAccount(principal);
        
        // ソート順の決定
        Sort sort = sortDirection.equalsIgnoreCase("asc") 
                    ? Sort.by("createdAt").ascending() 
                    : Sort.by("createdAt").descending();

        List<Memo> list;
        
        // 条件分岐の整理
        if (categoryId != null) {
            // A. カテゴリ絞り込みがある場合
            if (keyword != null && !keyword.isEmpty()) {
                list = repository.findByAccountAndCategoryIdAndTitleContaining(loginAccount, categoryId, keyword, sort);
            } else {
                list = repository.findByAccountAndCategoryId(loginAccount, categoryId, sort);
            }
        } else {
            // B. カテゴリ絞り込みがない場合
            if (keyword != null && !keyword.isEmpty()) {
                list = repository.findByAccountAndTitleContaining(loginAccount, keyword, sort);
            } else {
                list = repository.findByAccount(loginAccount, sort);
            }
        }
        
        // 画面に渡すデータ
        model.addAttribute("memos", list);
        model.addAttribute("categories", categoryRepository.findByAccount(loginAccount)); // フィルタ用
        model.addAttribute("selectedCategoryId", categoryId); // 選択状態保持用
        model.addAttribute("sort", sortDirection);
        model.addAttribute("keyword", keyword);
        
        return "memo/list";
    }
 // 新規作成画面を表示する
    @GetMapping("/add")
    public String add(Model model, Principal principal) {
        Account loginAccount = getLoginAccount(principal);
        
        // CategoryRepositoryを使って、ユーザーが作成したカテゴリを取得するように変更
        List<Category> categories = categoryRepository.findByAccount(loginAccount);
        
        model.addAttribute("memo", new Memo());
        model.addAttribute("categories", categories); // 変数名を categories に
        return "memo/form";
    }

    // 編集画面を表示する
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model, Principal principal) {
        Memo memo = repository.findById(id).orElseThrow();
        Account loginAccount = getLoginAccount(principal);
        
        // 同様に修正
        List<Category> categories = categoryRepository.findByAccount(loginAccount);
        
        model.addAttribute("memo", memo);
        model.addAttribute("categories", categories);
        return "memo/form";
    }
    
    @PostMapping
    public String save(@Valid @ModelAttribute Memo memo, 
                       BindingResult result,
                       @RequestParam(name = "categoryId", required = false) String categoryId,
                       @RequestParam(name = "newCategoryName", required = false) String newCategoryName,
                       @RequestParam(name = "newCategoryColor", required = false) String newCategoryColor,
                       Principal principal, Model model) {
        
        Account loginAccount = getLoginAccount(principal);

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findByAccount(loginAccount));
            return "memo/form";
        }

        // 重要: 既存のデータを取得（編集の場合、作成日時などを保持するため）
        if (memo.getId() != null) {
            Memo existing = repository.findById(memo.getId()).orElseThrow();
            memo.setCreatedAt(existing.getCreatedAt());
            memo.setAccount(existing.getAccount());
        } else {
            memo.setAccount(loginAccount);
        }

     // --- カテゴリ処理ロジック ---
        if ("new".equals(categoryId)) {
            // 1. 「新しく作る」が選ばれた場合
            if (newCategoryName != null && !newCategoryName.isBlank()) {
                Category newCat = new Category();
                newCat.setName(newCategoryName);
                newCat.setColor(newCategoryColor);
                newCat.setAccount(loginAccount);
                categoryRepository.save(newCat);
                memo.setCategory(newCat);
            }
        } else if (categoryId != null && !categoryId.isBlank()) {
            // 2. 既存のカテゴリIDが送られてきた場合（空文字でないかチェック）
            try {
                Long id = Long.parseLong(categoryId);
                categoryRepository.findById(id).ifPresent(memo::setCategory);
            } catch (NumberFormatException e) {
                // 数値変換に失敗（空文字など）した場合はカテゴリなしにする
                memo.setCategory(null);
            }
        } else {
            // 3. categoryId が null または "" の場合
            memo.setCategory(null);
        }
        repository.save(memo);
        return "redirect:/memos";
    }
    
 // メモを削除する処理
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal) {
        // 削除対象のメモを取得
        Memo memo = repository.findById(id).orElseThrow();
        
        // 自分のメモ以外は消せないようにチェック
        Account loginAccount = getLoginAccount(principal);
        if (!memo.getAccount().getId().equals(loginAccount.getId())) {
            return "redirect:/memos?error=unauthorized";
        }

        repository.delete(memo);
        return "redirect:/memos";
    }
}