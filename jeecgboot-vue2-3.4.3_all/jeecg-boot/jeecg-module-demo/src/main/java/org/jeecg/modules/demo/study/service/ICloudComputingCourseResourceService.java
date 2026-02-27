package org.jeecg.modules.demo.study.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.demo.study.entity.CloudComputingCourseResource;
import org.jeecg.modules.demo.study.vo.CloudComputingCourseResourceVO;

import java.util.List;
import java.util.Map;

/**
 * @Description: 学习资源表
 * @Author: jeecg-boot
 * @Date:   2025-09-20
 * @Version: V1.0
 */
public interface ICloudComputingCourseResourceService extends IService<CloudComputingCourseResource> {

    /**
     * 根据 courseId 分组统计各资源类型数量
     * @param courseId 课程 id
     * @return key=资源类型，value=数量
     */
    Map<String, Long> countByCourseIdGrouped(String courseId);


    List<CloudComputingCourseResourceVO> listWithLearningStatus(String courseId, String studentId);
}
