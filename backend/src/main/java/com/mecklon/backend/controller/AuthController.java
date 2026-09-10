package com.mecklon.backend.controller;

import com.mecklon.backend.configuration.CustomUserDetails;
import com.mecklon.backend.configuration.CustomUserDetailsService;
import com.mecklon.backend.configuration.JwtUtil;
import com.mecklon.backend.dtos.AuthRequest;
import com.mecklon.backend.dtos.AuthResponse;
import com.mecklon.backend.dtos.SignupRequest;
import com.mecklon.backend.exception.UserAlreadyExistsException;
import com.mecklon.backend.model.User;
import com.mecklon.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {

        System.out.println(request.getEmail());
        System.out.println(request.getPassword());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService
                .loadUserByUsername(request.getEmail());

        String token = jwtUtil.generateToken(userDetails);
        User user = userRepository.findByEmail(userDetails.getUsername());
        return new AuthResponse(user.getId(),token, userDetails.getUsername(), userDetails.getDisplayUsername());
    }


    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request){
        String token = jwtUtil.generateToken(request.getEmail());

        if(request.getUsername()==null || request.getUsername().isEmpty() ||
                request.getEmail()==null || request.getEmail().isEmpty() ||
                request.getPassword()==null || request.getPassword().isEmpty()){
            throw new BadCredentialsException("email or password not found");
        }
        User user = userRepository.findByEmail(request.getEmail());
        if(user!=null){
            throw new UserAlreadyExistsException("User with this email already exists");
        }
        user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername())
                .build();
        userRepository.save(user);


        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(user.getId(),token, request.getEmail(), request.getUsername()));
    }

}
