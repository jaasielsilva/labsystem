package com.jaasielsilva.labsystem.config;

import com.jaasielsilva.labsystem.common.AccessScope;
import com.jaasielsilva.labsystem.common.ImpersonationContext;
import com.jaasielsilva.labsystem.common.ImpersonationState;
import com.jaasielsilva.labsystem.common.JwtTokenContext;
import com.jaasielsilva.labsystem.features.auth.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            authenticateIfPresent(request);
            filterChain.doFilter(request, response);
        } finally {
            ImpersonationContext.clear();
        }
    }

    private void authenticateIfPresent(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        try {
            String token = authHeader.substring(7);
            if (!jwtService.isAccessToken(token)) {
                return;
            }

            JwtTokenContext tokenContext = jwtService.readTokenContext(token);
            if (tokenContext.scope() == AccessScope.TENANT_IMPERSONATION) {
                ImpersonationContext.set(ImpersonationState.of(
                        tokenContext.actingEmpresaId(),
                        tokenContext.actingEmpresaNome()
                ));
            }

            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                return;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(tokenContext.email());
            if (!jwtService.isTokenValid(token, userDetails.getUsername())) {
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            ImpersonationContext.clear();
        }
    }
}
