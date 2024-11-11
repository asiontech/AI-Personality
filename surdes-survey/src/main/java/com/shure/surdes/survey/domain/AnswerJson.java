package com.shure.surdes.survey.domain;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.TableField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.shure.surdes.common.annotation.Excel;
import com.shure.surdes.common.core.domain.BaseEntity;

/**
 * 问卷答案结果json对象 tb_answer_json
 *
 * @author Shure
 * @date 2021-10-18
 */
public class AnswerJson extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 答案主键
     */
    private Long anId;

    /**
     * 问卷主键
     */
    @Excel(name = "问卷主键")
    private Long surveyId;

    /**
     * 答案结果，json格式存储
     */
    @Excel(name = "答案结果，json格式存储")
    private String answerJson;

    @Excel(name = "性格结果")
    private String answerResult;
    
    @Excel(name = "性格结果原始")
    private String answerResultOrigin;
    
    /** 词云 */
    private String keyCloud;
    
    /**
     * 答题人唯一标识
     */
    @Excel(name = "答题人唯一标识")
    private String userId;

    @Excel(name = "微博id")
    private String wbUid;

    /**
     * 答题人姓名
     */
    @Excel(name = "答题人姓名")
    private String userName;

    /**
     * 账套
     */
    @Excel(name = "账套")
    private String bookCode;
    
    /** 问卷类型 */
    private String surveyType;
    
    /** 订单时间戳 */
    private Long orderTimestamp;
    
    /** ai测试时间段开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startTime;
    
    /** ai测试时间段结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endTime;

    private List<Map<String, Object>> radar;

    @TableField(exist = false)
    private String starUid;
    
    /** 词云封装数据 */
    private List<JSONObject> keyCloudData;
    
	public List<JSONObject> getKeyCloudData() {
		return keyCloudData;
	}

	public void setKeyCloudData(List<JSONObject> keyCloudData) {
		this.keyCloudData = keyCloudData;
	}

	public List<Map<String, Object>> getRadar() {
		return radar;
	}

	public void setRadar(List<Map<String, Object>> radar) {
		this.radar = radar;
	}

	public String getKeyCloud() {
		return keyCloud;
	}

	public void setKeyCloud(String keyCloud) {
		this.keyCloud = keyCloud;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Long getOrderTimestamp() {
		return orderTimestamp;
	}

	public void setOrderTimestamp(Long orderTimestamp) {
		this.orderTimestamp = orderTimestamp;
	}

	public String getSurveyType() {
		return surveyType;
	}

	public void setSurveyType(String surveyType) {
		this.surveyType = surveyType;
	}

	public void setAnId(Long anId) {
        this.anId = anId;
    }

    public Long getAnId() {
        return anId;
    }

    public void setSurveyId(Long surveyId) {
        this.surveyId = surveyId;
    }

    public Long getSurveyId() {
        return surveyId;
    }

    public void setAnswerJson(String answerJson) {
        this.answerJson = answerJson;
    }

    public String getAnswerJson() {
        return answerJson;
    }

    public String getAnswerResult() {
		return answerResult;
	}

	public void setAnswerResult(String answerResult) {
		this.answerResult = answerResult;
	}

	public String getAnswerResultOrigin() {
		return answerResultOrigin;
	}

	public void setAnswerResultOrigin(String answerResultOrigin) {
		this.answerResultOrigin = answerResultOrigin;
	}

	public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setBookCode(String bookCode) {
        this.bookCode = bookCode;
    }

    public String getBookCode() {
        return bookCode;
    }

    public String getWbUid() {
        return wbUid;
    }

    public void setWbUid(String wbUid) {
        this.wbUid = wbUid;
    }

    public String getStarUid() {
        return starUid;
    }

    public void setStarUid(String starUid) {
        this.starUid = starUid;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("anId", getAnId())
                .append("surveyId", getSurveyId())
                .append("answerJson", getAnswerJson())
                .append("userId", getUserId())
                .append("userName", getUserName())
                .append("createTime", getCreateTime())
                .append("bookCode", getBookCode())
                .toString();
    }
}
