package org.jeecg.modules.demo.study.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * @Description: 学生学习记录表
 * @Author: jeecg-boot
 * @Date:   2025-09-22
 * @Version: V1.0
 */
@Data
@TableName("cloud_computing_student_learning_record")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="cloud_computing_student_learning_record对象", description="学生学习记录表")
public class CloudComputingStudentLearningRecord implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
	/**创建人*/
    @ApiModelProperty(value = "创建人")
    private String createBy;
	/**更新人*/
    @ApiModelProperty(value = "更新人")
    private String updateBy;
	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新日期")
    private java.util.Date updateTime;
	/**所属部门*/
    @ApiModelProperty(value = "所属部门")
    private String sysOrgCode;
	/**课程名称*/
	@Excel(name = "课程名称", width = 15, dictTable = "cloud_computing_course", dicText = "course_name", dicCode = "id")
	@Dict(dictTable = "cloud_computing_course", dicText = "course_name", dicCode = "id")
    @ApiModelProperty(value = "课程名称")
    private String courseId;
	/**学生名称*/
	@Excel(name = "学生名称", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "id")
	@Dict(dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @ApiModelProperty(value = "学生名称")
    private String userId;
	/**学习资源名称*/
	@Excel(name = "学习资源名称", width = 15, dictTable = "cloud_computing_course_resource", dicText = "resource_name", dicCode = "id")
	@Dict(dictTable = "cloud_computing_course_resource", dicText = "resource_name", dicCode = "id")
    @ApiModelProperty(value = "学习资源名称")
    private String contentId;
	/**学习状态*/
	@Excel(name = "学习状态", width = 15, dicCode = "learning_status")
	@Dict(dicCode = "learning_status")
    @ApiModelProperty(value = "学习状态")
    private String learningStatus;
	/**累计学习时长(秒)*/
	@Excel(name = "累计学习时长(秒)", width = 15)
    @ApiModelProperty(value = "累计学习时长(秒)")
    private Integer learningTime;
	/**最后一次学习时间*/
	@Excel(name = "最后一次学习时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "最后一次学习时间")
    private java.util.Date lastLearnTime;
	/**记录创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "记录创建日期")
    private java.util.Date createTime;
}
