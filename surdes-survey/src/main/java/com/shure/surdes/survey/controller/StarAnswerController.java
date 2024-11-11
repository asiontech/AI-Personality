package com.shure.surdes.survey.controller;

import com.alibaba.fastjson.JSONObject;
import com.shure.surdes.common.constant.HttpStatus;
import com.shure.surdes.common.core.controller.BaseController;
import com.shure.surdes.common.core.domain.AjaxResult;
import com.shure.surdes.common.utils.SecurityUtils;
import com.shure.surdes.survey.domain.StarAnswer;
import com.shure.surdes.survey.service.IStarAnswerService;
import com.shure.surdes.survey.util.FileUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Date;

/**
 * 明星模块
 *
 * @author lixiyang
 * @date 2024-10-30
 */
@Validated
@Slf4j
@RestController
@RequestMapping("/survey/staranswer")
@Api(tags = "明星api")
public class StarAnswerController extends BaseController {

    @Autowired
    private IStarAnswerService starAnswerService;

    @Value(value = "${star.imgUrl}")
    private String startImgUrl;

    /**
     * 分页查询明星列表
     */
    @GetMapping("/list")
    @ApiOperation(value = "分页查询明星列表", notes = "分页查询明星列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页码", dataType = "Integer", dataTypeClass = Integer.class,
                    required = true),
            @ApiImplicitParam(name = "pageSize", value = "分页量", dataType = "Integer", dataTypeClass = Integer.class,
                    required = true)
    })
    public AjaxResult queryStarAnswerByPage(@NotNull Integer pageNum, @NotNull Integer pageSize) {
        JSONObject json = starAnswerService.queryStarAnswerByPage(pageNum, pageSize);
        return AjaxResult.success(json);
    }

    @GetMapping("/treeselect")
    @ApiOperation(value = "查询明星选择栏", notes = "查询明星选择栏")
    public AjaxResult selectAllTree() {
        return AjaxResult.success(starAnswerService.selectAllTree());
    }


    /**
     * 新增明星
     */
    @PostMapping
    @ApiOperation(value = "新增明星", notes = "新增明星")
    @ApiImplicitParam(name = "starAnswer", value = "starAnswer对象", dataType = "StarAnswer", dataTypeClass =
            StarAnswer.class)
    public AjaxResult insertStarAnswer(@Validated StarAnswer starAnswer,
                                       @RequestParam(value = "starImgFile")MultipartFile multipartFile) {
        if (multipartFile!=null){
            AjaxResult ajaxResult=FileUtil.uploadImage(multipartFile, startImgUrl);
            if(HttpStatus.SUCCESS==Integer.parseInt(ajaxResult.get(AjaxResult.CODE_TAG).toString())){
                starAnswer.setStarImg(ajaxResult.get(AjaxResult.MSG_TAG).toString());
            }else{
                return ajaxResult;
            }
        }else {
            starAnswer.setStarImg(null);
        }
        starAnswer.setStarAnswerId(null);
        starAnswer.setUpdateBy(SecurityUtils.getUsername());
        starAnswer.setUpdateTime(new Date());
        boolean flag = starAnswerService.save(starAnswer);
        return toAjax(flag);
    }

    /**
     * 修改明星
     */
    @PutMapping
    @ApiOperation(value = "修改明星", notes = "修改明星")
    @ApiImplicitParam(name = "starAnswer", value = "明星对象", dataType = "StarAnswer", dataTypeClass =
            StarAnswer.class)
    public AjaxResult updateStarAnswer(@Validated  StarAnswer starAnswer,
                                       @RequestParam(value = "starImgFile")MultipartFile multipartFile) {
        if (starAnswer.getStarId() == null) {
            return AjaxResult.error("明星id缺失");
        }

        if (multipartFile!=null){
            AjaxResult ajaxResult=FileUtil.uploadImage(multipartFile, startImgUrl);
            if(HttpStatus.SUCCESS==Integer.parseInt(ajaxResult.get(AjaxResult.CODE_TAG).toString())){
                starAnswer.setStarImg(ajaxResult.get(AjaxResult.MSG_TAG).toString());
            }else{
                return ajaxResult;
            }
        }else {
            starAnswer.setStarImg(null);
        }
        starAnswer.setUpdateBy(SecurityUtils.getUsername());
        starAnswer.setUpdateTime(new Date());
        boolean flag = starAnswerService.updateById(starAnswer);
        return toAjax(flag);
    }

    /**
     * 批量删除明星
     */
    @DeleteMapping("/{starIds}")
    @ApiOperation(value = "批量删除明星", notes = "批量删除明星")
    @ApiImplicitParam(name = "starIds", value = "明星id集合", dataType = "Long", dataTypeClass = Long.class)
    public AjaxResult deleteBatchStarAnswer(@NotEmpty @PathVariable Long[] starIds) {
        boolean flag = starAnswerService.removeByIds(Arrays.asList(starIds));
        return toAjax(flag);
    }

}
