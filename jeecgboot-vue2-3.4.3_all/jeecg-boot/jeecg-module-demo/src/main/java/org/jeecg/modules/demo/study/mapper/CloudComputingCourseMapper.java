package org.jeecg.modules.demo.study.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.demo.study.entity.CloudComputingCourse;

import java.util.List;
import java.util.Map;

/**
 * @Description: 课程表
 * @Author: jeecg-boot
 * @Date: 2025-09-20
 * @Version: V1.0
 */
public interface CloudComputingCourseMapper extends BaseMapper<CloudComputingCourse> {
    @Select("SELECT \n" +
            "    su.id as value,\n" +
            "    su.realname as text,\n" +
            "    su.realname as title,\n" +
            "    su.realname as label\n" +
            "FROM sys_user_depart sud\n" +
            "INNER JOIN sys_depart sd ON sud.dep_id = sd.id \n" +
            "INNER JOIN sys_user su ON sud.user_id = su.id\n" +
            "WHERE sd.depart_name = '教师' ")
    public List<Map> getTeacher();

    @Delete("DELETE co, cr FROM cloud_computing_course co " +
            "LEFT JOIN cloud_computing_course_resource cr ON cr.course_id = co.id " +
            "WHERE co.id = #{courseId}")
    int deleteCourseById(@Param("courseId") String courseId);


    @Select("SELECT count(*)  \n" +
            "FROM cloud_computing_course as a \n" +
            "left join cloud_computing_class as b on a.id = b.course_id\n" +
            "left join cloud_computing_class_student as c on b.id = c.class_id\n" +
            "left join sys_user as d on c.student_id = d.username\n" +
            "where d.username =  #{UserName}")
    public int getSumcourse(String UserName);
//    查询全部课程

    @Select("SELECT \n" +
            "     a.id,\n" +
            "    COUNT(*) AS total_resources,\n" +
            "    SUM(CASE WHEN e.learning_status = 1 THEN 1 ELSE 0 END) AS completed_count\n" +
            "FROM cloud_computing_course a\n" +
            "LEFT JOIN cloud_computing_class b ON a.id = b.course_id\n" +
            "LEFT JOIN cloud_computing_class_student c ON b.id = c.class_id\n" +
            "LEFT JOIN sys_user d ON c.student_id = d.username\n" +
            "LEFT JOIN cloud_computing_student_learning_record e ON a.id = e.course_id\n" +
            "WHERE d.username =  #{UserName}\n" +
            "  AND e.learning_status IS NOT NULL\n" +
            "GROUP BY a.id\n" +
            "HAVING COUNT(*) = SUM(CASE WHEN e.learning_status = 1 THEN 1 ELSE 0 END);\n")
    public List<String> getCompletedcourse(String UserName);
//   查询已完成课程


    @Select("SELECT c.id , c.course_name\n" +
            "            FROM cloud_computing_course c \n" +
            "           LEFT JOIN sys_user u ON c.teacher_id = u.ID\n" +
            "            WHERE c.teacher_id =  #{UserId}")
    public List<Map> getTeachercourse(String UserId);
//    查询教师所有的课程

    @Select("SELECT count(*) \n" +
            "FROM cloud_computing_course a \n" +
            "LEFT JOIN cloud_computing_class b ON a.ID = b.course_id\n" +
            "-- 课程id\n" +
            "where a.id = #{CourseId}\n" +
            "-- 教师id\n" +
            "AND a.teacher_id = #{UserId}")
    public int getTeacherNumber(String UserId,String CourseId);
//    查询教师某课程的开班数量



    @Select("SELECT\n" +
            "    FLOOR(SUM(c.learning_time) / 3600) AS total_learning_time\n" +
            "    FROM cloud_computing_course a\n" +
            "    LEFT JOIN cloud_computing_class b ON a.id = b.course_id\n" +
            "    LEFT JOIN cloud_computing_class_student d ON d.class_id = b.id\n" +
            "    LEFT JOIN cloud_computing_student_learning_record c\n" +
            "    ON c.course_id = a.id\n" +
            "    AND c.update_by = d.student_id\n" +
            "    WHERE a.id = #{CourseId}\n" +
            "    AND a.teacher_id = #{UserId}\n" +
            "    AND c.learning_time IS NOT NULL")
    public int getTeacherTime(String UserId,String CourseId);
    //    查询教师某课程的全部学生学习时长


    @Select("SELECT count(*)\n" +
            "FROM cloud_computing_course a \n" +
            "LEFT JOIN cloud_computing_class b ON a.ID = b.course_id\n" +
            "LEFT JOIN cloud_computing_class_student c ON c.class_id = b.ID\n" +
            "where a.id = #{CourseId}\n" +
            "AND a.teacher_id = #{UserId}")
    public int getTeachersum(String UserId,String CourseId);
    //    查询教师某课程里的全部学生


    @Select("SELECT \n" +
            "    a.course_name,\n" +
            "    b.class_name,\n" +
            "    c.student_id,\n" +
            "     \n" +
            "    CASE \n" +
            "        WHEN MAX(COALESCE(e.learning_status, 0)) = 1 THEN 1 \n" +
            "        ELSE 0 \n" +
            "    END AS final_learning_status\n" +
            "FROM \n" +
            "    cloud_computing_course a\n" +
            "LEFT JOIN \n" +
            "    cloud_computing_class b ON a.id = b.course_id\n" +
            "LEFT JOIN \n" +
            "    cloud_computing_class_student c ON b.id = c.class_id\n" +
            "LEFT JOIN \n" +
            "    cloud_computing_student_learning_record e \n" +
            "    ON a.id = e.course_id AND c.student_id = e.update_by\n" +
            "WHERE \n" +
            "\t\t a.id = #{CourseId}\n" +
            "\t  AND a.teacher_id = #{UserId}\n" +
            " AND e.learning_status = 1\n" +
            "GROUP BY \n" +
            "    a.course_name, b.class_name, c.student_id;")
    public List<String> getTeacherComplete(String UserId,String CourseId);
    //    -- 查询教师某课程里学习完成的学生数量

    @Select("SELECT role_name FROM sys_role WHERE id =(SELECT role_id FROM sys_user_role WHERE  user_id =(SELECT id FROM sys_user WHERE username =#{UserId}))")
    public String getrole(String UserName);
    //    -- 查询教师某课程里学习完成的学生数量



}
