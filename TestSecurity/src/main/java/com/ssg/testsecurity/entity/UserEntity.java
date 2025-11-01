package com.ssg.testsecurity.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    // entity는 ID가 필수, 생성값을 줄 것이고 생성타입은 ID
    // entity는 데이터 바구니 역할, 이전에 쓰던 VO 역할

    private String username;
    private String password;

    private String role;
}
