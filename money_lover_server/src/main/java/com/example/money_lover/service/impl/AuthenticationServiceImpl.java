package com.example.money_lover.service.impl;

import com.example.money_lover.dto.request.AuthenticationRequest;
import com.example.money_lover.dto.request.IntrospectRequest;
import com.example.money_lover.dto.request.LogoutRequest;
import com.example.money_lover.dto.request.RefreshRequest;
import com.example.money_lover.dto.response.ApiResponse;
import com.example.money_lover.dto.response.AuthenticationResponse;
import com.example.money_lover.dto.response.IntrospectResponse;
import com.example.money_lover.entity.InvalidatedToken;
import com.example.money_lover.entity.User;
import com.example.money_lover.exception.AppException;
import com.example.money_lover.exception.ErrorCode;
import com.example.money_lover.repository.InvalidatedTokenRepository;
import com.example.money_lover.repository.UserRepository;
import com.example.money_lover.service.EmailService;
import com.example.money_lover.service.IAuthenticationService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements IAuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final EmailService emailService;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // FIX: Thêm tham số loại token ("ACCESS", "REFRESH")
        var accessToken = generateToken(user, VALID_DURATION, "ACCESS");
        var refreshToken = generateToken(user, REFRESHABLE_DURATION, "REFRESH");

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authenticated(true)
                .build();
    }

    // LOGOUT
    @Override
    public void logout(LogoutRequest request) {
        try {
            var signToken = verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jit)
                    .expiryTime(expiryTime)
                    .build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (Exception e) {
            // Token không hợp lệ hoặc đã hết hạn thì coi như đã logout
            log.info("Token already expired or invalid, ignored.");
        }
    }

    // Refresh
    @Override
    @Transactional // Đảm bảo tính toàn vẹn dữ liệu khi xử lý refresh token rotation
    public AuthenticationResponse refreshToken(RefreshRequest request) {
        // 1. Verify Signature & Expiration
        SignedJWT signedJWT;
        try {
            signedJWT = verifyToken(request.getToken(), true);
        } catch (JOSEException | ParseException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // 2. Extract Claims MỘT LẦN
        JWTClaimsSet claims;
        try {
            claims = signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // 3. Kiểm tra Token Type (Chỉ chấp nhận token loại REFRESH)
        Object type = claims.getClaim("type");
        if (!"REFRESH".equals(type)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // 4. Kiểm tra JTI (Token Replay Detection)
        String jti = claims.getJWTID();
        Date expiryTime = claims.getExpirationTime();

        if (invalidatedTokenRepository.existsById(jti)) {
            // Nếu token này đã bị sử dụng rồi mà vẫn gửi lên -> Có dấu hiệu tấn công
            log.warn("Phat hien hanh vi bat thuong: Token ID {} da bi su dung lai!", jti);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // 5. Invalidate token cũ ngay lập tức (Token Rotation)
        invalidatedTokenRepository.save(InvalidatedToken.builder()
                .id(jti)
                .expiryTime(expiryTime)
                .build());

        // 6. Cấp cặp token mới
        String userId = claims.getSubject();
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        var accessToken = generateToken(user, VALID_DURATION, "ACCESS");
        var refreshToken = generateToken(user, REFRESHABLE_DURATION, "REFRESH");

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authenticated(true)
                .build();
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);
        } catch (Exception e) {
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    // --- PRIVATE HELPER METHODS ---

    private String generateToken(User user, long duration, String tokenType) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet.Builder jwtClaimsSetBuilder = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("money_lover.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(duration, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("email", user.getEmail())
                .claim("type", tokenType); 

        if (!CollectionUtils.isEmpty(user.getRoles())) {
            StringJoiner stringJoiner = new StringJoiner(" ");
            user.getRoles().forEach(role -> stringJoiner.add(role.getName()));
            jwtClaimsSetBuilder.claim("scope", stringJoiner.toString());
        }

        Payload payload = new Payload(jwtClaimsSetBuilder.build().toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        // Kiểm tra chữ ký và hạn sử dụng
        if (!(verified && expiryTime.after(new Date())))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        // Kiểm tra xem token có nằm trong blacklist không (Logout/Revoked)
        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    // API Test gửi mail
    @PostMapping("/email/test")
    public ApiResponse<String> sendTestEmail() {
        // Nội dung HTML test thử
        String htmlContent = "<h1>Chào mừng đến với Money Lover!</h1>" +
                             "<p>Đây là email test định dạng <b>HTML</b>.</p>";
                             
        // Thay email bên dưới bằng email phụ của bạn để kiểm tra
        emailService.sendEmail("email_nhan_test_cua_ban@gmail.com", "Test Email Money Lover", htmlContent);
        
        return ApiResponse.<String>builder()
                .result("Email sent successfully!")
                .build();
    }
}