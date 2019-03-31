package com.github.wuchao.activiti.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementation of AuditorAware based on Spring Security.
 */
@Component
public class SpringSecurityAuditorAware implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        CustomUser customUser = SecurityUtils.getCurrentUser();
        return customUser != null ?
                Optional.of(customUser.getUserId()) : Optional.empty();
    }

}
