package com.ssafy.barguni.api.basket.controller;

import com.ssafy.barguni.api.basket.entity.Categories;
import com.ssafy.barguni.api.basket.service.CategoryService;
import com.ssafy.barguni.api.basket.vo.CategoryRes;
import com.ssafy.barguni.api.common.ResVO;
import com.ssafy.barguni.api.error.ErrorCode;
import com.ssafy.barguni.api.error.ErrorResVO;
import com.ssafy.barguni.api.error.Exception.CategoryException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/basket/category")
@Tag(name = "category controller", description = "카테고리 관련 컨트롤러")
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("/{basketId}")
    @Operation(summary = "카테고리 등록", description = "해당 바구니에 카테고리를 등록한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ResVO<Long>> registerCatetory(@PathVariable Long basketId, @RequestParam String name){
        ResVO<Long> result = new ResVO<>();
        HttpStatus status = null;


        if(categoryService.isDuplicated(basketId, name))
            throw new CategoryException(new ErrorResVO(ErrorCode.CATEGOTY_DUPLICATED));

        Long categoryId = categoryService.register(basketId, name);
        result.setData(categoryId);
        result.setMessage("카테고리 등록 성공");
        status = HttpStatus.OK;

        return new ResponseEntity<ResVO<Long>>(result, status);

    }

    @GetMapping("/{basketId}")
    @Operation(summary = "바구니 내 카테고리 목록 조회", description = "바구니 내 카테고리 목록을 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ResVO<List<CategoryRes>>> getCategoryList(@PathVariable Long basketId){
        ResVO<List<CategoryRes>> result = new ResVO<>();
        HttpStatus status = null;

        List<Categories> categories = categoryService.getByBasketId(basketId);
        result.setData(categories
                .stream()
                .map(CategoryRes::new)
                .collect(Collectors.toList()));
        result.setMessage("카테고리 조회 성공");
        status = HttpStatus.OK;

        return new ResponseEntity<ResVO<List<CategoryRes>>>(result, status);
    }

    @PutMapping("/{basketId}/{categoryId}")
    @Operation(summary = "카테고리 수정", description = "해당 바구니에 카테고리를 수정한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ResVO<Long>> modifyCatetory(@PathVariable Long basketId, @PathVariable Long categoryId, @RequestParam String name){
        ResVO<Long> result = new ResVO<>();
        HttpStatus status = null;

        categoryService.modify(categoryId, name);
        result.setData(categoryId);
        result.setMessage("카테고리 수정 성공");
        status = HttpStatus.OK;

        return new ResponseEntity<ResVO<Long>>(result, status);
    }

    @DeleteMapping("/{basketId}/{categoryId}")
    @Operation(summary = "카테고리 삭제", description = "해당 카테고리를 삭제한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ResVO<Boolean>> deleteCategory(@PathVariable Long basketId, @PathVariable Long categoryId){
        ResVO<Boolean> result = new ResVO<>();
        HttpStatus status = null;

        Categories category = categoryService.getById(categoryId);
        if(category == null)
            throw new CategoryException(new ErrorResVO(ErrorCode.CATEGORY_NOT_FOUNDED));

        // 기본 카테고리 삭제 요청인 경우
        if("기본".equals(category.getName()))
            throw new CategoryException(new ErrorResVO(ErrorCode.CATEGORY_DEFAULT_NOT_DELETED));

        categoryService.delete(categoryId);
        result.setMessage("카테고리 삭제 성공");
        result.setData(true);
        status = HttpStatus.OK;

        return new ResponseEntity<ResVO<Boolean>>(result, status);
    }
}
