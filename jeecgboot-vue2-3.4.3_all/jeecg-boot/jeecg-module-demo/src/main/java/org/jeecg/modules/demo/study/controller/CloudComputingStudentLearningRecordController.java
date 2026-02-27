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
import org.jeecg.modules.demo.study.entity.CloudComputingStudentLearningRecord;
import org.jeecg.modules.demo.study.service.ICloudComputingStudentLearningRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @Description: 学生学习记录表
 * @Author: jeecg-boot
 * @Date: 2025-09-22
 * @Version: V1.0
 */
@Api(tags = "学生学习记录表")
@RestController
@RequestMapping("/study/cloudComputingStudentLearningRecord")
@Slf4j
public class CloudComputingStudentLearningRecordController extends JeecgController<CloudComputingStudentLearningRecord, ICloudComputingStudentLearningRecordService> {
	@Autowired
	private ICloudComputingStudentLearningRecordService cloudComputingStudentLearningRecordService;

	// java
	@ApiOperation(value = "学生学习记录表-分页列表查询", notes = "学生学习记录表-分页列表查询")
	@GetMapping(value = "/userlist")
	public Result<IPage<CloudComputingStudentLearningRecord>> queryPageList(CloudComputingStudentLearningRecord cloudComputingStudentLearningRecord,
																			HttpServletRequest req) {
		// 使用当前登录用户的 id
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		if (user != null && user.getId() != null) {
			cloudComputingStudentLearningRecord.setUserId(user.getId());
		}
		QueryWrapper<CloudComputingStudentLearningRecord> queryWrapper = QueryGenerator.initQueryWrapper(cloudComputingStudentLearningRecord, req.getParameterMap());
		Page<CloudComputingStudentLearningRecord> page = new Page<>();
		IPage<CloudComputingStudentLearningRecord> pageList = cloudComputingStudentLearningRecordService.page(page, queryWrapper);
		return Result.OK(pageList);
	}


	/**
	 * 分页列表查询
	 *
	 * @param cloudComputingStudentLearningRecord
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "学生学习记录表-分页列表查询")
	@ApiOperation(value = "学生学习记录表-分页列表查询", notes = "学生学习记录表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CloudComputingStudentLearningRecord>> queryPageList(CloudComputingStudentLearningRecord cloudComputingStudentLearningRecord,
																			@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
																			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
																			HttpServletRequest req) {
		QueryWrapper<CloudComputingStudentLearningRecord> queryWrapper = QueryGenerator.initQueryWrapper(cloudComputingStudentLearningRecord, req.getParameterMap());
		Page<CloudComputingStudentLearningRecord> page = new Page<CloudComputingStudentLearningRecord>(pageNo, pageSize);
		IPage<CloudComputingStudentLearningRecord> pageList = cloudComputingStudentLearningRecordService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 * 添加
	 *
	 * @param cloudComputingStudentLearningRecord
	 * @return
	 */
	@AutoLog(value = "学生学习记录表-添加")
	@ApiOperation(value = "学生学习记录表-添加", notes = "学生学习记录表-添加")
	//@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_student_learning_record:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CloudComputingStudentLearningRecord cloudComputingStudentLearningRecord) {
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		cloudComputingStudentLearningRecord.setUserId(user.getId());
		cloudComputingStudentLearningRecordService.save(cloudComputingStudentLearningRecord);
		return Result.OK("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param cloudComputingStudentLearningRecord
	 * @return
	 */
	@AutoLog(value = "学生学习记录表-编辑")
	@ApiOperation(value = "学生学习记录表-编辑", notes = "学生学习记录表-编辑")
	//@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_student_learning_record:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
	public Result<String> edit(@RequestBody CloudComputingStudentLearningRecord cloudComputingStudentLearningRecord) {
		cloudComputingStudentLearningRecordService.updateById(cloudComputingStudentLearningRecord);
		return Result.OK("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "学生学习记录表-通过id删除")
	@ApiOperation(value = "学生学习记录表-通过id删除", notes = "学生学习记录表-通过id删除")
	//@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_student_learning_record:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
		cloudComputingStudentLearningRecordService.removeById(id);
		return Result.OK("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "学生学习记录表-批量删除")
	@ApiOperation(value = "学生学习记录表-批量删除", notes = "学生学习记录表-批量删除")
	//@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_student_learning_record:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
		this.cloudComputingStudentLearningRecordService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "学生学习记录表-通过id查询")
	@ApiOperation(value = "学生学习记录表-通过id查询", notes = "学生学习记录表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CloudComputingStudentLearningRecord> queryById(@RequestParam(name = "id", required = true) String id) {
		CloudComputingStudentLearningRecord cloudComputingStudentLearningRecord = cloudComputingStudentLearningRecordService.getById(id);
		if (cloudComputingStudentLearningRecord == null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cloudComputingStudentLearningRecord);
	}

	/**
	 * 导出excel
	 *
	 * @param request
	 * @param cloudComputingStudentLearningRecord
	 */
	//@RequiresPermissions("org.jeecg.modules.demo:cloud_computing_student_learning_record:exportXls")
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, CloudComputingStudentLearningRecord cloudComputingStudentLearningRecord) {
		return super.exportXls(request, cloudComputingStudentLearningRecord, CloudComputingStudentLearningRecord.class, "学生学习记录表");
	}

	/**
	 * 通过excel导入数据
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	//@RequiresPermissions("cloud_computing_student_learning_record:importExcel")
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		return super.importExcel(request, response, CloudComputingStudentLearningRecord.class);
	}

}
