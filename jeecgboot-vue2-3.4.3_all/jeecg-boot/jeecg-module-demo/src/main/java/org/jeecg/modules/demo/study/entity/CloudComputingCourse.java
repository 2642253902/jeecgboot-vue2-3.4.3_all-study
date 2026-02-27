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
import java.util.Date;

/**
 * @Description: 课程表
 * @Author: jeecg-boot
 * @Date:   2025-09-20
 * @Version: V1.0
 */
@Data
@TableName("cloud_computing_course")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="cloud_computing_course对象", description="课程表")
public class CloudComputingCourse implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
	/**创建人*/
    @ApiModelProperty(value = "创建人")
    private String createBy;
	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
    private Date createTime;
	/**更新人*/
    @ApiModelProperty(value = "更新人")
    private String updateBy;
	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新日期")
    private Date updateTime;
	/**所属部门*/
    @ApiModelProperty(value = "所属部门")
    private String sysOrgCode;
	/**课程名称*/
	@Excel(name = "课程名称", width = 15)
    @ApiModelProperty(value = "课程名称")
    private String courseName;
	/**课程分类名称*/
	@Excel(name = "课程分类名称", width = 15, dictTable = "cloud_computing_course_type", dicText = "course_type_name", dicCode = "id")
	@Dict(dictTable = "cloud_computing_course_type", dicText = "course_type_name", dicCode = "id")
    @ApiModelProperty(value = "课程分类名称")
    private String courseTypeId;
	/**课程标签*/
	@Excel(name = "课程标签", width = 15)
    @ApiModelProperty(value = "课程标签")
    private String courseTag;
	/**关联负责教师*/
	@Excel(name = "关联负责教师", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "id")
	@Dict(dictTable = "sys_user", dicText = "realname", dicCode = "id")
    @ApiModelProperty(value = "关联负责教师")
    private String teacherId;
	/**总课时（h）*/
	@Excel(name = "总课时（h）", width = 15)
    @ApiModelProperty(value = "总课时（h）")
    private Integer courseHours;
	/**课程状态*/
	@Excel(name = "课程状态", width = 15, dicCode = "course_status")
	@Dict(dicCode = "course_status")
    @ApiModelProperty(value = "课程状态")
    private Integer courseStatus;
}
