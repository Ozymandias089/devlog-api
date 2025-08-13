package com.ozymandias089.devlog_api.member.service;

import com.ozymandias089.devlog_api.member.PasswordValidationResult;
import com.ozymandias089.devlog_api.member.dto.response.PasswordResetResponseDTO;
import com.ozymandias089.devlog_api.member.jwt.JwtTokenProvider;
import com.ozymandias089.devlog_api.global.enums.Role;
import com.ozymandias089.devlog_api.global.exception.DuplicateEmailExcpetion;
import com.ozymandias089.devlog_api.global.exception.InvalidCredentialsException;
import com.ozymandias089.devlog_api.global.exception.JwtValidationException;
import com.ozymandias089.devlog_api.member.provider.MemberMapper;
import com.ozymandias089.devlog_api.member.dto.request.LoginRequestDTO;
import com.ozymandias089.devlog_api.member.dto.request.PasswordResetConfirmRequestDTO;
import com.ozymandias089.devlog_api.member.dto.request.SignupRequestDTO;
import com.ozymandias089.devlog_api.member.dto.response.LoginResponseDTO;
import com.ozymandias089.devlog_api.member.dto.response.PasswordValidationResponseDTO;
import com.ozymandias089.devlog_api.member.dto.response.SignupResponseDTO;
import com.ozymandias089.devlog_api.member.entity.MemberEntity;
import com.ozymandias089.devlog_api.member.provider.MemberProvider;
import com.ozymandias089.devlog_api.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.ozymandias089.devlog_api.global.util.RegexPatterns.EMAIL_REGEX;
import static com.ozymandias089.devlog_api.global.util.RegexPatterns.USERNAME_REGEX;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository repository;
    private final MemberMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final MemberProvider memberProvider;
    @Value("${app.frontend.password-reset-url}")
    private String passwordResetUrl;

    /**
     * Handles the full member signup process.
     * <p>
     * This includes:
     * <ul>
     *   <li>Duplicate email check</li>
     *   <li>Password encoding</li>
     *   <li>Random username generation</li>
     *   <li>Role assignment (default: ROLE_USER)</li>
     *   <li>Member entity creation and persistence</li>
     *   <li>JWT Access/Refresh token creation</li>
     * </ul>
     *
     * @param requestDTO DTO containing signup information (email, password, etc.)
     * @return DTO containing registered member's basic info and issued tokens
     * @throws DuplicateEmailExcpetion if the given email already exists in the system
     */
    @Transactional
    public SignupResponseDTO signUp(SignupRequestDTO requestDTO) {
        if (memberProvider.isEmailValidAndUnique(requestDTO.getEmail())) {
            throw new DuplicateEmailExcpetion(requestDTO.getEmail());
        }

        // Encode password
        String encodedPassword = memberProvider.hashPassword(requestDTO.getPassword());

        // Random username creation
        String username = memberProvider.generateUsername();

        Role defaultRole = Role.ROLE_USER;
        log.info("Default role {} given to username {}", defaultRole, username);

        // Create / save Member Entity
        MemberEntity member = mapper.toMemberEntity(requestDTO, encodedPassword, username, defaultRole);
        MemberEntity saved = repository.save(member);
        log.info("User information saved to entity.");

        // Create JWT AnR Tokens
        String accessToken = jwtTokenProvider.generateAccessToken(saved.getUuid().toString(), defaultRole);
        String refreshToken = jwtTokenProvider.generateRefreshToken(saved.getUuid().toString());

        return mapper.toSignupResponseDTO(saved.getUuid(), saved.getEmail(), saved.getUsername(), accessToken, refreshToken);
    }

    /**
     * Validates a password against predefined complexity rules.
     * <p>
     * Rules:
     * <ul>
     *     <li>Must not be null or blank</li>
     *     <li>Must be at least 8 characters long</li>
     *     <li>Must contain at least one uppercase letter (A-Z)</li>
     *     <li>Must contain at least one lowercase letter (a-z)</li>
     *     <li>Must contain at least one digit (0-9)</li>
     *     <li>Must contain at least one special character (!@#$%^&*())</li>
     * </ul>
     *
     * @param password the password to validate
     * @return a {@link PasswordValidationResponseDTO} containing validation status and error messages
     */
    public PasswordValidationResponseDTO validatePassword(String password) {
        PasswordValidationResult result = memberProvider.passwordValidator(password);
        return new PasswordValidationResponseDTO(result.validity(), result.errors());
    }

    /**
     * Performs the login process by validating user credentials.
     *
     * This method:
     * <ul>
     *   <li>Fetches the member entity by email.</li>
     *   <li>Verifies the provided password against the stored hashed password.</li>
     *   <li>Generates JWT access and refresh tokens upon successful authentication.</li>
     * </ul>
     *
     * @param requestDTO The login request data transfer object containing email and password.
     * @return A {@link LoginResponseDTO} containing the generated access and refresh tokens.
     * @throws InvalidCredentialsException if the email does not exist or the password is incorrect.
     */
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO requestDTO){
        MemberEntity member = repository.findByEmail(requestDTO.getEmail()).orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));

        if (!passwordEncoder.matches(requestDTO.getPassword(), member.getPassword())) {
            log.warn("Invalid password for user: {}", member.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Create JWT AnR Tokens
        String accessToken = jwtTokenProvider.generateAccessToken(member.getUuid().toString(), member.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(member.getUuid().toString());

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 로그아웃 처리를 수행합니다.
     * <p>
     * - Authorization 헤더에서 액세스 토큰을 추출하고 유효성을 검사합니다.
     * - 해당 UUID의 리프레시 토큰을 삭제합니다.
     * - 액세스 토큰을 블랙리스트에 등록하여 즉시 만료 처리합니다.
     *
     * @param uuid 사용자의 고유 식별자(UUID 문자열)
     * @param authorizationHeader HTTP 요청 헤더에서 받은 Authorization 값 (Bearer 토큰 형식)
     * @throws IllegalArgumentException Authorization 헤더가 없거나 형식이 올바르지 않을 경우 발생
     */
    public void logout(String uuid, String authorizationHeader) {
        // 1. Validate Header
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        } else {
            // 토큰 없거나 잘못된 형식일 때 처리 (예외 던지거나 로그 남기기)
            throw new IllegalArgumentException("Invalid Authorization header");
        }
        // 2. Delete Refresh Token
        jwtTokenProvider.deleteRefreshToken(uuid);
        // 3. Blacklist Access Token
        jwtTokenProvider.blacklistAccessToken(token);
    }

    /**
     * 회원 탈퇴를 수행합니다.
     * <p>
     * - UUID로 회원을 조회하고, 입력받은 원문 비밀번호와 저장된 비밀번호를 비교하여 검증합니다.
     * - 비밀번호가 일치하지 않으면 인증 예외를 발생시킵니다.
     * - 회원 정보를 삭제하고, 관련된 리프레시 토큰을 삭제합니다.
     * - 액세스 토큰을 블랙리스트에 등록하여 즉시 만료 처리합니다.
     *
     * @param uuid 회원의 고유 식별자(UUID 문자열)
     * @param rawPassword 회원 탈퇴를 위한 본인 확인용 비밀번호 (원문)
     * @throws RuntimeException 회원을 찾을 수 없을 경우 발생
     * @throws InvalidCredentialsException 비밀번호가 일치하지 않을 경우 발생
     */
    @Transactional
    public void deleteMember(String uuid, String rawPassword){
        MemberEntity member = repository.findByUuid(UUID.fromString(uuid)).orElseThrow(() -> new RuntimeException("No Such member found with the provided token"));
        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        repository.delete(member);

        // 관련 토큰 삭제 (리프레시 토큰, 블랙리스트 등록)
        jwtTokenProvider.deleteRefreshToken(uuid);  // 리프레시 토큰 삭제
        jwtTokenProvider.blacklistAccessToken(uuid);
    }

    /**
     * 이메일 형식과 중복 여부를 검증
     *
     * @param email 입력된 이메일
     * @return true: 사용 가능, false: 사용 불가
     */
    public boolean isEmailValidAndAvailable(String email) {
        if (email == null || email.isBlank()) {
            log.warn("Email is either null or blank");
            return false;
        }

        // 이메일 형식 검증
        if (!EMAIL_REGEX.matcher(email).matches()) {
            log.warn("Invalid email format: {}", email);
            return false;
        }

        if (memberProvider.isEmailValidAndUnique(email)) return false;

        log.info("Email {} is valid and available", email);
        return true;
    }

    /**
     * Updates the username of a member identified by their UUID.
     * <p>
     * This method validates the provided new username against a predefined format
     * using {@code USERNAME_REGEX}, ensures it is not blank or identical to the current one,
     * and then updates it in the database.
     * </p>
     *
     * @param uuid        The unique UUID of the member as a string.
     * @param newUsername The new username to set for the member.
     * @throws IllegalArgumentException        if the username is null, blank, or does not match the regex format.
     * @throws InvalidCredentialsException     if no account exists for the given UUID.
     */
    @Transactional
    public void updateUsername(String uuid, String newUsername) {
        if (newUsername == null || newUsername.isBlank() || !USERNAME_REGEX.matcher(newUsername).matches())
            throw new IllegalArgumentException("Invalid Username format");

        MemberEntity member = repository.findByUuid(UUID.fromString(uuid)).orElseThrow(() -> new InvalidCredentialsException("No Account found with the provided UUID"));

        if (newUsername.equals(member.getUsername())) return;

        member.updateUsername(newUsername);
        repository.save(member);
    }

    /**
     * 비밀번호 재설정 요청을 처리한다.
     * - 이메일 존재 여부 확인
     * - 재설정 토큰 생성
     * - 재설정 링크 포함 이메일 발송
     *
     * @param email 비밀번호 재설정을 요청한 이메일 주소
     * @throws IllegalArgumentException 이메일이 등록되어 있지 않은 경우
     */
    public void requestPasswordReset(String email) {
        MemberEntity member = repository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("No Account found with the provided email"));
        String resetToken = jwtTokenProvider.generatePasswordResetToken(member.getUuid().toString());
        String resetURL = passwordResetUrl + "?token=" + resetToken;
        emailService.sendPasswordResetEmail(email, resetURL);
    }

    /**
     * Validates the given password reset token by checking its signature, expiration,
     * and whether it is stored in Redis.
     *
     * @param token the JWT password reset token to validate
     * @return true if the token is valid and currently stored; false otherwise
     */
    public boolean isPasswordResetTokenValid(String token) {
        if (!jwtTokenProvider.isPasswordResetTokenValid(token)) return false;

        Claims claims = jwtTokenProvider.parseClaims(token);
        String uuid = claims.getSubject();

        return jwtTokenProvider.isPasswordResetTokenStored(uuid, token);
    }

    /**
     * Resets the member's password based on a valid password reset token and new password.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Validates and parses the JWT token from the request DTO.</li>
     *     <li>Verifies that the token is of type "password_reset".</li>
     *     <li>Extracts the member UUID from the token's subject.</li>
     *     <li>Retrieves the member entity and updates the password with an encrypted value.</li>
     *     <li>Saves the updated member entity to the repository.</li>
     *     <li>Deletes any existing refresh tokens associated with the member to invalidate sessions.</li>
     * </ul>
     *
     * @param requestDTO the DTO containing the reset token and new password
     * @throws JwtValidationException if the token type is invalid
     * @throws IllegalArgumentException if the member cannot be found by UUID
     */
    @Transactional
    public void resetPassword(PasswordResetConfirmRequestDTO requestDTO) {
        // 1. Validate Tokens and parse claims
        Claims claims = jwtTokenProvider.parseClaims(requestDTO.getResetToken());

        // 2. Check Token types
        String type = claims.get("type", String.class);
        if (!"password_reset".equals(type)) throw new JwtValidationException("Invalid Token Type");

        // 3. Extract UUID
        UUID uuid = UUID.fromString(claims.getSubject());

        // 4. Check Members
        MemberEntity member = repository.findByUuid(uuid).orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // 5. Check password validity
        if (!memberProvider.passwordValidator(requestDTO.getNewPassword()).validity()) throw new IllegalArgumentException("Invalid Password format");

        // 6. Encrypt and save password
        String encryptedPassword = memberProvider.hashPassword(requestDTO.getNewPassword());
        member.updatePassword(encryptedPassword);
        repository.save(member);

        // 7. Delete existing refreshToken to invalidate session
        jwtTokenProvider.deleteRefreshToken(uuid.toString());

        log.info("Password reset successful for UUID: {}.", uuid);
    }

    /**
     * Issues a short-lived, one-time-use password reset token for an authenticated user.
     * <p>
     * This method is intended for use when a user is logged in but wishes to change their
     * password. It performs the following steps:
     * <ol>
     *   <li>Retrieves the member record by UUID.</li>
     *   <li>Validates that the provided current password matches the stored password hash.</li>
     *   <li>Generates a password reset token with limited validity, suitable for immediate use.</li>
     *   <li>Wraps the token in a {@link PasswordResetResponseDTO} for return to the caller.</li>
     * </ol>
     * The generated reset token can then be used in the standard password reset confirmation
     * endpoint to set a new password.
     * </p>
     *
     * @param uuid the UUID of the currently authenticated user, as a {@link String}
     * @param currentPassword the user's current plain-text password for verification
     * @return a {@link PasswordResetResponseDTO} containing the newly generated reset token
     * @throws InvalidCredentialsException if no member with the given UUID exists or if the current password does not match
     */
    public PasswordResetResponseDTO issueResetToken(String uuid, String currentPassword) {
        MemberEntity member = repository.findByUuid(UUID.fromString(uuid)).orElseThrow(() -> new InvalidCredentialsException("No member found with the provided token"));
        if(!passwordEncoder.matches(currentPassword, member.getPassword())) throw new InvalidCredentialsException("The current Password doesn't match");

        String resetToken = jwtTokenProvider.generatePasswordResetToken(uuid);
        return PasswordResetResponseDTO.builder()
                .resetToken(resetToken)
                .build();
    }
}
