package org.jeecg.modules.demo.study.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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
public interface CloudComputingCourseResourceMapper extends BaseMapper<CloudComputingCourseResource> {


    /**
     * 返回每个资源类型的数量（请根据实际表/列名调整 SQL）
     */
    @Select("SELECT resource_type AS type, COUNT(*) AS cnt FROM cloud_computing_course_resource WHERE course_id = #{courseId} GROUP BY resource_type")
    List<Map<String, Object>> countByCourseIdGroupByType(@Param("courseId") String courseId);

    @Select("SELECT "
            + "r.id, r.create_by AS createBy, r.create_time AS createTime, r.update_by AS updateBy, r.update_time AS updateTime, r.sys_org_code AS sysOrgCode, "
            + "r.resource_type AS resourceType, r.course_id AS courseId, r.resource_name AS resourceName, r.resource_url AS resourceUrl, r.resource_sort AS resourceSort, "
            + "lr.id AS recordId, lr.user_id AS studentId, lr.content_id AS contentId, lr.learning_status AS learningStatus, lr.learning_time AS learningTime, lr.last_learn_time AS lastLearnTime, lr.create_time AS recordCreateTime "
            + "FROM cloud_computing_course_resource r "
            + "LEFT JOIN cloud_computing_student_learning_record lr "
            + "ON r.id = lr.content_id AND (#{studentId} IS NULL OR lr.user_id = #{studentId}) AND lr.course_id = #{courseId} "
            + "WHERE r.course_id = #{courseId} "
            + "ORDER BY r.resource_sort ASC")
    List<CloudComputingCourseResourceVO> listWithLearningStatus(@Param("courseId") String courseId, @Param("studentId") String studentId);
}
