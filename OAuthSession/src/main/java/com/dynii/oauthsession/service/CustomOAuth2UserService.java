package com.dynii.oauthsession.service;

import com.dynii.oauthsession.dto.CustomOAuth2User;
import com.dynii.oauthsession.dto.GoogleResponse;
import com.dynii.oauthsession.dto.NaverResponse;
import com.dynii.oauthsession.dto.OAuth2Response;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 부모 객체에서 받은 userRequest 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println(oAuth2User.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("naver")) {

            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        }
        else if (registrationId.equals("google")) {

            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }
        else {

            return null;
        }
        // 가장 기본 role이 user임을 가장하고 임의로 하드코딩
        String role = "ROLE_USER";
        return new CustomOAuth2User(oAuth2Response, role);

    }
}
