package com.scorelens.Service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.scorelens.DTOs.Request.AuthenticationRequestDto;
import com.scorelens.DTOs.Request.IntrospectRequestDto;
import com.scorelens.DTOs.Request.LogoutRequestDto;
import com.scorelens.DTOs.Request.RefreshRequest;
import com.scorelens.DTOs.Response.AuthenticationResponseDto;
import com.scorelens.DTOs.Response.IntrospectResponseDto;

import com.scorelens.Entity.InvalidatedToken;
import com.scorelens.Enums.UserType;
import com.scorelens.Exception.AppException;
import com.scorelens.Exception.ErrorCode;

import com.scorelens.Repository.InvalidatedTokenRepository;
import com.scorelens.Service.Interface.IAuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService implements IAuthenticationService {


    InvalidatedTokenRepository invalidatedTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${jwt.signerKey}") //Đọc từ file application.yaml
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}") //Đọc từ file application.yaml
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}") //Đọc từ file application.yaml
    protected long REFRESHABLE_DURATION;



    //--------------------------------------- AUTHENTICATION --------------------------------------------------
    @Override
    public AuthenticationResponseDto authenticate(AuthenticationRequestDto request) {

        String token;

        token = generateToken(request);
        return AuthenticationResponseDto.builder()
                .authenticated(true)
                .token(token)
                .user(request)
                .userType(UserType.CUSTOMER)
                .build();
    }

    //--------------------------------------- LOGOUT -----------------------------------------------------------
    public void logout(LogoutRequestDto request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);

            String jti = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jti)
                    .expiryTime(expiryTime)
                    .build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException e){
            log.info("Token is already expired");
        }
    }

    //-----------------------------------VERIFY---------------------------------------
    private SignedJWT verifyToken(String token, boolean isRefresh) throws ParseException, JOSEException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime()
                .toInstant().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                :
                signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);
        if(!(verified && expiryTime.after(new Date())))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if(invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    public AuthenticationResponseDto refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signJWT = verifyToken(request.getAccessToken(), true);

        var jti = signJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signJWT.getJWTClaimsSet().getExpirationTime();

        //lưu token đã bị invalidated vào db - phần này của logout
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jti)
                .expiryTime(expiryTime)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);



        String token = generateToken(request);
        return AuthenticationResponseDto.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    private String generateToken(Object userEntity) {
        String email = "";
        String userId = "";
        String scope = "";


        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(email)
                .claim("userID", userId)
                .claim("scope", scope)
                .jwtID(UUID.randomUUID().toString())
                .issuer("scorelens")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .build();

        try {
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS512),
                    claims
            );
            signedJWT.sign(new MACSigner(SIGNER_KEY.getBytes(StandardCharsets.UTF_8)));
            return signedJWT.serialize(); // SignedJWT trả về chuỗi JWT chuẩn
        } catch (JOSEException e) {
            log.error("Token generation failed", e);
            throw new RuntimeException(e);
        }
    }


    @Override
    public IntrospectResponseDto introspect(IntrospectRequestDto request)
            throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;;
        try {
            verifyToken(token, false);
        } catch (AppException e){
            isValid = false;
        }
        return IntrospectResponseDto.builder()
                .valid(isValid)
                .build();
    }


}
