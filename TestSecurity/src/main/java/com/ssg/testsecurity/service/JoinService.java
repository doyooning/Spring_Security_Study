package com.ssg.testsecurity.service;

import com.ssg.testsecurity.dto.JoinDTO;
import com.ssg.testsecurity.entity.UserEntity;
import com.ssg.testsecurity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JoinService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public void joinProcess(JoinDTO joinDTO) {

        // 아이디 중복 검사
        boolean isUser = userRepository.existsByUsername(joinDTO.getUsername());
        if (isUser) {
            return; // 이미 존재하는 유저이면 메서드 종료
        }

        UserEntity data = new UserEntity();

        data.setUsername(joinDTO.getUsername());
        data.setPassword(bCryptPasswordEncoder.encode(joinDTO.getPassword()));
//        data.setRole("ROLE_USER"); // ROLE_역할
        data.setRole("ROLE_ADMIN"); // ROLE_역할

        userRepository.save(data);
    }
}
