package org.jeecg.modules.demo.study.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.demo.study.entity.CloudComputingStudentLearningRecord;

/**
 * @Description: 学生学习记录表
 * @Author: jeecg-boot
 * @Date:   2025-09-22
 * @Version: V1.0
 */
public interface ICloudComputingStudentLearningRecordService extends IService<CloudComputingStudentLearningRecord> {
    public int getSumstudytime(String UserName);

    public int getSumstudyday(String UserName);

}
