package com.github.wuchao.activiti.security;

import com.github.wuchao.activiti.domain.User;
import com.github.wuchao.activiti.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.assertj.core.util.Lists;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Authenticate a user from the database.
 */
@Slf4j
@Component("domainUserDetailsService")
public class DomainUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

    public DomainUserDetailsService() {
        super();
    }

    public DomainUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDetails loadUserByUsername(final String username) {
        log.debug("Authenticating {}", username);
        String lowercaseUsername = username.toLowerCase(Locale.ENGLISH);
        Optional<User> userOptional = userRepository.findTopByUsername(lowercaseUsername);
        return userOptional.map(user -> {
            if (!user.isActivated()) {
                throw new UserNotActivatedException("User " + lowercaseUsername + " was not activated");
            }
            List<GrantedAuthority> grantedAuthorities = Lists.newArrayList(new SimpleGrantedAuthority("LEAVE"));
            if (CollectionUtils.isNotEmpty(grantedAuthorities)) {
                return new CustomUser(user.getId(), user.getNickname(),
                        lowercaseUsername,
                        user.getPassword(),
                        grantedAuthorities);
            } else {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                RuntimeException exception = new RuntimeException("Access denied");
                saveException(request, exception);
                throw exception;
            }
        }).orElseThrow(() -> new UsernameNotFoundException("User " + lowercaseUsername + " was not found in the " +
                "database"));
    }

    private void saveException(HttpServletRequest request, RuntimeException exception) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            request.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, exception);
        } else {
            request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, exception);
        }
    }

}
