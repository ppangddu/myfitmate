package com.myfitmate.myfitmate.security;

import com.myfitmate.myfitmate.domain.user.entity.User;
import com.myfitmate.myfitmate.domain.user.service.CustomerUserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomerUserDetailService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        // 🔐 인증 없이 통과시킬 경로: 회원가입, 로그인 등
        if (uri.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String header = request.getHeader("Authorization");

            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                Long userId = jwtUtil.extractUserId(token); // 내부에서 유효성 검증도 수행해야 안정적

                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetailsImpl userDetails = userDetailsService.loadUserById(userId);
                    JwtAuthenticationToken auth = new JwtAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    // 컨트롤러에서 사용 가능하게 userId 저장
                    request.setAttribute("userId", userId);
                }
            }
        } catch (Exception e) {
            // 예외 발생 시 인증 안 된 상태로 그냥 다음 필터 진행
            logger.warn("🔐 JWT 필터 처리 실패: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
