package com.scorelens.Config;

import com.google.api.Http;
import com.scorelens.Enums.StaffRole;
import com.scorelens.Enums.UserType;
import com.scorelens.Security.CustomAccessDeniedHandler;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.crypto.spec.SecretKeySpec;
import java.beans.Encoder;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity(debug = false)
@EnableMethodSecurity(prePostEnabled = true)
@CrossOrigin(origins = {"http://localhost:5173", "exp://192.168.90.68:8081"})
public class SecurityConfig {

    private final String[] PUBLIC_ENDPOINTS = {
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
            "/v*/ping",
            "/index.html",
            "/ws/**",
            "/ws-native", "/ws-native/**",
            "/v3/health"
    };

    private final String[] MANAGER_ENDPOINTS = {
            "/v*/s3",
            "/v*/s3/**",
            "/v*/kafka/**",
            "/v*/heartbeats",
            "/v*/heartbeats/**",
            "/v*/fcm/**",
            "/v*/qr_codes",
            "/v*/web_socket"

    };

    public final String[] ADMIN_ENDPOINTS = {
            "/v*/stores",
            "/v*/stores/**"
    };

    public final String[] CUSTOMER_ENDPOINTS = {
            "/v*/events",
            "/v*/events/player/**",
            "/v*/events/game_set/**",
            "/v*/players",
            "/v*/players/**",
            "/v*/notifications",
            "/v*/gamesets",
            "/v*/gamesets/**",
            "/v*/teams",
            "/v*/teams/**"

    };



    @Autowired
    @Lazy
    private CustomJwtDecoder customJwtDecoder;

    @Autowired
    private CookieJwtAuthenticationFilter cookieJwtAuthenticationFilter;

    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(request -> request
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers(HttpMethod.GET, "/v*/tables").permitAll()
                .requestMatchers(HttpMethod.POST, "/v*/tables").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.PUT, "/v*/tables").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/v*/tables/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.POST, "/v*/billiard-matches", "/v3/fcm/operation").permitAll()
                .requestMatchers(HttpMethod.POST, "/v*/billiard-matches").hasAnyRole("CUSTOMER", "STAFF", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/v*/billiard-matches").hasAnyRole( "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/v*/billiard-matches/**").hasAnyRole( "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/v*/billiard-matches").hasAnyRole("CUSTOMER", "STAFF", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/v*/billiard-matches").hasAnyRole("CUSTOMER", "STAFF", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/v*/billiard-matches/byEmail/**").hasAnyRole("CUSTOMER", "STAFF", "MANAGER", "ADMIN")
                .requestMatchers(CUSTOMER_ENDPOINTS).hasAnyRole("CUSTOMER", "STAFF", "MANAGER", "ADMIN")
                .requestMatchers(MANAGER_ENDPOINTS).hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.POST, "/v*/modes").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.PUT, "/v*/modes/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/v*/modes/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.GET, "/v*/modes").hasAnyRole("CUSTOMER", "STAFF", "MANAGER", "ADMIN")
                .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")
                .anyRequest().authenticated());

        http.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer ->
                                jwtConfigurer.decoder(customJwtDecoder)
                                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                        .accessDeniedHandler(customAccessDeniedHandler));

        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults());

        // Thêm cookie JWT filter trước OAuth2 resource server
//        http.addFilterBefore(cookieJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<GrantedAuthority> authorities = new ArrayList<>();

            // Lấy claim "role"
            String role = jwt.getClaimAsString("role");
            if (role != null && !role.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
            }

            System.out.println("✅ Extracted authorities from JWT: " + authorities);

            return authorities;
        });
        return converter;
    }

    //giải mã JWT -> ĐÃ DÙNG CUSTOMJWTDECODER
//    @Bean
//    JwtDecoder jwtDecoder(){
//        SecretKeySpec secretKeySpec = new SecretKeySpec(SIGNER_KEY.getBytes(), "HS512");
//        return NimbusJwtDecoder.withSecretKey(secretKeySpec)
//                .macAlgorithm(MacAlgorithm.HS512)
//                .build();
//    }

    //CORS handling
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173",
                "https://localhost:5173",
                "https://score-lens.vercel.app",
                "https://scorelens.onrender.com",
                "https://scorelens-gateway.onrender.com",
                "https://localhost:7174",
                "http://localhost:7174"
        ));

        // Thêm pattern cho mobile apps
        corsConfiguration.setAllowedOriginPatterns(Arrays.asList(
                "exp://*",           // Expo mobile apps
                "capacitor://*",     // Capacitor apps
                "ionic://*",         // Ionic apps
                "file://*",          // Local file protocol
                "*://192.168.*.*:*", // Local network IPs
                "*://10.*.*.*:*",    // Private network IPs
                "*://172.*.*.*:*"    // Private network IPs
        ));
        //corsConfiguration.addAllowedOriginPattern("*"); // mở rộng cho tất cả các port localhost
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource url = new UrlBasedCorsConfigurationSource();
        url.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(url);
    }

//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/**")
//                        .allowedOrigins("http://localhost:3000")
//                        .allowedMethods("*")
//                        .allowCredentials(true);
//            }
//        };
//    }

    //Encoding password
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
