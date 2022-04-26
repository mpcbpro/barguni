package com.ssafy.barguni.api.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.barguni.api.common.ResVO;
import com.ssafy.barguni.api.user.vo.KakaoProfile;
import com.ssafy.barguni.api.user.vo.OauthToken;
import com.ssafy.barguni.common.auth.AccountUserDetails;
import com.ssafy.barguni.common.util.JwtTokenUtil;
import com.ssafy.barguni.common.util.KakaoOauthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/")
@Tag(name = "user controller", description = "회원 관련 컨트롤러")
public class UserController {
    private final UserService userService;


    @PostMapping("/login")
    @Operation(summary = "로그인", description = "테스트 로그인한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ResVO<String>> login(@RequestParam String email) {
        ResVO<String> result = new ResVO<>();
        HttpStatus status = null;

        // 로그인 확인
        Boolean duplicated = userService.isDuplicated(email);
        if(!duplicated) {
            result.setMessage("회원이 아닙니다.");
            status = HttpStatus.NOT_ACCEPTABLE;
            return new ResponseEntity<ResVO<String>>(result, status);
        }

        //
        User user = userService.findByEmail(email);
        status = HttpStatus.OK;
        String accessToken = JwtTokenUtil.getToken(user.getId().toString());
        result.setData(accessToken);
        result.setMessage("성공");

        return new ResponseEntity<ResVO<String>>(result, status);
    }

    @GetMapping
    @Operation(summary = "사용자 조회", description = "테스트 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ResVO<String>> find(){
        ResVO<String> result = new ResVO<>();
        HttpStatus status = null;

        try {
            AccountUserDetails userDetails = (AccountUserDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
            User nowUser = userService.findById(userDetails.getUserId());
            status = HttpStatus.OK;
            result.setData(nowUser.getEmail());
            result.setMessage("조회 성공");
        } catch (Exception e) {
            e.printStackTrace();
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            result.setMessage("조회 실패");
        }

        return new ResponseEntity<ResVO<String>>(result, status);
    }


    @PostMapping("/oauth-login/kakao")
    @Operation(summary = "카카오 로그인", description = "카카오 인증 코드로 카카오 토큰을 얻고 정보로 로그인한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ResVO<String>> kakaoLogin (@RequestParam String code) {
        ResVO<String> result = new ResVO<>();
        HttpStatus status = null;

        //------------ 통신 ---------------//
        // 토큰 관련 정보 얻기
        ResponseEntity<String> responseToken = KakaoOauthUtil.getKakaoToken(code);
        if(responseToken == null){
            result.setMessage("유효하지 않은 카카오 인증 코드 입니다.");
            status = HttpStatus.BAD_REQUEST;
            return new ResponseEntity<ResVO<String>>(result, status);
        }

        // 토큰 정보 추출
        ObjectMapper objectMapper = new ObjectMapper();
        OauthToken oauthToken = null;
        try {
            oauthToken = objectMapper.readValue(responseToken.getBody(), OauthToken.class);
        } catch (JsonProcessingException e) { // 파싱 에러
            e.printStackTrace();
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            result.setMessage("서버 오류");
            return new ResponseEntity<ResVO<String>>(result, status);
        }


        //------------ 통신 ---------------//
        // 토큰으로 프로필 정보 가져오기
        ResponseEntity<String> responseProfile = KakaoOauthUtil.getKakaoProfile(oauthToken);
        if(responseProfile == null){
            result.setMessage("유효하지 않은 토큰 입니다.");
            status = HttpStatus.BAD_REQUEST;
            return new ResponseEntity<ResVO<String>>(result, status);
        }

        // 프로필 정보 추출
        KakaoProfile kakaoProfile = null;
        try {
            kakaoProfile = objectMapper.readValue(responseProfile.getBody(), KakaoProfile.class);
        } catch (JsonProcessingException e) { // 파싱 에러
            e.printStackTrace();
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            result.setMessage("서버 오류");
            return new ResponseEntity<ResVO<String>>(result, status);
        }



        // 이메일 중복 확인 및 토큰 반환
        String email = kakaoProfile.getKakao_account().getEmail();
        String nickname = kakaoProfile.getProperties().getNickname();
        Boolean duplicated = userService.isDuplicated(email);
        User user = null;
        if(!duplicated) {
            user = userService.oauthSignup(email, nickname);
//            System.out.println("소셜 회원가입");
        }
        else{
            user = userService.findByEmail(email);
//            System.out.println("소셜 로그인");
        }

        status = HttpStatus.OK;
        String accessToken = JwtTokenUtil.getToken(user.getId().toString());
        result.setData(accessToken);
        result.setMessage("카카오 로그인 성공");

        return new ResponseEntity<ResVO<String>>(result, status);
    }
}