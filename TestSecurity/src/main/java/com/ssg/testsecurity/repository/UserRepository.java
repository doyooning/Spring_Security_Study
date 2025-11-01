package com.ssg.testsecurity.repository;

import com.ssg.testsecurity.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    // repository는 인터페이스로 생성, jparepository 상속받도록 작성
    // 맵핑은 entity:id -> UserEntity, Integer
}
