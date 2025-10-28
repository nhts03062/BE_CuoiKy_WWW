package fit.iuh.se.security;

import fit.iuh.se.repositories.UserAccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserAccountRepository userRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/api/auth") ||
                path.startsWith("/auth") ||
                (path.startsWith("/api/cart") && !path.equals("/api/cart/checkout")) ||
                (path.startsWith("/cart") && !path.equals("/cart/checkout")) ||
                path.startsWith("/api/payment/callback") ||
                path.startsWith("/api/payment/ipn") ||
                path.startsWith("/payment/callback") ||
                path.startsWith("/payment/ipn") ||
                path.startsWith("/api/products") ||
                path.startsWith("/api/categories")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || header.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.toLowerCase().startsWith("bearer ")
                ? header.substring(7).trim()
                : header.trim();

        if (!jwtUtils.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtUtils.extractEmail(token);
        String role = jwtUtils.extractRole(token);
        if (role != null && !role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            userRepo.findByEmail(email).ifPresent(user -> {
                CustomUserDetails userDetails = new CustomUserDetails(user);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            });
        }

        filterChain.doFilter(request, response);
    }
}