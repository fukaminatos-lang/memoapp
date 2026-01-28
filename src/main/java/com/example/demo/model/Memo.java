package com.example.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
public class Memo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = true)
    private Category category; // カテゴリは任意なのでアノテーションなしでOK
    
    @NotBlank(message = "タイトルを入力してください") // titleの直前に移動
    @Size(max = 20, message = "タイトルは20文字以内で入力してください") // titleの直前に移動
    private String title;

    @NotBlank(message = "内容を入力してください")
    private String content;

    private boolean publicFlag;
    
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
    
    @CreatedDate // 作成時に自動セット
    @Column(updatable = false) // 更新時には書き換えない
    private LocalDateTime createdAt;

    @LastModifiedDate // 更新時に自動セット
    private LocalDateTime updatedAt;
}