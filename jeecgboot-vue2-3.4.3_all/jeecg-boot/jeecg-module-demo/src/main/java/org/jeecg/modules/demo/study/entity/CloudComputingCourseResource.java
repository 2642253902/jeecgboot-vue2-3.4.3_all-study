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
 * @Description: 学习资源表
 * @Author: jeecg-boot
 * @Date:   2025-09-20
 * @Version: V1.0
 */
@Data
@TableName("cloud_computing_course_resource")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="cloud_computing_course_resource对象", description="学习资源表")
public class CloudComputingCourseResource implements Serializable {
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
	/**学习内容类型*/
	@Excel(name = "学习内容类型", width = 15, dicCode = "resource_type")
	@Dict(dicCode = "resource_type")
    @ApiModelProperty(value = "学习内容类型")
    private Integer resourceType;
	/**课程名称*/
	@Excel(name = "课程名称", width = 15, dictTable = "cloud_computing_course", dicText = "course_name", dicCode = "id")
	@Dict(dictTable = "cloud_computing_course", dicText = "course_name", dicCode = "id")
    @ApiModelProperty(value = "课程名称")
    private String courseId;
	/**资源名称*/
	@Excel(name = "资源名称", width = 15)
    @ApiModelProperty(value = "资源名称")
    private String resourceName;
	/**资源存储地址*/
	@Excel(name = "资源存储地址", width = 15)
    @ApiModelProperty(value = "资源存储地址")
    private String resourceUrl;
	/**资源排序*/
	@Excel(name = "资源排序", width = 15)
    @ApiModelProperty(value = "资源排序")
    private Integer resourceSort;
}
