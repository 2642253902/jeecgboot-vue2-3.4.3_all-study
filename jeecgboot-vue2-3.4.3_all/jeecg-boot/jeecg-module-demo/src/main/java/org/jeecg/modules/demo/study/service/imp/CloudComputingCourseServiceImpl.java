package org.jeecg.modules.demo.study.service.imp;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.demo.study.entity.CloudComputingCourse;
import org.jeecg.modules.demo.study.mapper.CloudComputingCourseMapper;
import org.jeecg.modules.demo.study.mapper.CloudComputingCourseResourceMapper;
import org.jeecg.modules.demo.study.service.ICloudComputingCourseService;
import org.jeecg.modules.demo.study.vo.CloudComputingCourseResourceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @Description: 课程表
 * @Author: jeecg-boot
 * @Date: 2025-09-20
 * @Version: V1.0
 */
@Service
public class CloudComputingCourseServiceImpl extends ServiceImpl<CloudComputingCourseMapper, CloudComputingCourse> implements ICloudComputingCourseService {

    @Autowired
    private CloudComputingCourseResourceMapper resourceMapper;

    @Autowired
    private CloudComputingCourseMapper courseMapper;

    @Override
    public List<CloudComputingCourseResourceVO> listWithLearningStatus(String courseId, String studentId) {
        return resourceMapper.listWithLearningStatus(courseId, studentId);
    }

    @Override
    public List<Map> getTeacher() {
        return getBaseMapper().getTeacher();
    }

    @Override
    @Transactional
    public boolean deleteCourseById(String courseId) {

            // 删除课程关联的全部信息
            int departResult = courseMapper.deleteCourseById(courseId);

            return true;

    }



    public int getSumcourse(String UserName){
        return  getBaseMapper().getSumcourse(UserName);
    };

    public  List<String> getCompletedcourse(String UserName){
        return  getBaseMapper().getCompletedcourse(UserName);
    };

    public  List<Map> getTeachercourse(String UserId){
        return  getBaseMapper().getTeachercourse(UserId);
    };

    public int getTeacherNumber(String UserId,String CourseId){
        return  getBaseMapper().getTeacherNumber(UserId,CourseId);
    };

    public int getTeacherTime(String UserId,String CourseId){
        return  getBaseMapper().getTeacherTime(UserId,CourseId);
    };
    public int getTeachersum(String UserId,String CourseId){
        return  getBaseMapper().getTeachersum(UserId,CourseId);
    };

    public List<String> getTeacherComplete(String UserId,String CourseId){
        return  getBaseMapper().getTeacherComplete(UserId,CourseId);
    };

    public  String getrole(String UserName){
        return  getBaseMapper().getrole(UserName);
    };

}
