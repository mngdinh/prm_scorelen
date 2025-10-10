package com.scorelens.Controller.v2;

import com.nimbusds.jose.JOSEException;
import com.scorelens.DTOs.Request.*;
import com.scorelens.DTOs.Response.CustomerResponseDto;
import com.scorelens.DTOs.Response.GoogleLoginResponseDto;
import com.scorelens.DTOs.Request.LoginGoogleRequestDto;
import com.scorelens.Entity.ResponseObject;
import com.scorelens.Security.TokenCookieManager;
import com.scorelens.Service.AuthenticationService;
import com.scorelens.Service.AuthenticationV2Service;
import com.scorelens.Service.CustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@Tag(name = "Authentication", description = "Authentication APIs")
@RestController
@RequestMapping("v2/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationV2Controller {

    TokenCookieManager tokenCookieManager;
    AuthenticationV2Service authenticationService;
    AuthenticationService authenticationServiceV1;
    CustomerService customerService;

    @PostMapping("/login")
    ResponseObject authenticate(@RequestBody AuthenticationRequestDto request, HttpServletResponse response) {
        var result = authenticationService.authenticateV2(request);

        //lưu refreshtoken & accesstoken vào Cookie
        tokenCookieManager.addAuthCookies(response, result.accessToken(), result.refreshToken());
        return ResponseObject.builder()
                .status(1000)
                .data(result.responseDto())
                .message("Login successfully!!")
                .build();
    }

    @PostMapping("/register")
    ResponseObject register(@RequestBody @Valid CustomerCreateRequestDto request) {
        CustomerResponseDto response = customerService.createCustomer(request);
        return ResponseObject.builder()
                .status(1000)
                .data(response)
                .message("Register successfully")
                .build();
    }
    
    @PostMapping("/introspect")
    ResponseObject introspect(@CookieValue(value = "AccessToken", required = false) String accessToken) {
        var result = authenticationService.introspectV2(accessToken);

        String message;
        if (!result.isAuth()) {
            if (accessToken == null || accessToken.isEmpty()) {
                message = "No authentication token found";
            } else {
                message = "Invalid authentication token";
            }
        } else {
            message = "Authentication status retrieved successfully";
        }

        return ResponseObject.builder()
                .status(1000)
                .message(message)
                .data(result)
                .build();
    }

//    @PostMapping("/refresh")
//    ResponseObject authenticate(@RequestBody RefreshV2Request request, HttpServletResponse response)
//            throws ParseException, JOSEException {
//        var result = authenticationService.refreshTokenV2(request);
//
//        tokenCookieManager.addAuthCookies(response, result.accessToken(), result.refreshToken());
//
//        return ResponseObject.builder()
//                .status(1000)
//                .data(result.responseDto())
//                .message("Login successfully!!")
//                .build();
//    }

    @PostMapping("/refresh")
    public ResponseObject authenticate(@CookieValue("RefreshToken") String refreshToken,
                                       HttpServletResponse response)
            throws ParseException, JOSEException {

        var result = authenticationService.refreshTokenV2(refreshToken);

        tokenCookieManager.addAuthCookies(response, result.accessToken(), result.refreshToken());

        return ResponseObject.builder()
                .status(1000)
                .data(result.responseDto())
                .message("Login successfully!!")
                .build();
    }

    @PostMapping("/login-google")
    ResponseObject authenticateGoogle(@RequestBody LoginGoogleRequestDto request, HttpServletResponse response) {
        var result = authenticationServiceV1.authenticateGoogle(request);

        // Lưu refreshtoken & accesstoken vào Cookie
        tokenCookieManager.addAuthCookies(response, result.getAccessToken(), result.getRefreshToken());

        // Tạo response DTO không chứa tokens
        GoogleLoginResponseDto responseDto = GoogleLoginResponseDto.builder()
                .authenticated(result.isAuthenticated())
                .user(result.getUser())
                .userType(result.getUserType())
                .build();

        return ResponseObject.builder()
                .status(1000)
                .data(responseDto)
                .message("Login with Google successfully!!")
                .build();
    }

    @PostMapping("/logout")
    public ResponseObject logout(@CookieValue(value = "AccessToken", required = false) String accessToken,
                                 @CookieValue(value = "RefreshToken", required = false) String refreshToken,
                                 HttpServletResponse response) throws ParseException, JOSEException {

        // Logout và blacklist tokens
        if (accessToken != null || refreshToken != null) {
            authenticationService.logout(accessToken, refreshToken);
        }

        // Clear cookies
        tokenCookieManager.clearAuthCookies(response);

        return ResponseObject.builder()
                .status(1000)
                .message("Logout successfully")
                .data(null)
                .build();
    }

    @PostMapping("/password-forgot")
    public ResponseObject forgotPassword(@RequestBody ForgotPasswordRequestDto request) {
        authenticationService.forgotPassword(request);
        return ResponseObject.builder()
                .status(1000)
                .message("Password reset email sent successfully")
                .data(null)
                .build();
    }

    @PostMapping("/password-reset")
    public ResponseObject resetPassword(@RequestBody ResetPasswordRequestDto request) {
        authenticationService.resetPassword(request);
        return ResponseObject.builder()
                .status(1000)
                .message("Password reset successfully")
                .data(null)
                .build();
    }

}
