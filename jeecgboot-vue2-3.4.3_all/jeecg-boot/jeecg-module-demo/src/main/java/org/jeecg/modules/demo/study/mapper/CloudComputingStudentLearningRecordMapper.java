package org.jeecg.modules.demo.study.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.demo.study.entity.CloudComputingStudentLearningRecord;

/**
 * @Description: 学生学习记录表
 * @Author: jeecg-boot
 * @Date:   2025-09-22
 * @Version: V1.0
 */
public interface CloudComputingStudentLearningRecordMapper extends BaseMapper<CloudComputingStudentLearningRecord> {

    @Select("SELECT \n" +
            "    ROUND(SUM(learning_time) / 3600.0) AS total_learning_hours   \n" +
            "FROM \n" +
            "    cloud_computing_student_learning_record cslr\n" +
            "        LEFT JOIN sys_user u on u.id = cslr.user_id\n" +
            "WHERE \n" +
            "    u.username =  #{UserName}  \n" +
            "    AND learning_time > 0;  ")
    public int getSumstudytime(String UserName);
//   查询总学习时长

    @Select("SELECT COUNT(*) AS '总学习天数'\n" +
            "FROM (\n" +
            "    SELECT DATE(csld.create_time) AS d \n" +
            "        FROM cloud_computing_student_learning_record csld \n" +
            "        LEFT JOIN sys_user u ON u.id = csld.user_id\n" +
            "        WHERE u.username =  #{UserName}\n" +
            "    UNION\n" +
            "    SELECT DATE(csld.last_learn_time) AS d \n" +
            "        FROM cloud_computing_student_learning_record csld \n" +
            "        LEFT JOIN sys_user u ON u.id = csld.user_id\n" +
            "        WHERE u.username =  #{UserName}\n" +
            ") AS unique_days; ")
    public int getSumstudyday(String UserName);
//    查询总学习天数

}
