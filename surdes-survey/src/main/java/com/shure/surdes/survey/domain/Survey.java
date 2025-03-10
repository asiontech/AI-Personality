package com.shure.surdes.survey.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.shure.surdes.common.annotation.Excel;
import com.shure.surdes.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 问卷对象 tb_survey
 *
 * @author Shure
 * @date 2021-10-18
 */
public class Survey extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 问卷主键
     */
    private Long surveyId;

    /**
     * 问卷名称
     */
    @Excel(name = "问卷名称")
    private String surveyName;

    /**
     * 问卷说明描述
     */
    @Excel(name = "问卷说明描述")
    private String surveyDesc;

    /**
     * 问卷类型
     */
    @Excel(name = "问卷类型")
    private String surveyType;
    
    /** 问卷类型参数 */
    private String surveyTypeChar;

    /**
     * 问卷状态（0：未发布，1：收集中，2：已结束）
     */
    @Excel(name = "问卷状态", readConverterExp = "0=：未发布，1：收集中，2：已结束")
    private String surveyStatus;
    

    public String getSurveyTypeChar() {
		return surveyTypeChar;
	}

	public void setSurveyTypeChar(String surveyTypeChar) {
		this.surveyTypeChar = surveyTypeChar;
	}

	/**
     * 创建人
     */
    @Excel(name = "创建人")
    private Long userId;
    
    /** 测评状态finish */
    private String testStaus;
    
    /** 支付状态pay */
    private String payStatus;
    
    /** 是否免费0免费1收费 */
    private Integer freeFlag;
    
    /** 原价 */
    private Double originPrice;
    
    /** 特惠价格 */
    private Double discountPrice;

    private String remark;

    public Integer getFreeFlag() {
		return freeFlag;
	}

	public void setFreeFlag(Integer freeFlag) {
		this.freeFlag = freeFlag;
	}

	public Double getOriginPrice() {
		return originPrice;
	}

	public void setOriginPrice(Double originPrice) {
		this.originPrice = originPrice;
	}

	public Double getDiscountPrice() {
		return discountPrice;
	}

	public void setDiscountPrice(Double discountPrice) {
		this.discountPrice = discountPrice;
	}

	public String getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(String payStatus) {
		this.payStatus = payStatus;
	}

	/**
     * 数据状态（1：有效，0：无效）
     */
    @Excel(name = "数据状态", readConverterExp = "1=：有效，0：无效")
    private String status;

    /**
     * 帐套编码
     */
    @Excel(name = "帐套编码")
    private String bookCode;

    public void setSurveyId(Long surveyId) {
        this.surveyId = surveyId;
    }

    public Long getSurveyId() {
        return surveyId;
    }

    public void setSurveyName(String surveyName) {
        this.surveyName = surveyName;
    }

    public String getSurveyName() {
        return surveyName;
    }

    public void setSurveyDesc(String surveyDesc) {
        this.surveyDesc = surveyDesc;
    }

    public String getSurveyDesc() {
        return surveyDesc;
    }

    public void setSurveyType(String surveyType) {
        this.surveyType = surveyType;
    }

    public String getSurveyType() {
        return surveyType;
    }

    public void setSurveyStatus(String surveyStatus) {
        this.surveyStatus = surveyStatus;
    }

    public String getSurveyStatus() {
        return surveyStatus;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTestStaus() {
		return testStaus;
	}

	public void setTestStaus(String testStaus) {
		this.testStaus = testStaus;
	}

	public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setBookCode(String bookCode) {
        this.bookCode = bookCode;
    }

    public String getBookCode() {
        return bookCode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("surveyId", getSurveyId())
                .append("surveyName", getSurveyName())
                .append("surveyDesc", getSurveyDesc())
                .append("surveyType", getSurveyType())
                .append("surveyStatus", getSurveyStatus())
                .append("createTime", getCreateTime())
                .append("userId", getUserId())
                .append("status", getStatus())
                .append("bookCode", getBookCode())
                .toString();
    }
}
