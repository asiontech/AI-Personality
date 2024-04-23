package com.shure.surdes.survey.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shure.surdes.common.annotation.Log;
import com.shure.surdes.common.core.controller.BaseController;
import com.shure.surdes.common.core.domain.AjaxResult;
import com.shure.surdes.common.core.page.TableDataInfo;
import com.shure.surdes.common.enums.BusinessType;
import com.shure.surdes.common.utils.poi.ExcelUtil;
import com.shure.surdes.survey.domain.AnswerJson;
import com.shure.surdes.survey.service.IAnswerJsonService;
import com.shure.surdes.survey.vo.AiTestVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * 问卷答案结果jsonController
 *
 * @author Shure
 * @date 2021-10-18
 */
@Api(tags = "问卷答案结果json")
@RestController
@RequestMapping("/survey/json")
@Slf4j
@CrossOrigin
public class AnswerJsonController extends BaseController {
    @Autowired
    private IAnswerJsonService answerJsonService;

    /**
     * 查询问卷答案结果json列表
     */
    @ApiOperation(value = "查询问卷答案结果json列表")
//    @PreAuthorize("@ss.hasPermi('survey:json:list')")
    @GetMapping("/list")
    public TableDataInfo list(AnswerJson answerJson) {
        startPage();
        List<AnswerJson> list = answerJsonService.selectAnswerJsonList(answerJson);
        return getDataTable(list);
    }
    
    @ApiOperation(value = "查询用户的最新答案结果")
//  @PreAuthorize("@ss.hasPermi('survey:json:list')")
    @GetMapping("/latest")
    public AjaxResult getLatest(AnswerJson answerJson) {
    	return AjaxResult.success(answerJsonService.selectAnswerJsonLatest(answerJson));
    }

    /**
     * 导出问卷答案结果json列表
     */
    @ApiOperation(value = "导出问卷答案结果json列表")
//    @PreAuthorize("@ss.hasPermi('survey:json:export')")
    @Log(title = "问卷答案结果json", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(AnswerJson answerJson) {
        List<AnswerJson> list = answerJsonService.selectAnswerJsonList(answerJson);
        ExcelUtil<AnswerJson> util = new ExcelUtil<AnswerJson>(AnswerJson.class);
        return util.exportExcel(list, "问卷答案结果json数据");
    }

    /**
     * 获取问卷答案结果json详细信息
     */
    @ApiOperation(value = "获取问卷答案结果json详细信息")
//    @PreAuthorize("@ss.hasPermi('survey:json:query')")
    @GetMapping(value = "/{anId}")
    public AjaxResult getInfo(@PathVariable("anId") Long anId) {
        return AjaxResult.success(answerJsonService.selectAnswerJsonByAnId(anId));
    }

    /**
     * 新增问卷答案结果json
     */
    @ApiOperation(value = "新增问卷答案结果json")
//    @PreAuthorize("@ss.hasPermi('survey:json:add')")
    @Log(title = "问卷答案结果json", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AnswerJson answerJson) {
    	try {
    		String userId = getUserId().toString();
    		log.debug("提交结果，控制层当前登录用户id为：" + userId);
//    		answerJson.setUserId(userId);
     	} catch (Exception e) {
//			return AjaxResult.error("获取用户id失败，请重新登录！");
		}
        return toAjax(answerJsonService.insertAnswerJson(answerJson));
    }
    
    @ApiOperation(value = "用户AI测试")
    @Log(title = "用户AI测试", businessType = BusinessType.INSERT)
    @PostMapping("/ai")
    public AjaxResult aiTest(@RequestBody AiTestVo vo) {
    	int row = answerJsonService.aiTest(vo);
    	if (row == 1) {
    		return AjaxResult.success();
    	} else {
    		return AjaxResult.error("AI测试失败，请手动填写问卷测试！");
    	} 
    		
    }

    /**
     * 修改问卷答案结果json
     */
    @ApiOperation(value = "修改问卷答案结果json")
//    @PreAuthorize("@ss.hasPermi('survey:json:edit')")
    @Log(title = "问卷答案结果json", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AnswerJson answerJson) {
        return toAjax(answerJsonService.updateAnswerJson(answerJson));
    }

    /**
     * 删除问卷答案结果json
     */
    @ApiOperation(value = "删除问卷答案结果json")
//    @PreAuthorize("@ss.hasPermi('survey:json:remove')")
    @Log(title = "问卷答案结果json", businessType = BusinessType.DELETE)
    @DeleteMapping("/{anIds}")
    public AjaxResult remove(@PathVariable Long[] anIds) {
        return toAjax(answerJsonService.deleteAnswerJsonByAnIds(anIds));
    }
}
