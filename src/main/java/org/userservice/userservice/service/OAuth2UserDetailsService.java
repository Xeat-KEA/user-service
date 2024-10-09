package org.userservice.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.userservice.userservice.domain.User;
import org.userservice.userservice.dto.OAuth2UserDetails;
import org.userservice.userservice.dto.NaverResponse;
import org.userservice.userservice.dto.OAuth2Response;
import org.userservice.userservice.dto.UserDto;
import org.userservice.userservice.repository.UserRepository;

/**
 * 클래스 요약:
 * OAuth2 리소스 서버에서 받은 사용자 정보(OAuth2UserRequest)를 바탕으로, 사용자 정보를 로드하고 가공하는 단계
 * <p>
 * 설명:
 * 인증이 완료되면 return 되는 OAuth2UserDetails 객체는 OAuth2UserDetails 객체는 Authentication 객체로 래핑되어 인증완료
 * Authentication 객체는 SecurityContextHolder(인증 객체를 저장하는 컨테이너) 에 저장
 * 이 후 customSuccessHandler 에서는 SecurityContext 에 저장된 인증 객체를 가져와 추가 작업(JWT 생성)을 수행가능
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserDetailsService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("리소스 서버로부터 인증된 유저는? = {}", oAuth2User);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("google")) {
            //oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        //리소스 서버에서 발급 받은 정보로 사용자를 특정할 아이디값을 만듬, ex) naver_12345
        String providerName = oAuth2Response.getProvider() + "_" + oAuth2Response.getProviderId();
        String role = "ROLE_USER_A"; //소셜로그인만 진행하였을 경우 모두 회원가입해야한다.
        User user = userRepository.findById(providerName).orElse(null);

        if (user == null) {
            userRepository.save(User.builder()
                    .userId(providerName)
                    .name(oAuth2Response.getName())
                    .email(oAuth2Response.getEmail())
                    .role(role)
                    .build());

            UserDto userDto = UserDto.builder()
                    .providerName(providerName)
                    .name(oAuth2Response.getName())
                    .role(role)
                    .build();
            return new OAuth2UserDetails(userDto);
        } else {
            userRepository.save(user.toBuilder()
                    .name(oAuth2Response.getName())
                    .email(oAuth2Response.getEmail())
                    .build());

            UserDto userDto = UserDto.builder()
                    .providerName(user.getUserId())
                    .name(oAuth2Response.getName())
                    .role(user.getRole())
                    .build();
            return new OAuth2UserDetails(userDto);
        }
    }
}