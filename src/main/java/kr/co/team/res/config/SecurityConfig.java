package kr.co.team.res.config;

import kr.co.team.res.common.handlers.CustomLoginFailureHandler;
import kr.co.team.res.common.handlers.CustomLoginSuccessHandler;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.CharacterEncodingFilter;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    ApplicationContext context;

    @Autowired
    UserDetailsService userDetailsService;


    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return new CustomLoginSuccessHandler("/loginSuccess");
    }

    @Bean
    public AuthenticationFailureHandler failureHandler(String userId, String userPw) {
        return new CustomLoginFailureHandler("/loginFailure", userId, userPw);
    }

    /**
     * resource/static 제외
     *
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().mvcMatchers("/favicon.ico");
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
        //web.ignoring().antMatchers( "/webjars/**");
    }

    // 라우터 세팅
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        CharacterEncodingFilter filter = new CharacterEncodingFilter();

        http.headers().frameOptions().disable();
        http.headers().xssProtection().block(true);
        http.headers().defaultsDisabled().contentTypeOptions();

        // 수정중 김재일 인증강사 추가
        http.authorizeRequests()
                .mvcMatchers("/pages/myPage/**").hasAnyRole("NORMAL,STUDENT,PARENT,TEACHER,INSTRUCTOR")
                .mvcMatchers("/expert/**", "/device/**", "/pages/expert/**", "/pages/device/**", "/ui/**","/node_modules/**","/loginFailure","/message","/error"
                        ,"/fragments/**","/popup/**","/","/index","/login","/api/common/download").permitAll()
                .mvcMatchers("/pages/**","/api/member/**","/api/nice/**","/api/openData/**","/upload/**","/api/commonCode/listForUppCdPid").permitAll()
                .mvcMatchers("/soulGod/**").hasAnyRole("MASTER,ADMIN,LECTURER,COUNSELOR")
                .anyRequest().authenticated();

        http.formLogin()
                .loginPage("/login").permitAll()
                .successForwardUrl("/loginSuccess")
                .usernameParameter("userId")
                .passwordParameter("userPw")
                .failureHandler(failureHandler("userId", "userPw"))
        ;
        http.rememberMe()
                .userDetailsService(userDetailsService)
                .key("remember-me-key");

        http.logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true);
                /*.and()
                    .apply(new SpringSocialConfigurer())*/
        http.addFilterBefore(filter, CsrfFilter.class)
                .csrf().disable();

        http.sessionManagement()
                .sessionFixation()
                .changeSessionId()
                .invalidSessionUrl("/")
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false) //false : 이전세션아웃, true : 이전세션점유
                .expiredUrl("/pages/session/duplication ");
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}
