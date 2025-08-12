package com.ozymandias089.devlog_api.member.service;

import com.ozymandias089.devlog_api.auth.jwt.JwtTokenProvider;
import com.ozymandias089.devlog_api.global.enums.Role;
import com.ozymandias089.devlog_api.global.exception.DuplicateEmailExcpetion;
import com.ozymandias089.devlog_api.global.exception.InvalidCredentialsException;
import com.ozymandias089.devlog_api.member.MemberMapper;
import com.ozymandias089.devlog_api.member.dto.request.LoginRequestDTO;
import com.ozymandias089.devlog_api.member.dto.request.SignupRequestDTO;
import com.ozymandias089.devlog_api.member.dto.response.LoginResponseDTO;
import com.ozymandias089.devlog_api.member.dto.response.SignupResponseDTO;
import com.ozymandias089.devlog_api.member.entity.Member;
import com.ozymandias089.devlog_api.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.ozymandias089.devlog_api.global.util.RegexPatterns.EMAIL_REGEX;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository repository;
    private final MemberMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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

    private String generateUsername() {
        String username;
        do {
            int random = (int) (Math.random() * 1_000_000);
            username = String.format("User-%06d", random);
        } while (repository.findByUsername(username).isPresent());
        return username;
    }

    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

}
