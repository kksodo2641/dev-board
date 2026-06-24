package com.minseok.devboard.board.dto.request;

import com.minseok.devboard.board.dto.response.UpdateBoardResponse;
import com.minseok.devboard.board.entity.BoardCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBoardRequest {
    
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 최대 100자까지 허용됩니다.")
    private String title;
    
    @NotBlank(message = "내용을 입력해주세요.")
    private String content;
    
    @NotNull(message = "카테고리를 선택해주세요.")
    private BoardCategory category;
    
    public static UpdateBoardRequest from(final UpdateBoardResponse response) {
        assert (response != null);
        
        return new UpdateBoardRequest(response.getTitle(),
                                      response.getContent(),
                                      response.getCategory());
    }
    
}
