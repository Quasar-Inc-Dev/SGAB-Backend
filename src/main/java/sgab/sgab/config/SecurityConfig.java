package sgab.sgab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.security.web.SecurityFilterChain;

//Configuração para liberação do h2 database em ambiente de desenvolvimento. Quando for para produção ela deve ser alterada.
//localhost:8080/h2-console
//localhost:8080/swagger-ui/index.html

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        var h2Console = PathRequest.toH2Console();

        http
                .csrf(csrf -> csrf.disable()) // API stateless não precisa de CSRF
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(h2Console).permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuarios/cadastro").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuarios/cadastro/leitor").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuarios/buscarPorCpf").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/usuario/deletar/{id}").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> {}); 

        return http.build();
    }
}
