package org.jeecg.modules.demo.study.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.demo.study.entity.CloudComputingCourseType;
import org.jeecg.modules.demo.study.service.ICloudComputingCourseTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
* @Description: 课程分类表
* @Author: jeecg-boot
* @Date:   2025-09-20
* @Version: V1.0
*/
@Api(tags="课程分类表")
@RestController
@RequestMapping("/study/cloudComputingCourseType")
@Slf4j
public class CloudComputingCourseTypeController extends JeecgController<CloudComputingCourseType, ICloudComputingCourseTypeService> {
   @Autowired
   private ICloudComputingCourseTypeService cloudComputingCourseTypeService;






   /**
    * 分页列表查询
    *
    * @param cloudComputingCourseType
    * @param pageNo
    * @param pageSize
    * @param req
    * @return
    */
   //@AutoLog(value = "课程分类表-分页列表查询")
   @ApiOperation(value="课程分类表-分页列表查询", notes="课程分类表-分页列表查询")
   @GetMapping(value = "/list")
   public Result<IPage<CloudComputingCourseType>> queryPageList(CloudComputingCourseType cloudComputingCourseType,
                                  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                  HttpServletRequest req) {
       QueryWrapper<CloudComputingCourseType> queryWrapper = QueryGenerator.initQueryWrapper(cloudComputingCourseType, req.getParameterMap());
       Page<CloudComputingCourseType> page = new Page<CloudComputingCourseType>(pageNo, pageSize);
       IPage<CloudComputingCourseType> pageList = cloudComputingCourseTypeService.page(page, queryWrapper);
       return Result.OK(pageList);
   }

   /**
    *   添加
    *
    * @param cloudComputingCourseType
    * @return
    */
   @AutoLog(value = "课程分类表-添加")
   @ApiOperation(value="课程分类表-添加", notes="课程分类表-添加")
   //@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_course_type:add")
   @PostMapping(value = "/add")
   public Result<String> add(@RequestBody CloudComputingCourseType cloudComputingCourseType) {
       cloudComputingCourseTypeService.save(cloudComputingCourseType);
       return Result.OK("添加成功！");
   }

   /**
    *  编辑
    *
    * @param cloudComputingCourseType
    * @return
    */
   @AutoLog(value = "课程分类表-编辑")
   @ApiOperation(value="课程分类表-编辑", notes="课程分类表-编辑")
   //@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_course_type:edit")
   @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
   public Result<String> edit(@RequestBody CloudComputingCourseType cloudComputingCourseType) {
       cloudComputingCourseTypeService.updateById(cloudComputingCourseType);
       return Result.OK("编辑成功!");
   }

   /**
    *   通过id删除
    *
    * @param id
    * @return
    */
   @AutoLog(value = "课程分类表-通过id删除")
   @ApiOperation(value="课程分类表-通过id删除", notes="课程分类表-通过id删除")
   //@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_course_type:delete")
   @DeleteMapping(value = "/delete")
   public Result<String> delete(@RequestParam(name="id",required=true) String id) {
       cloudComputingCourseTypeService.removeById(id);
       return Result.OK("删除成功!");
   }

   /**
    *  批量删除
    *
    * @param ids
    * @return
    */
   @AutoLog(value = "课程分类表-批量删除")
   @ApiOperation(value="课程分类表-批量删除", notes="课程分类表-批量删除")
   //@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_course_type:deleteBatch")
   @DeleteMapping(value = "/deleteBatch")
   public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
       this.cloudComputingCourseTypeService.removeByIds(Arrays.asList(ids.split(",")));
       return Result.OK("批量删除成功!");
   }

   /**
    * 通过id查询
    *
    * @param id
    * @return
    */
   //@AutoLog(value = "课程分类表-通过id查询")
   @ApiOperation(value="课程分类表-通过id查询", notes="课程分类表-通过id查询")
   @GetMapping(value = "/queryById")
   public Result<CloudComputingCourseType> queryById(@RequestParam(name="id",required=true) String id) {
       CloudComputingCourseType cloudComputingCourseType = cloudComputingCourseTypeService.getById(id);
       if(cloudComputingCourseType==null) {
           return Result.error("未找到对应数据");
       }
       return Result.OK(cloudComputingCourseType);
   }

   /**
   * 导出excel
   *
   * @param request
   * @param cloudComputingCourseType
   */
   //@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_course_type:exportXls")
   @RequestMapping(value = "/exportXls")
   public ModelAndView exportXls(HttpServletRequest request, CloudComputingCourseType cloudComputingCourseType) {
       return super.exportXls(request, cloudComputingCourseType, CloudComputingCourseType.class, "课程分类表");
   }

   /**
     * 通过excel导入数据
   *
   * @param request
   * @param response
   * @return
   */
   //@RequiresPermissions("cloud_computing_course_type:importExcel")
   @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
   public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
       return super.importExcel(request, response, CloudComputingCourseType.class);
   }

}
