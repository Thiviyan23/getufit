package com.getufit.config;

import com.getufit.model.Member;
import com.getufit.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private final MemberRepository members;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        HttpSession session = request.getSession(false);
        Long id = session == null ? null : (Long) session.getAttribute("memberId");
        Member member = id == null ? null : members.findById(id).orElse(null);
        if (member == null || !"Active".equals(member.getStatus())) {
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        request.setAttribute("currentMember", member);
        return true;
    }
}