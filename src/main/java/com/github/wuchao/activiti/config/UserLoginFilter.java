package com.github.wuchao.activiti.config;

import com.github.wuchao.activiti.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UserLoginFilter extends GenericFilterBean {

    private SimpleUrlAuthenticationFailureHandler failureHandler;

    private UserDetailsService domainUserDetailsService;

    private UserRepository userRepository;

    public UserLoginFilter(SimpleUrlAuthenticationFailureHandler failureHandler, UserDetailsService domainUserDetailsService, UserRepository userRepository) {
        this.failureHandler = failureHandler;
        this.domainUserDetailsService = domainUserDetailsService;
        this.userRepository = userRepository;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (new AntPathRequestMatcher("/api/login").matches(request)) {
            String loginName = request.getParameter("j_username");
            if (StringUtils.isNotBlank(loginName)) {
                UserDetails userDetails = domainUserDetailsService.loadUserByUsername(loginName);

                UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                result.setDetails(new WebAuthenticationDetails(request));
                SecurityContextHolder.getContext().setAuthentication(result);
                request.getSession(true);
            }
        }

        filterChain.doFilter(request, response);
    }
}
