package com.shure.surdes.survey.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shure.surdes.common.annotation.Log;
import com.shure.surdes.common.core.controller.BaseController;
import com.shure.surdes.common.core.domain.AjaxResult;
import com.shure.surdes.common.core.page.TableDataInfo;
import com.shure.surdes.common.core.redis.RedisCache;
import com.shure.surdes.common.enums.BusinessType;
import com.shure.surdes.common.utils.poi.ExcelUtil;
import com.shure.surdes.survey.domain.AnswerJson;
import com.shure.surdes.survey.domain.MbtiMatch;
import com.shure.surdes.survey.domain.UserSurveyResult;
import com.shure.surdes.survey.mapper.MbtiMatchMapper;
import com.shure.surdes.survey.mapper.UserSurveyResultMapper;
import com.shure.surdes.survey.pay.zfb.AliPayDTO;
import com.shure.surdes.survey.service.IAnswerJsonService;
import com.shure.surdes.survey.vo.AiTestVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    MbtiMatchMapper mbtiMatchMapper;

    @Autowired
    private UserSurveyResultMapper userSurveyResultMapper;

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

    @ApiOperation(value = "查询用户与明星最新结果")
    @PostMapping("/pk/latest")
    public AjaxResult getUserAndStarLatest(@RequestBody AnswerJson answerJson) {
        return AjaxResult.success(answerJsonService.selectAnswerJsonLatest(answerJson));
    }

    /**
     * 查询mbti的详细解说以及性格相同，匹配的人
     *
     * @param mbti
     * @return
     */
    @ApiOperation(value = "查询mbti解析数据，以及性格相同，匹配的人")
    @GetMapping("/mbti/desc")
    public AjaxResult getMbtiDesc(String mbti, String userId) {
        return AjaxResult.success(answerJsonService.getMbtiDesc(mbti, userId));
    }

    @ApiOperation(value = "查询用户的disc状态")
    @GetMapping("/disc")
    public AjaxResult getUserDisc(String userId) {
        try {
            Long id = Long.valueOf(userId);
            return AjaxResult.success(answerJsonService.getUserDisc(id));
        } catch (Exception e) {
            JSONObject json = new JSONObject();
            json.put("payStatus", "");
            json.put("testStaus", "");
            return AjaxResult.success(json);
        }
    }

    @ApiOperation(value = "查询性格匹配用户")
    @GetMapping("/user/match")
    public AjaxResult getMatchUser(String mbti, Integer pageNum, Integer pageSize) {
        return AjaxResult.success(answerJsonService.getMatchUser(mbti, pageNum, pageSize));
    }

    @ApiOperation(value = "查询性格相同用户")
    @GetMapping("/user/same")
    public AjaxResult getSameUser(String mbti, Integer pageNum, Integer pageSize) {
        return AjaxResult.success(answerJsonService.getSameUser(mbti, pageNum, pageSize));
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
        JSONObject result = answerJsonService.insertAnswerJson(answerJson);
        if (null != result) {
            return AjaxResult.success(result);
        } else {
            return AjaxResult.error();
        }
    }

    @Autowired
    RedisCache redisCache;

    @PostMapping("/test/redis")
    public String setRedis(@RequestBody AliPayDTO aliPayDTO) {
        String uuid = UUID.randomUUID().toString();
        redisCache.setCacheObject(uuid, JSON.toJSONString(aliPayDTO), 1, TimeUnit.HOURS);

        System.out.println(JSON.toJSONString(redisCache.getCacheObject(uuid)));
        return uuid;
    }

    @ApiOperation(value = "用户AI测试")
    @Log(title = "用户AI测试", businessType = BusinessType.INSERT)
    @PostMapping("/ai")
    public AjaxResult aiTest(@RequestBody AiTestVo vo) {

        return AjaxResult.success(answerJsonService.aiTest(vo));
    }

    @ApiOperation(value = "用户AI测试，选择时间段")
    @Log(title = "用户AI测试，选择时间段", businessType = BusinessType.INSERT)
    @PostMapping("/aitime")
    public AjaxResult aiTestByTime(@RequestBody AiTestVo vo) {
        return AjaxResult.success(answerJsonService.aiTestByTime(vo));
    }
    //用户自己与明星的配对    图

    //解析接口

    @ApiOperation(value = "检测用户与明星匹配值")
    @GetMapping("/star/match")
    public AjaxResult aiTestWithStar(String starMbti, String userMbti) {
//        UserSurveyResult userSurveyResult = userSurveyResultMapper.selectById(userId);
//        if (userSurveyResult == null) {
//            return AjaxResult.error("无匹配用户(userId=" + userId + ")");
//        }
//        String userMbti = userSurveyResult.getAnswerResult();

        LambdaQueryWrapper<MbtiMatch> mbtiWrapper = new LambdaQueryWrapper<>();
        mbtiWrapper.eq(MbtiMatch::getUserMbti, userMbti)
                .eq(MbtiMatch::getStarMbti, starMbti);
        // TODO: 2024/10/18
        MbtiMatch mbtiMatch = mbtiMatchMapper.selectOne(mbtiWrapper);
        if (mbtiMatch == null) {
            return AjaxResult.error();
        }
        return AjaxResult.success(mbtiMatch.getMatchLevel());
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
