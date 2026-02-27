package org.jeecg.modules.demo.study.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.demo.study.entity.CloudComputingCourse;
import org.jeecg.modules.demo.study.vo.CloudComputingCourseResourceVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @Description: 课程表
 * @Author: jeecg-boot
 * @Date:   2025-09-20
 * @Version: V1.0
 */

public interface ICloudComputingCourseService extends IService<CloudComputingCourse> {


    List<CloudComputingCourseResourceVO> listWithLearningStatus(String courseId, String studentId);
    public List<Map> getTeacher();

    @Transactional
    boolean deleteCourseById(String id);

    public int getSumcourse(String UserName);

    public  List<String> getCompletedcourse(String UserName);

    public  List<Map> getTeachercourse(String UserId);


    public int getTeacherNumber(String UserId,String CourseId);

    public int getTeacherTime(String UserId,String CourseId);

    public int getTeachersum(String UserId,String CourseId);

    public List<String> getTeacherComplete(String UserId,String CourseId);

    public  String getrole(String UserName);
}
