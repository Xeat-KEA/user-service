package org.userservice.userservice.dto.codebankclient;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserScoreRequest {

    @Schema(description = "유저 ID", example = "1")
    private String userId;

    @Schema(description = "획득한 점수", example = "10")
    private int score;
}