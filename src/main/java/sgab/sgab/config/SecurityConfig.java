package sgab.sgab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.security.web.SecurityFilterChain;

//Configuração para liberação do h2 database em ambiente de desenvolvimento. Quando for para produção ela deve ser alterada.

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        var h2Console = PathRequest.toH2Console();

        http
                .csrf(csrf -> csrf.ignoringRequestMatchers(h2Console))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(h2Console).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.permitAll());

        return http.build();
    }
}
