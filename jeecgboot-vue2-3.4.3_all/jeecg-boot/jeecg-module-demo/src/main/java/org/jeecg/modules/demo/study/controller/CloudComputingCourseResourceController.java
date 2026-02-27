package org.jeecg.modules.demo.study.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.demo.study.entity.CloudComputingCourseResource;
import org.jeecg.modules.demo.study.service.ICloudComputingCourseResourceService;
import org.jeecg.modules.demo.study.vo.CloudComputingCourseResourceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: 学习资源表
 * @Author: jeecg-boot
 * @Date: 2025-09-20
 * @Version: V1.0
 */
@Api(tags = "学习资源表")
@RestController
@RequestMapping("/study/cloudComputingCourseResource")
@Slf4j
public class CloudComputingCourseResourceController extends JeecgController<CloudComputingCourseResource, ICloudComputingCourseResourceService> {
	@Autowired
	private ICloudComputingCourseResourceService cloudComputingCourseResourceService;


	@ApiOperation(value = "根据 id 查询各资源类型总数", notes = "返回视频/讲义/实验等各类资源的数量")
	@GetMapping(value = "/counts")
	public Result<Map<String, Long>> getResourceCounts(@RequestParam(name = "id", required = true) String id) {
		Map<String, Long> counts = cloudComputingCourseResourceService.countByCourseIdGrouped(id);
		return Result.OK(counts);
	}

	// language: java
	@ApiOperation(value = "学习资源列表(含学生学习状态)", notes = "传入 courseId 和 studentId 返回资源及该学生的学习记录状态")
	@GetMapping(value = "/listWithStatus")
	public Result<List<CloudComputingCourseResourceVO>> listWithStatus(@RequestParam(name = "courseId", required = true) String courseId) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		List<CloudComputingCourseResourceVO> list = cloudComputingCourseResourceService.listWithLearningStatus(courseId, user.getId());
		return Result.OK(list);
	}


	/**
	 * 分页列表查询`
	 *
	 * @param cloudComputingCourseResource
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "学习资源表-分页列表查询")
	@ApiOperation(value = "学习资源表-分页列表查询", notes = "学习资源表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CloudComputingCourseResource>> queryPageList(CloudComputingCourseResource cloudComputingCourseResource, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
		QueryWrapper<CloudComputingCourseResource> queryWrapper = QueryGenerator.initQueryWrapper(cloudComputingCourseResource, req.getParameterMap());
		Page<CloudComputingCourseResource> page = new Page<CloudComputingCourseResource>(pageNo, pageSize);
		IPage<CloudComputingCourseResource> pageList = cloudComputingCourseResourceService.page(page, queryWrapper);
		return Result.OK(pageList);
	}


	/**
	 * 添加
	 *
	 * @param cloudComputingCourseResource
	 * @return
	 */
	@AutoLog(value = "学习资源表-添加")
	@ApiOperation(value = "学习资源表-添加", notes = "学习资源表-添加")
	//@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_course_resource:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CloudComputingCourseResource cloudComputingCourseResource) {
		cloudComputingCourseResourceService.save(cloudComputingCourseResource);
		return Result.OK("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param cloudComputingCourseResource
	 * @return
	 */
	@AutoLog(value = "学习资源表-编辑")
	@ApiOperation(value = "学习资源表-编辑", notes = "学习资源表-编辑")
	//@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_course_resource:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
	public Result<String> edit(@RequestBody CloudComputingCourseResource cloudComputingCourseResource) {
		cloudComputingCourseResourceService.updateById(cloudComputingCourseResource);
		return Result.OK("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "学习资源表-通过id删除")
	@ApiOperation(value = "学习资源表-通过id删除", notes = "学习资源表-通过id删除")
	//@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_course_resource:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
		cloudComputingCourseResourceService.removeById(id);
		return Result.OK("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "学习资源表-批量删除")
	@ApiOperation(value = "学习资源表-批量删除", notes = "学习资源表-批量删除")
	//@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_course_resource:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
		this.cloudComputingCourseResourceService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "学习资源表-通过id查询")
	@ApiOperation(value = "学习资源表-通过id查询", notes = "学习资源表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CloudComputingCourseResource> queryById(@RequestParam(name = "id", required = true) String id) {
		CloudComputingCourseResource cloudComputingCourseResource = cloudComputingCourseResourceService.getById(id);
		if (cloudComputingCourseResource == null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cloudComputingCourseResource);
	}

	/**
	 * 导出excel
	 *
	 * @param request
	 * @param cloudComputingCourseResource
	 */
	//@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_course_resource:exportXls")
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, CloudComputingCourseResource cloudComputingCourseResource) {
		return super.exportXls(request, cloudComputingCourseResource, CloudComputingCourseResource.class, "学习资源表");
	}

	/**
	 * 通过excel导入数据
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	//@RequiresPermissions("cloud_computing_course_resource:importExcel")
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		return super.importExcel(request, response, CloudComputingCourseResource.class);
	}

}
