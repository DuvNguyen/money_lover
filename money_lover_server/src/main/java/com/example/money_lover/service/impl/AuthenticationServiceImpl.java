package com.example.money_lover.service.impl;

import com.example.money_lover.dto.request.AuthenticationRequest;
import com.example.money_lover.dto.response.AuthenticationResponse;
import com.example.money_lover.entity.User;
import com.example.money_lover.exception.AppException;
import com.example.money_lover.exception.ErrorCode;
import com.example.money_lover.repository.UserRepository;
import com.example.money_lover.service.IAuthenticationService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.StringJoiner; // Serve roles
import org.springframework.util.CollectionUtils; // Serve roles

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements IAuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Đọc Key bí mật từ application.properties
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    // Đọc thời gian hết hạn (giây) từ application.properties
    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // 1. Kiểm tra Email có tồn tại không
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // 2. Kiểm tra Mật khẩu (So sánh password raw vs password hash trong DB)
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // 3. Nếu đúng hết -> Sinh Token
        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet.Builder jwtClaimsSetBuilder = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("money_lover.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .claim("email", user.getEmail()); 
        // Logic: Lấy list roles -> Nối thành chuỗi cách nhau bởi dấu cách (VD: "ADMIN, USER")
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            StringJoiner stringJoiner = new StringJoiner(" ");
            user.getRoles().forEach(role -> stringJoiner.add(role.getName()));

            // Lưu vào claim tên là "scope" (Chuẩn mặc định của Oauth2/Spring Security)
            jwtClaimsSetBuilder.claim("scope", stringJoiner.toString());
        }
        // -----------------------------

        Payload payload = new Payload(jwtClaimsSetBuilder.build().toJSONObject()); // Nhớ dùng .build()
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }
}