package org.jeecg.modules.demo.study.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.util.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.study.entity.CloudComputingCourse;
import org.jeecg.modules.demo.study.service.ICloudComputingCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 课程表
 * @Author: jeecg-boot
 * @Date: 2025-09-20
 * @Version: V1.0
 */
@Api(tags = "课程表")
@RestController
@RequestMapping("/study/cloudComputingCourse")
@Slf4j
public class CloudComputingCourseController extends JeecgController<CloudComputingCourse, ICloudComputingCourseService> {
    @Autowired
    private ICloudComputingCourseService cloudComputingCourseService;

    /**
     * 分页列表查询
     *
     * @param cloudComputingCourse
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "课程表-分页列表查询")
    @ApiOperation(value = "课程表-分页列表查询", notes = "课程表-分页列表查询")
    @GetMapping(value = "/list")
    //@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_course:list")
    public Result<IPage<CloudComputingCourse>> queryPageList(CloudComputingCourse cloudComputingCourse, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<CloudComputingCourse> queryWrapper = QueryGenerator.initQueryWrapper(cloudComputingCourse, req.getParameterMap());
        Page<CloudComputingCourse> page = new Page<CloudComputingCourse>(pageNo, pageSize);
        IPage<CloudComputingCourse> pageList = cloudComputingCourseService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param cloudComputingCourse
     * @return
     */

    @AutoLog(value = "课程表-添加")
    @ApiOperation(value = "课程表-添加", notes = "课程表-添加")
    //@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_course:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody CloudComputingCourse cloudComputingCourse) {
        cloudComputingCourseService.save(cloudComputingCourse);
        return Result.OK("添加成功！");
    }

    @AutoLog(value = "课程表-添加使获取用户信息")
    @ApiOperation(value = "课程表-添加使获取用户信息", notes = "课程表-添加使获取用户信息")
    //@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_course:addByUser")
    @GetMapping(value = "/addByUser")
    public Result<LoginUser> addByUser() {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<Map> teacher = cloudComputingCourseService.getTeacher();
        for (Map i : teacher) {
            if (i.get("value").equals(user.getId())) {
                return Result.OK(user);
            }
        }
        return Result.OK("该用户是管理员");
    }

    /**
     * 编辑
     *
     * @param cloudComputingCourse
     * @return
     */
    @AutoLog(value = "课程表-编辑")
    @ApiOperation(value = "课程表-编辑", notes = "课程表-编辑")
    //@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_course:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody CloudComputingCourse cloudComputingCourse) {
        cloudComputingCourseService.updateById(cloudComputingCourse);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "课程表-通过id删除")
    @ApiOperation(value = "课程表-通过id删除", notes = "课程表-通过id删除")
    //@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_course:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        cloudComputingCourseService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "课程表-批量删除")
    @ApiOperation(value = "课程表-批量删除", notes = "课程表-批量删除")
    //@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_course:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.cloudComputingCourseService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }


    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "课程表-通过id查询")
    @ApiOperation(value = "课程表-通过id查询", notes = "课程表-通过id查询")
    @GetMapping(value = "/queryById")
    //@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_course:queryById")
    public Result<CloudComputingCourse> queryById(@RequestParam(name = "id", required = true) String id) {
        CloudComputingCourse cloudComputingCourse = cloudComputingCourseService.getById(id);
        if (cloudComputingCourse == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(cloudComputingCourse);
    }

    /**
     * 通过id区分教师
     * //	  * @param teacher
     *
     * @return
     */
    //@AutoLog(value = "课程表-通过id区分教师")
    @ApiOperation(value = "课程表-通过id区分教师", notes = "课程表-通过id区分教师")
    @GetMapping(value = "/queryById/teacher")
    //@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_course:queryByIdTeacher")
    public Result<?> getTeacher() {
        List<?> cloudComputingCourse = cloudComputingCourseService.getTeacher();
        if (cloudComputingCourse == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(cloudComputingCourse);
    }

    /**
     * TODO按指定教师ID过滤的课程列表查询
     *
     * @param cloudComputingCourse
     * @param teacherId
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "课程表-按指定教师ID过滤分页列表查询", notes = "课程表-按指定教师ID过滤分页列表查询")
    @GetMapping(value = "/listByTeacherId")
    //@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_course:listByTeacherId")
    public Result<IPage<CloudComputingCourse>> queryPageListByTeacherId(CloudComputingCourse cloudComputingCourse, @RequestParam(name = "teacherId", required = false) String teacherId, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<CloudComputingCourse> queryWrapper = new QueryWrapper<>();
        // 如果传入了教师ID，则按教师ID过滤
        if (StringUtils.hasText(teacherId)) {
            queryWrapper.eq("teacher_id", teacherId);
        }
        QueryGenerator.initQueryWrapper(cloudComputingCourse, req.getParameterMap());
        // 执行分页查询
        Page<CloudComputingCourse> page = new Page<>(pageNo, pageSize);
        IPage<CloudComputingCourse> pageList = cloudComputingCourseService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 通过id删除全部课程信息
     *
     * @param courseId
     * @return
     */
    @AutoLog(value = "课程表-通过id删除全部课程信息")
    @ApiOperation(value = "课程表-通过id删除全部课程信息", notes = "课程表-通过id删除全部课程信息")
    //@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_course:deleteCourse")
    @DeleteMapping(value = "/deleteCourse")
    public Result<String> deleteCourse(@RequestParam(name = "courseId", required = true) String courseId) {
        cloudComputingCourseService.deleteCourseById(courseId);
        return Result.OK("删除成功!");
    }

    @AutoLog(value = "课程表-更改课程状态")
    @ApiOperation(value = "课程表-更改课程状态", notes = "课程表-更改课程状态")
    //@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_course:editCourseStatusCourseIdStatus")
    @PostMapping(value = "/editCourseStatus/{courseId}/{status}")
    public Result<String> editCourseStatus(@PathVariable String courseId, @PathVariable int status) {
        CloudComputingCourse byId = cloudComputingCourseService.getById(courseId);
        byId.setCourseStatus(status);
        cloudComputingCourseService.updateById(byId);
        QueryWrapper<CloudComputingCourse> queryWrapper = new QueryWrapper<>();
        return Result.OK("更新成功!");
    }


    /**
     * 导出excel
     *
     * @param request
     * @param cloudComputingCourse
     */
    //@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_course:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CloudComputingCourse cloudComputingCourse) {
        return super.exportXls(request, cloudComputingCourse, CloudComputingCourse.class, "课程表");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("cloud_computing_course:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CloudComputingCourse.class);
    }

}
