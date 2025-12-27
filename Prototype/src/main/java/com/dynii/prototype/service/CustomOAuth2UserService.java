package com.dynii.prototype.service;

import com.dynii.prototype.dto.*;
import com.dynii.prototype.entity.SellerEntity;
import com.dynii.prototype.entity.UserEntity;
import com.dynii.prototype.repository.SellerRepository;
import com.dynii.prototype.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;

    public CustomOAuth2UserService(UserRepository userRepository, SellerRepository sellerRepository) {

        this.userRepository = userRepository;
        this.sellerRepository = sellerRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println(oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;

        switch (registrationId) {
            case "google" -> oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());

            case "naver" -> oAuth2Response = new NaverResponse(oAuth2User.getAttributes());

            case "kakao" -> oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());

            default -> {
                return null;
            }
        }

        String username = oAuth2Response.getProvider()+" "+oAuth2Response.getProviderId();
        UserEntity existData = userRepository.findByUsername(username);
        SellerEntity existSeller = sellerRepository.findByLoginId(username);

        if (existData == null && existSeller == null) {

            // Build user info for a pending signup user.
            UserDTO userDTO = UserDTO.builder()
                    .username(username)
                    .name(oAuth2Response.getName())
                    .email(oAuth2Response.getEmail())
                    .role("ROLE_GUEST")
                    .newUser(true)
                    .build();

            return new CustomOAuth2User(userDTO);
        }
        else if (existData != null) {

            existData.setEmail(oAuth2Response.getEmail());
            existData.setName(oAuth2Response.getName());

            userRepository.save(existData);

            UserDTO userDTO = UserDTO.builder()
                    .username(existData.getUsername())
                    .name(oAuth2Response.getName())
                    .email(oAuth2Response.getEmail())
                    .role(existData.getRole())
                    .newUser(false)
                    .build();

            return new CustomOAuth2User(userDTO);
        }
        else {

            existSeller.setName(oAuth2Response.getName());
            existSeller.setUpdatedAt(java.time.LocalDateTime.now());

            sellerRepository.save(existSeller);

            UserDTO userDTO = UserDTO.builder()
                    .username(existSeller.getLoginId())
                    .name(existSeller.getName())
                    .email(oAuth2Response.getEmail())
                    .role(existSeller.getRole())
                    .newUser(false)
                    .build();

            return new CustomOAuth2User(userDTO);
        }
    }
}
