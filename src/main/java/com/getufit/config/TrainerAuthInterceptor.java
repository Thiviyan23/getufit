package com.getufit.config;

import com.getufit.model.TrainerAccount;
import com.getufit.repository.TrainerAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TrainerAuthInterceptor implements HandlerInterceptor {
    private final TrainerAccountRepository trainers;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        HttpSession session = request.getSession(false);
        Long id = session == null ? null : (Long) session.getAttribute("trainerId");
        TrainerAccount trainer = id == null ? null : trainers.findById(id).orElse(null);
        if (trainer == null || trainer.getPasswordHash() == null) {
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/trainer/login");
            return false;
        }
        request.setAttribute("currentTrainer", trainer);
        return true;
    }
}
