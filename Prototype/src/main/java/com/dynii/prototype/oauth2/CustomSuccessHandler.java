package com.dynii.prototype.oauth2;

import com.dynii.prototype.dto.CustomOAuth2User;
import com.dynii.prototype.entity.RefreshEntity;
import com.dynii.prototype.jwt.JWTUtil;
import com.dynii.prototype.repository.RefreshRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Log4j2
@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        CustomOAuth2User user = (CustomOAuth2User) authentication.getPrincipal();
        String username = user.getUsername();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        long accessExpiryMs = 600000L;
        long refreshExpiryMs = 86400000L;
        String access = jwtUtil.createJwt("access", username, role, accessExpiryMs);
        String refresh = jwtUtil.createJwt("refresh", username, role, refreshExpiryMs);

        addRefreshEntity(username, refresh, refreshExpiryMs);

        response.setHeader("access", access);
        response.addCookie(createCookie("refresh", refresh, Math.toIntExact(refreshExpiryMs / 1000)));
        response.sendRedirect("http://localhost:5173/my");
    }

    private Cookie createCookie(String key, String value, int maxAge) {

        Cookie cookie = new Cookie(key, value);

        cookie.setMaxAge(maxAge);
        // cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }

    private void addRefreshEntity(String username, String refresh, Long expiredMs) {

        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUsername(username);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(date.toString());

        refreshRepository.save(refreshEntity);
    }
}
