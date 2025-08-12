package com.ozymandias089.devlog_api.member.service;

import com.ozymandias089.devlog_api.auth.jwt.JwtTokenProvider;
import com.ozymandias089.devlog_api.global.enums.Role;
import com.ozymandias089.devlog_api.global.exception.DuplicateEmailExcpetion;
import com.ozymandias089.devlog_api.global.exception.InvalidCredentialsException;
import com.ozymandias089.devlog_api.global.exception.JwtValidationException;
import com.ozymandias089.devlog_api.member.MemberMapper;
import com.ozymandias089.devlog_api.member.dto.request.LoginRequestDTO;
import com.ozymandias089.devlog_api.member.dto.request.PasswordResetConfirmRequestDTO;
import com.ozymandias089.devlog_api.member.dto.request.SignupRequestDTO;
import com.ozymandias089.devlog_api.member.dto.response.LoginResponseDTO;
import com.ozymandias089.devlog_api.member.dto.response.SignupResponseDTO;
import com.ozymandias089.devlog_api.member.entity.Member;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository repository;
    private final MemberMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
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
        if (isEmailAlreadyExists(requestDTO.getEmail())) {
            throw new DuplicateEmailExcpetion(requestDTO.getEmail());
        }

        // Encode password
        String encodedPassword = hashPassword(requestDTO.getPassword());

        // Random username creation
        String username = generateUsername();

        Role defaultRole = Role.ROLE_USER;
        log.info("Default role {} given to username {}", defaultRole, username);

        // Create / save Member Entity
        Member member = mapper.toMemberEntity(requestDTO, encodedPassword, username, defaultRole);
        Member saved = repository.save(member);
        log.info("User information saved to entity.");

        // Create JWT AnR Tokens
        String accessToken = jwtTokenProvider.generateAccessToken(saved.getUuid().toString(), defaultRole);
        String refreshToken = jwtTokenProvider.generateRefreshToken(saved.getUuid().toString());

        return mapper.toSignupResponseDTO(saved.getUuid(), saved.getEmail(), saved.getUsername(), accessToken, refreshToken);
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
        Member member = repository.findByEmail(requestDTO.getEmail()).orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));

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

        if (isEmailAlreadyExists(email)) return false;

        log.info("Email {} is valid and available", email);
        return true;
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
        Member member = repository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("No Account found with the provided email"));
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
        Member member = repository.findByUuid(uuid).orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // 5. Encrypt and save password
        String encryptedPassword = hashPassword(requestDTO.getNewPassword());
        member.updatePassword(encryptedPassword);
        repository.save(member);

        // 6. Delete existing refreshToken to invalidate session
        jwtTokenProvider.deleteRefreshToken(uuid.toString());

        log.info("Password reset successful for UUID: {}.", uuid);
    }

    /**
     * Checks if the given email is valid in format and not already registered.
     *
     * @param email The email address to check
     * @return true if the email format is valid and not already in use; false otherwise
     */
    private boolean isEmailAlreadyExists(String email) {
        // DB에서 중복 확인
        boolean exists = repository.findByEmail(email).isPresent();
        if (exists) {
            log.info("Email {} already exists", email);
            return true;
        }
        return false;
    }

    /**
     * Generates a unique username in the format "User-xxxxxx", where 'xxxxxx' is a zero-padded
     * random 6-digit number. The method ensures that the generated username does not already
     * exist in the repository.
     *
     * @return a unique username string
     */
    private String generateUsername() {
        String username;
        do {
            int random = (int) (Math.random() * 1_000_000);
            username = String.format("User-%06d", random);
        } while (repository.findByUsername(username).isPresent());
        return username;
    }

    /**
     * Hashes the given plain text password using the configured PasswordEncoder.
     *
     * @param password the plain text password to be hashed
     * @return the encoded (hashed) password string
     */
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

}
