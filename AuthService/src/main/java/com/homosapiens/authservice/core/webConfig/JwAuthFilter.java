package com.homosapiens.authservice.core.webConfig;

import com.homosapiens.authservice.core.exception.AppException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwAuthFilter extends OncePerRequestFilter {
    private final JWTAuthProvider jwtAuthProvider;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(header!=null){
            String[] authElements = header.split(" ");
            if(authElements.length==2  && "Bearer".equals(authElements[0])){
                try {
                    SecurityContextHolder.getContext().
                            setAuthentication(jwtAuthProvider.validateToken(authElements[1]));
                } catch (AppException e) {
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
                    return;
                } catch (Exception e) {
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
