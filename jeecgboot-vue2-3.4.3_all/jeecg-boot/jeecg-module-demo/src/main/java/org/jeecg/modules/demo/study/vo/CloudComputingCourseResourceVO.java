// language: java
package org.jeecg.modules.demo.study.vo;

import lombok.Data;

import java.util.Date;

@Data
public class CloudComputingCourseResourceVO {
    // 资源表字段
    private String id;
    private String createBy;
    private Date createTime;
    private String updateBy;
    private Date updateTime;
    private String sysOrgCode;
    private Integer resourceType;
    private String courseId;
    private String resourceName;
    private String resourceUrl;
    private Integer resourceSort;

    // 学习记录表字段（可能为 null）
    private String recordId;
    private String studentId;
    private String contentId;
    private String learningStatus;
    private Integer learningTime;
    private Date lastLearnTime;
    private Date recordCreateTime;
}
