package com.example.demo.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.model.Account;
import com.example.demo.repository.AccountRepository;

@Service 
public class AccountDetailsService implements UserDetailsService {

    @Autowired
    private AccountRepository repository;

    /**
     * ログイン時にSpring Securityが自動的に呼び出すメソッド
     * @param username ユーザーが入力したログインID
     * @return Spring Securityが理解できるユーザー情報(UserDetails)
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        // 1. データベースからユーザーを探す
        Optional<Account> accountOpt = repository.findByUsername(username);

        // 2. もし見つからなかったらエラーを投げる
        if (accountOpt.isEmpty()) {
            throw new UsernameNotFoundException("ユーザーが見つかりません: " + username);
        }

        Account account = accountOpt.get();

        // 3. データベースの情報を、Spring Security専用の「User」オブジェクトに詰め替えて返す
        return User.withUsername(account.getUsername())
                .password(account.getPassword()) // 暗号化済みのパスワード
                .roles("USER")                   // 権限を設定（ROLE_USERとして扱われる）
                .build();
    }
}