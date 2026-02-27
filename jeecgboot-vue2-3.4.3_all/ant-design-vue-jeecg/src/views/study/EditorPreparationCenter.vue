<template>
  <a-card :bordered="false">

    <a-spin :spinning="confirmLoading">
      <!-- 标题 -->
      <div class="card-title">
        <h4 style="margin: 0; font-size: 18px;">{{ title }}</h4>
        <a-tag v-if="disabled" color="blue">查看模式</a-tag>
      </div>
      <!--    上半部分布局-->
      <!-- 表单 -->
      <a-form :form="form" layout="horizontal">
        <!-- 第一行 -->
        <a-row :gutter="16">
          <!-- 课程名称 -->
          <a-col :xl="8" :lg="8" :md="12" :sm="24" :xs="24">
            <a-form-item label="课程名称" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
              <a-input placeholder="请输入课程名称" v-decorator="['course_name', validatorRules.course_name]"
                :disabled="true" />
            </a-form-item>
          </a-col>

          <!-- 课程类别 -->
          <a-col :xl="8" :lg="8" :md="12" :sm="24" :xs="24">
            <a-form-item label="课程类别" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
              <a-select placeholder="请选择课程类别" v-decorator="['course_type_id', validatorRules.course_type_id]"
                @change="handleCourseTypeChange" :disabled="disabled">
                <a-select-option v-for="item in courseTypeList" :key="item.id" :value="item.id">
                  {{ item.courseTypeName }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>


          <!-- 课程状态 -->
          <a-col :xl="8" :lg="8" :md="12" :sm="24" :xs="24">
            <a-form-item label="课程状态" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
              <a-switch
                v-decorator="['course_status', { valuePropName: 'checked', rules: [{ required: true, message: '请选择课程状态!' }] }]"
                checkedChildren="已开课" unCheckedChildren="未开课" :disabled="disabled" />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- 第二行 -->
        <a-row :gutter="16">
          <!-- 课程标签 -->
          <a-col :xl="8" :lg="8" :md="12" :sm="24" :xs="24">
            <a-form-item label="课程标签" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
              <a-input placeholder="请输入课程标签" v-decorator="['course_tag', validatorRules.course_tag]"
                :disabled="disabled" />
            </a-form-item>
          </a-col>

          <!-- 负责教师 -->
          <a-col :xl="8" :lg="8" :md="12" :sm="24" :xs="24">
            <a-form-item label="负责教师" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
              <a-select placeholder="请选择负责教师" v-decorator="['teacher_id', validatorRules.teacher_id]"
                @change="handleTeacherChange" :disabled="isTeacherSelectDisabled">
                <a-select-option v-for="item in teacherList" :key="item.id" :value="item.id">
                  {{ item.realname }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>


          <!-- 总课时 -->
          <a-col :xl="8" :lg="8" :md="12" :sm="24" :xs="24">
            <a-form-item label="总课时" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
              <a-input placeholder="请输入总课时" v-decorator="['course_hours', validatorRules.course_hours]"
                :disabled="disabled" />
            </a-form-item>
          </a-col>

        </a-row>
        <!-- 操作按钮区域 -->
        <div class="form-actions">
          <a-button @click="handleCancel">{{ disabled ? '返回' : '取消' }}</a-button>
          <a-button v-if="!disabled" type="primary" @click="handleSubmit" :loading="confirmLoading">提交</a-button>
        </div>
        <!--下半部分布局-->

        <a-tabs defaultActiveKey="video" @change="handleTabsChange" class="custom-tabs" v-show="!isAddMode">
          <!-- 视频上传标签页 -->
          <a-tab-pane key="video" tab="视频">
            <div class="tab-content">
              <div class="tab-header">
                <a-button v-if="!disabled" type="primary" icon="plus" @click="showModal('video')" class="add-btn">添加视频
                </a-button>
                <div class="file-stats">
                  <span class="file-count">已上传：{{ videoFiles.length }} 个视频</span>
                </div>
              </div>
              <a-table :columns="fileColumns" :dataSource="videoFiles"
                :pagination="{ pageSize: 5, showTotal: (total) => `共 ${total} 个文件` }"
                :rowKey="record => record.id || record.uid" size="middle" class="file-table"
                :defaultSortOrder="{ columnKey: 'resourceSort', order: 'ascend' }">
                <span slot="name" slot-scope="text, record">
                  <a-icon type="video-camera" class="file-icon video-icon" />
                  {{ record.name }}
                </span>
                <span slot="action" slot-scope="text, record">
                  <a v-if="!disabled" @click="handleEditResource(record)">编辑</a>
                  <a-divider v-if="!disabled" type="vertical" />
                  <a @click="handleDetailResource(record)">详情</a>
                  <a-divider v-if="!disabled" type="vertical" />
                  <a-popconfirm v-if="!disabled" title="确定要删除此文件吗?" @confirm="() => handleDeleteResource(record)">
                    <a>删除</a>
                  </a-popconfirm>
                </span>
              </a-table>
            </div>
          </a-tab-pane>

          <!-- 讲义上传标签页 -->
          <a-tab-pane key="lecture" tab="讲义" v-show="!isAddMode">
            <div class="tab-content">
              <div class="tab-header">
                <a-button v-if="!disabled" type="primary" icon="plus" @click="showModal('lecture')" class="add-btn">添加讲义
                </a-button>
                <div class="file-stats">
                  <span class="file-count">已上传：{{ lectureFiles.length }} 个讲义</span>
                </div>
              </div>
              <a-table :columns="fileColumns" :dataSource="lectureFiles"
                :pagination="{ pageSize: 5, showTotal: (total) => `共 ${total} 个文件` }"
                :rowKey="record => record.id || record.uid" size="middle" class="file-table"
                :defaultSortOrder="{ columnKey: 'resourceSort', order: 'ascend' }">
                <span slot="name" slot-scope="text, record">
                  <a-icon type="file-pdf" class="file-icon pdf-icon" />
                  {{ record.name }}
                </span>
                <span slot="action" slot-scope="text, record">
                  <a v-if="!disabled" @click="handleEditResource(record)">编辑</a>
                  <a-divider v-if="!disabled" type="vertical" />
                  <a @click="handleDetailResource(record)">详情</a>
                  <a-divider v-if="!disabled" type="vertical" />
                  <a-popconfirm v-if="!disabled" title="确定要删除此文件吗?" @confirm="() => handleDeleteResource(record)">
                    <a>删除</a>
                  </a-popconfirm>
                </span>
              </a-table>
            </div>
          </a-tab-pane>

          <!-- 资料上传标签页 -->
          <a-tab-pane key="experiment" tab="资料" v-show="!isAddMode">
            <div class="tab-content">
              <div class="tab-header">
                <a-button v-if="!disabled" type="primary" icon="plus" @click="showModal('experiment')"
                  class="add-btn">添加资料
                </a-button>
                <div class="file-stats">
                  <span class="file-count">已上传：{{ experimentFiles.length }} 个资料</span>
                </div>
              </div>
              <a-table :columns="fileColumns" :dataSource="experimentFiles"
                :pagination="{ pageSize: 5, showTotal: (total) => `共 ${total} 个文件` }"
                :rowKey="record => record.id || record.uid" size="middle" class="file-table"
                :defaultSortOrder="{ columnKey: 'resourceSort', order: 'ascend' }">
                <span slot="name" slot-scope="text, record">
                  <a-icon type="experiment" class="file-icon experiment-icon" />
                  {{ record.name }}
                </span>
                <span slot="action" slot-scope="text, record">
                  <a v-if="!disabled" @click="handleEditResource(record)">编辑</a>
                  <a-divider v-if="!disabled" type="vertical" />
                  <a @click="handleDetailResource(record)">详情</a>
                  <a-divider v-if="!disabled" type="vertical" />
                  <a-popconfirm v-if="!disabled" title="确定要删除此文件吗?" @confirm="() => handleDeleteResource(record)">
                    <a>删除</a>
                  </a-popconfirm>
                </span>
              </a-table>
            </div>
          </a-tab-pane>

          <!-- 可以添加标签页右侧的内容 -->
          <template slot="tabBarExtraContent" v-show="!isAddMode">
            <span class="tab-extra-info">
              总文件数：{{ videoFiles.length + lectureFiles.length + experimentFiles.length }}
            </span>
          </template>
        </a-tabs>
        <cloud-computing-course-resource-modal ref="resourceModalForm" @ok="handleResourceModalOk"
          :visible.sync="resourceModalVisible">
        </cloud-computing-course-resource-modal>

      </a-form>

    </a-spin>

    <!-- 文件管理模态框 -->
    <a-modal :title="modalTitle" :visible="modalVisible" :footer="null" @cancel="hideModal" width="800px">

    </a-modal>
  </a-card>
</template>

<script>
import { deleteAction, getAction, putAction } from '@api/manage'
import JUpload from '@comp/jeecg/JUpload.vue'
import CloudComputingCourseResourceModal from './modules/CloudComputingCourseResourceModal'

export default {
  name: 'CourseEdit',
  components: {
    JUpload,
    CloudComputingCourseResourceModal
  },

  /**
   * 数据定义区域
   * 按照功能模块分组，提高可读性
   */
  data() {
    return {
      // 页面状态相关
      title: '课程信息',
      isEdit: false,
      disabled: false,
      isDetail: false,
      isAddMode: false,
      confirmLoading: false,
      fromSource: '',
      isTeacherSelectDisabled: true,

      // 表单数据相关
      model: {},
      formFields: {
        id: '',
        course_name: '',
        course_type_name: '',
        course_type_id: '',
        course_status: '',
        course_tag: '',
        teacher_id: '',
        teacher_name: '',
        course_hours: ''
      },

      // 验证规则
      validatorRules: this.getValidatorRules(),

      // 隐藏字段
      hiddenFields: {
        course_type_id: '',
        teacher_id: '',
        course_type_name: '',
        teacher_name: ''
      },

      // 下拉选项数据
      courseTypeList: [],
      teacherList: [],

      // 资源文件管理
      videoFiles: [],
      lectureFiles: [],
      experimentFiles: [],

      // 模态框状态
      modalVisible: false,
      modalTitle: '',
      currentFileType: '',
      currentFiles: [],
      resourceModalVisible: false,
      currentResource: null,

      // 表格列定义
      fileColumns: this.getFileColumns(),

      // API接口配置
      url: {
        get: '/study/cloudComputingCourse/list',
        courseTypeList: '/study/cloudComputingCourseType/list',
        //teacherList: '/study/cloudComputingCourse/queryById/teacher',
        teacherList: '/sys/dict/getDictItems/sys_user,realname,id',
        getCourseResource: '/study/cloudComputingCourseResource/list',
        save: '/study/cloudComputingCourse/edit',
        upload: '/study/cloudComputingCourse/edit',
        deleteResource: '/study/cloudComputingCourseResource/delete'
      },

      // 布局配置
      labelCol: {
        xs: { span: 24 },
        sm: { span: 6 }
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 18 }
      }
    }
  },

  /**
   * 组件创建前钩子
   * 初始化表单对象
   */
  beforeCreate() {
    this.form = this.$form.createForm(this)
  },

  /**
   * 组件创建完成钩子
   * 初始化组件数据，根据路由参数判断操作模式
   */
  created() {
    this.initializeComponent()
  },

  computed: {
    /**
     * 计算总文件数（响应式）
     */
    totalFilesCount() {
      return this.videoFiles.length + this.lectureFiles.length + this.experimentFiles.length
    }
  },

  methods: {
    /**
     * 初始化组件
     * 根据路由参数设置组件状态和加载数据
     */
    initializeComponent() {
      const { id, mode, from } = this.$route.query

      // 设置组件模式状态
      this.setComponentMode(mode, from)

      // 加载基础数据
      this.loadBasicData()

      // 根据ID加载课程数据或初始化添加模式
      if (id) {
        this.loadCourseData(id)
      } else {
        this.initAddMode()
      }
    },

    /**
     * 设置组件模式
     * @param {string} mode - 模式类型
     * @param {string} from - 来源标识
     */
    setComponentMode(mode, from) {
      this.isDetail = mode === 'readonly'
      this.isAddMode = mode === 'add'
      this.fromSource = from || ''

      // 设置禁用状态
      this.disabled = this.isDetail
      this.isTeacherSelectDisabled = this.isDetail || from === 'teacher'

      // 设置标题
      this.title = this.getTitleByMode(mode)
    },

    /**
     * 根据模式获取标题
     * @param {string} mode - 模式类型
     * @returns {string} 标题文本
     */
    getTitleByMode(mode) {
      const titleMap = {
        'readonly': '课程详情',
        'add': '添加课程',
        'edit': '编辑课程'
      }
      return titleMap[mode] || '课程信息'
    },

    /**
     * 加载基础数据
     * 并行加载课程类型和教师列表
     */
    loadBasicData() {
      Promise.all([
        this.loadCourseTypeList(),
        this.loadTeacherList()
      ]).catch(error => {
        console.error('加载基础数据失败:', error)
        this.$message.error('加载基础数据失败，请检查网络连接')
      })
    },

    /**
     * ================================
     * 数据加载相关方法
     * ================================
     */

    /**
     * 加载课程数据
     * @param {string} courseId - 课程ID
     */
    loadCourseData(courseId) {
      if (!courseId) {
        this.$message.warning('课程ID不能为空')
        this.handleCancel()
        return
      }

      this.confirmLoading = true

      getAction(this.url.get, { id: courseId })
        .then(res => {
          if (this.validateApiResponse(res)) {
            const courseData = res.result.records[0]
            this.handleCourseDataLoaded(courseData)
          } else {
            this.$message.warning('未找到课程数据')
            this.handleCancel()
          }
        })
        .catch(this.handleApiError('查询课程数据'))
        .finally(() => {
          this.confirmLoading = false
        })
    },

    /**
     * 处理课程数据加载完成
     * @param {Object} courseData - 课程数据
     */
    handleCourseDataLoaded(courseData) {
      this.model = Object.assign({}, courseData)

      // 更新标题
      if (!this.isDetail) {
        this.title = `编辑课程: ${courseData.courseName}`
      }

      // 设置隐藏字段
      this.hiddenFields.course_type_id = courseData.courseTypeId || ''
      this.hiddenFields.teacher_id = courseData.teacherId || ''

      // 延迟填充表单数据
      this.$nextTick(() => {
        setTimeout(() => {
          this.populateForm(courseData)
        }, 100)
      })
    },

    /**
     * 加载课程类型列表
     */
    loadCourseTypeList() {
      return new Promise((resolve, reject) => {
        getAction(this.url.courseTypeList, {})
          .then(res => {
            if (res.success && res.result && res.result.records) {
              this.courseTypeList = res.result.records
              resolve(res.result.records)
            } else {
              this.$message.warning('获取课程类别列表为空')
              resolve([])
            }
          })
          .catch(error => {
            console.error('加载课程类别列表失败:', error)
            reject(error)
          })
      })
    },

    /**
     * 加载教师列表
     */
    loadTeacherList() {
      return new Promise((resolve, reject) => {
        getAction(this.url.teacherList)
          .then(res => {
            if (res.success && res.result) {
              this.teacherList = res.result.map(item => ({
                id: item.value,
                realname: item.label
              }))
              resolve(this.teacherList)
            } else {
              this.$message.warning('获取教师列表为空')
              resolve([])
            }
          })
          .catch(error => {
            console.error('加载教师列表失败:', error)
            reject(error)
          })
      })
    },

    /**
     * 加载课程资源
     * @param {string} courseId - 课程ID
     */
    loadCourseResources(courseId) {
      if (!courseId) {
        this.$message.warning('课程ID不能为空，无法加载资源')
        return
      }

      getAction(this.url.getCourseResource, { courseId: courseId })
        .then(res => {
          if (res.success && res.result && res.result.records) {
            this.classifyResources(res.result.records)
          } else {
            console.log('没有找到课程资源或返回数据格式不正确')
          }
        })
        .catch(this.handleApiError('加载课程资源'))
    },

    /**
     * 分类资源数据
     * @param {Array} resources - 资源列表
     */
    classifyResources(resources) {
      // 清空现有数据
      this.videoFiles = []
      this.lectureFiles = []
      this.experimentFiles = []

      // 资源类型映射
      const resourceTypeMap = {
        1: 'videoFiles',
        2: 'lectureFiles',
        3: 'experimentFiles'
      }

      resources.forEach(resource => {
        const fullResource = this.normalizeResourceData(resource)
        const targetArray = resourceTypeMap[resource.resourceType]

        if (targetArray && this[targetArray]) {
          this[targetArray].push(fullResource)
        } else {
          console.warn('未知资源类型:', resource.resourceType)
        }
      })


    },

    /**
     * ================================
     * 表单处理相关方法
     * ================================
     */

    /**
     * 初始化添加模式
     */
    initAddMode() {
      this.model = {}
      this.clearResourceArrays()

      this.$nextTick(() => {
        if (this.form) {
          this.form.setFieldsValue({
            course_type_name: '',
            course_status: false,
            course_name: '',
            course_tag: '',
            course_hours: ''
          })
        }
      })
    },

    /**
     * 填充表单数据
     * @param {Object} model - 课程数据模型
     */
    populateForm(model) {
      setTimeout(() => {
        const formFields = {
          course_name: model.courseName || '',
          course_type_id: model.courseTypeId || '',
          course_status: model.courseStatus !== undefined ? (model.courseStatus === 1) : false,
          course_tag: model.courseTag || '',
          teacher_id: model.teacherId || '',
          course_hours: model.courseHours || ''
        }

        if (this.form) {
          this.form.setFieldsValue(formFields)
        } else {
          console.error('表单对象未初始化')
        }

        // 加载相关资源数据
        if (model.id) {
          this.loadCourseResources(model.id)
        }
      }, 200)
    },

    /**
     * 表单提交处理
     */
    handleSubmit() {
      this.form.validateFields((err, values) => {
        if (err) {
          this.$message.warning('表单验证失败，请检查输入')
          return
        }

        this.confirmLoading = true

        const formData = this.buildSubmitData(values)


        putAction(this.url.upload, formData)
          .then(res => {
            if (res.success) {
              this.handleSubmitSuccess()
            } else {
              this.$message.error(res.message || '保存失败')
            }
          })
          .catch(this.handleApiError('提交数据'))
          .finally(() => {
            this.confirmLoading = false
          })
      })
    },

    /**
     * 构建提交数据
     * @param {Object} values - 表单值
     * @returns {Object} 提交数据对象
     */
    buildSubmitData(values) {
      const formData = {
        id: this.model.id,
        courseName: values.course_name,
        courseTypeId: values.course_type_id,
        courseTag: values.course_tag,
        teacherId: values.teacher_id,
        courseHours: values.course_hours,
        courseStatus: values.course_status ? 1 : 0,
        resources: this.getAllResourcesData(this.model.id)
      }

      return formData
    },

    /**
     * 处理提交成功
     */
    handleSubmitSuccess() {
      const message = this.isEdit ? '课程更新成功' : '课程添加成功'
      this.$message.success(message)

      const returnPath = this.getReturnPath()
      this.$router.push({
        path: returnPath,
        query: { refresh: 'true' }
      })
    },

    /**
     * ================================
     * 资源管理相关方法
     * ================================
     */

    /**
     * 显示资源模态框
     * @param {string} type - 资源类型
     */
    showModal(type) {
      this.resourceModalVisible = true

      this.$nextTick(() => {
        if (this.$refs.resourceModalForm && this.$refs.resourceModalForm.add) {
          const defaults = this.getResourceModalDefaults(type)
          this.$refs.resourceModalForm.add(defaults)
        } else {
          this.$message.error('资源编辑组件未正确加载')
        }
      })
    },

    /**
     * 编辑资源
     * @param {Object} record - 资源记录
     */
    handleEditResource(record) {
      this.openResourceModal(record, false)
    },

    /**
     * 查看资源详情
     * @param {Object} record - 资源记录
     */
    handleDetailResource(record) {
      this.openResourceModal(record, true)
    },

    /**
     * 打开资源模态框通用方法
     * @param {Object} record - 资源记录
     * @param {boolean} isDetail - 是否为详情模式
     */
    openResourceModal(record, isDetail) {
      this.resourceModalVisible = true

      this.$nextTick(() => {
        const method = isDetail ? 'detail' : 'edit'
        if (this.$refs.resourceModalForm && this.$refs.resourceModalForm[method]) {
          this.$refs.resourceModalForm[method](record, isDetail)
        } else {
          this.$message.error('资源编辑组件未正确加载')
        }
      })
    },

    /**
     * 处理资源模态框确认
     */
    handleResourceModalOk() {
      this.resourceModalVisible = false
      this.clearReadOnlySettings()

      if (this.model.id) {
        this.loadCourseResources(this.model.id)
      }
    },

    /**
     * 删除资源
     * @param {Object} record - 资源记录
     */
    handleDeleteResource(record) {
      this.$confirm({
        title: '确认删除',
        content: `确定要删除资源 "${record.name}" 吗？此操作不可恢复！`,
        onOk: () => {
          this.executeDeleteResource(record)
        }
      })
    },

    /**
     * 执行删除资源操作
     * @param {Object} record - 资源记录
     */
    executeDeleteResource(record) {
      if (!record || !record.id) {
        this.$message.error('资源ID无效，无法删除')
        return
      }

      deleteAction(this.url.deleteResource, { id: record.id })
        .then(res => {
          if (res.success) {
            this.$message.success('删除成功')
            this.removeResourceFromList(record)
          } else {
            this.$message.error(res.message || '删除失败')
          }
        })
        .catch(this.handleApiError('删除资源'))
    },

    /**
     * ================================
     * 工具方法
     * ================================
     */

    /**
     * 获取返回路径
     * @returns {string} 返回路径
     */
    getReturnPath() {
      const pathMap = {
        'teacher': '/study/PreparationCenterTeacher',
        'default': '/study/PreparationCenter'
      }
      return pathMap[this.fromSource] || pathMap.default
    },

    /**
     * 取消操作
     */
    handleCancel() {
      const returnPath = this.getReturnPath()
      this.$router.push({ path: returnPath })
    },

    /**
     * 课程类型变化处理
     * @param {string} value - 选中的课程类型ID
     */
    handleCourseTypeChange(value) {
      const selectedType = this.courseTypeList.find(item => item.id === value)
      this.hiddenFields.course_type_name = selectedType ? selectedType.courseTypeName : ''
    },

    /**
     * 教师变化处理
     * @param {string} value - 选中的教师ID
     */
    handleTeacherChange(value) {
      const selectedTeacher = this.teacherList.find(item => item.id === value)
      this.hiddenFields.teacher_name = selectedTeacher ? selectedTeacher.realname : ''
    },

    /**
     * 标签页切换处理
     * @param {string} activeKey - 激活的标签页key
     */
    handleTabsChange(activeKey) {

    },

    /**
     * 隐藏模态框
     */
    hideModal() {
      this.modalVisible = false
      this.currentFileType = ''
      this.currentFiles = []
    },

    /**
     * 清除只读设置
     */
    clearReadOnlySettings() {
      if (this.$refs.resourceModalForm) {
        this.$refs.resourceModalForm.isDetail = false
        this.$refs.resourceModalForm.disabled = false
      }
    },

    /**
     * 从资源列表中移除记录
     * @param {Object} record - 要移除的资源记录
     */
    removeResourceFromList(record) {
      const resourceTypeMap = {
        1: 'videoFiles',
        2: 'lectureFiles',
        3: 'experimentFiles'
      }

      const targetArray = resourceTypeMap[record.resourceType]
      if (targetArray && this[targetArray]) {
        const index = this[targetArray].findIndex(item => item.id === record.id)
        if (index !== -1) {
          this[targetArray].splice(index, 1)
        }
      }
    },

    /**
     * 清空资源数组
     */
    clearResourceArrays() {
      this.videoFiles = []
      this.lectureFiles = []
      this.experimentFiles = []
    },

    /**
     * 获取所有资源数据
     * @param {string} courseId - 课程ID
     * @returns {Array} 资源数据数组
     */
    getAllResourcesData(courseId) {
      const allResources = [
        ...this.videoFiles.map(file => this.createResourceData(file, 1, courseId)),
        ...this.lectureFiles.map(file => this.createResourceData(file, 2, courseId)),
        ...this.experimentFiles.map(file => this.createResourceData(file, 3, courseId))
      ]

      return allResources
    },

    /**
     * 创建资源数据对象
     * @param {Object} file - 文件数据
     * @param {number} resourceType - 资源类型
     * @param {string} courseId - 课程ID
     * @returns {Object} 标准化的资源数据
     */
    createResourceData(file, resourceType, courseId) {
      return {
        id: file.id || file.resourceId,
        resourceId: file.id || file.resourceId,
        resourceName: file.name || file.resourceName,
        resourceType: resourceType,
        resourceUrl: file.resourceUrl || file.url,
        resourceSize: file.size || file.resourceSize || 0,
        courseId: courseId
      }
    },

    /**
     * 标准化资源数据
     * @param {Object} resource - 原始资源数据
     * @returns {Object} 标准化后的资源数据
     */
    normalizeResourceData(resource) {
      return {
        ...resource,
        id: resource.id,
        name: resource.resourceName,
        resourceUrl: resource.resourceUrl
      }
    },

    /**
     * 获取资源模态框默认值
     * @param {string} type - 资源类型
     * @returns {Object} 默认值对象
     */
    getResourceModalDefaults(type) {
      const resourceTypeMap = {
        'video': 1,
        'lecture': 2,
        'experiment': 3
      }

      return {
        courseName: this.form.getFieldValue('course_name') || '',
        courseId: this.model.id || '',
        resourceType: resourceTypeMap[type] || 1
      }
    },

    /**
     * API响应验证
     * @param {Object} res - API响应
     * @returns {boolean} 是否有效响应
     */
    validateApiResponse(res) {
      return res.success && res.result && res.result.records && res.result.records.length > 0
    },

    /**
     * API错误处理工厂函数
     * @param {string} operation - 操作名称
     * @returns {Function} 错误处理函数
     */
    handleApiError(operation) {
      return (error) => {
        console.error(`${operation}失败:`, error)
        this.$message.error(`${operation}失败: ${error.message || '网络异常，请稍后重试'}`)
      }
    },

    /**
     * 获取验证规则配置
     * @returns {Object} 验证规则对象
     */
    getValidatorRules() {
      return {
        course_name: {
          rules: [{ required: true, message: '请输入课程名称!' }]
        },
        course_type_id: {
          rules: [{ required: true, message: '请选择课程类别!' }]
        },
        course_tag: {
          rules: [{ required: false, message: '请输入课程标签!' }]
        },
        teacher_id: {
          rules: [{ required: true, message: '请选择负责教师!' }]
        },
        course_hours: {
          rules: [
            { required: true, message: '请输入总课时!' },
            { pattern: /^\d+$/, message: '课时必须为数字!' }
          ]
        },
        course_status: {
          rules: [{ required: true, message: '请选择课程状态!' }]
        }
      }
    },

    /**
     * 获取文件列定义
     * @returns {Array} 表格列配置数组
     */
    getFileColumns() {
      return [
        {
          title: '排序',
          dataIndex: 'resourceSort',
          key: 'resourceSort',
          sorter: (a, b) => a.resourceSort - b.resourceSort,
          sortDirections: ['ascend', 'descend'],
          defaultSortOrder: 'ascend'
        },
        {
          title: '文件名称',
          dataIndex: 'name',
          key: 'name',
          scopedSlots: { customRender: 'name' }
        },
        {
          title: '资源存储地址',
          dataIndex: 'resourceUrl',
          key: 'resourceUrl',
          customRender: (text, record) => {

            return record.resourceUrl || '无'
          }
        },
        {
          title: '课程名称',
          dataIndex: 'courseId_dictText',
          key: 'courseId_dictText'
        },
        {
          title: '操作',
          key: 'action',
          scopedSlots: { customRender: 'action' }
        }
      ]
    }
  }
}
</script>

<style lang="less" scoped>
.tab-extra-info {
  color: rgba(0, 0, 0, 0.45);
  font-size: 14px;
}

/* 修改上传区域样式，适应标签页 */
.upload-section {
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  padding: 16px;
  background-color: #fafafa;
  margin-top: 16px;
}

.card-title {
  margin-bottom: 24px;
}

.form-actions {
  margin-top: 24px;
  text-align: right;

  button {
    margin-left: 8px;
  }
}

/* 修改上传区域样式，适应标签页 */
.upload-section {
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  padding: 16px;
  background-color: #fafafa;
  margin-top: 16px;
}

.card-title {
  margin-bottom: 24px;
}

.form-actions {
  margin-top: 24px;
  text-align: right;

  button {
    margin-left: 8px;
  }
}

.upload-section {
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  padding: 16px;
  background-color: #fafafa;
}

.upload-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;

  .upload-count {
    color: rgba(0, 0, 0, 0.65);
    line-height: 32px;
  }
}

/* 在现有样式中添加以下内容 */
.custom-tabs {
  margin-top: 20px;
}

.custom-tabs /deep/ .ant-tabs-bar {
  margin-bottom: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.custom-tabs /deep/ .ant-tabs-tab {
  font-size: 14px;
  padding: 12px 20px;
}

.tab-content {
  padding: 0 16px;
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.add-btn {
  border-radius: 4px;
}

.file-stats {
  display: flex;
  align-items: center;
  gap: 16px;
}

.file-count {
  color: #595959;
  font-size: 14px;
}

.file-table {
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  overflow: hidden;
}

.file-table /deep/ .ant-table-thead>tr>th {
  background-color: #fafafa;
  font-weight: 500;
}

.file-icon {
  margin-right: 8px;
  font-size: 16px;
}

.video-icon {
  color: #1890ff;
}

.pdf-icon {
  color: #f5222d;
}

.experiment-icon {
  color: #52c41a;
}

.resource-icon {
  color: #faad14;
}

.table-btn {
  padding: 0 4px;
  height: auto;
  line-height: 1;
}

.delete-btn {
  color: #ff4d4f;
}

.tab-extra-info {
  color: rgba(0, 0, 0, 0.45);
  font-size: 14px;
  font-weight: 500;
}

/* 响应式处理 */
@media (max-width: 768px) {
  .tab-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .file-stats {
    width: 100%;
    justify-content: space-between;
  }

  .file-table /deep/ .ant-table {
    font-size: 12px;
  }
}
</style>