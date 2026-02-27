<template>
  <div>
    <a-card :bordered="false">
      <!-- 搜索区域 -->
      <a-tabs defaultActiveKey="all" @change="handleTabChange" class="table-page-search-wrapper">
        <a-tab-pane key="all" tab="全部"></a-tab-pane>
        <a-tab-pane v-for="level in courseLevels" :key="level.key" :tab="level.name"></a-tab-pane>

        <template slot="tabBarExtraContent">
          <a-input-search placeholder="课程名称" v-model="queryParam.courseName" style="width: 200px; margin-right: 8px"
            @search="onSearch" @change="onSearchChange" allowClear />
        </template>
      </a-tabs>

      <!-- 操作按钮区域 -->
      <div class="table-operator">
        <a-button type="primary" icon="plus" @click="handleAdd">添加</a-button>
      </div>

      <!-- 课程列表 -->
      <a-list :dataSource="dataSource" :loading="{ spinning: loading, delay: 300 }" item-layout="horizontal"
        :grid="{ gutter: 16, column: 1 }">

        <a-list-item slot="renderItem" slot-scope="item">
          <a-card :hoverable="true">
            <!-- 卡片顶部区域 -->
            <a-row type="flex" justify="space-between" align="middle">
              <a-col :xs="24" :sm="12">
                <div class="clickable-title single-line-ellipsis" @click="handleView(item)">
                  {{ item.courseName }}
                </div>
              </a-col>

              <a-col :xs="24" :sm="12" style="text-align: right">
                <a-space>
                  <a-button type="link" @click="handleEdit(item)">编辑</a-button>
                  <a-popconfirm title="确定要删除该课程吗？" @confirm="handleDelete(item)" okText="确定" cancelText="取消">
                    <a-button type="link" style="color: #ff4d4f">删除</a-button>
                  </a-popconfirm>
                  <a-divider type="vertical" />
                  <!-- <a-button type="link" @click="handleClassManage(item)">班级管理</a-button> -->
                  <a-divider type="vertical" />

                  <a-switch :checked="item.isOpen" checkedChildren="已开课" unCheckedChildren="未开课"
                    @change="(checked) => handleOpenStatusChange(checked, item)" />
                </a-space>
              </a-col>
            </a-row>

            <!-- 卡片底部：课程统计信息 -->
            <div class="course-info-container">
              <div class="course-info-row">
                <div class="course-info-item">
                  <span class="info-label">课程类别：</span>
                  <span class="info-value">{{ item.courseTypeName || '未分类' }}</span>
                </div>
                <div class="course-info-item">
                  <span class="info-label">课程标签：</span>
                  <span class="info-value">{{ item.courseTag || '无' }}</span>
                </div>
                <div class="course-info-item">
                  <span class="info-label">负责教师：</span>
                  <span class="info-value">{{ item.teacherName || '未指定' }}</span>
                </div>
                <div class="course-info-item">
                  <span class="info-label">总课时：</span>
                  <span class="info-value">{{ item.courseHours || '0' }}</span>
                </div>
              </div>

              <div class="course-card-stats-container">
                <div class="course-card-stat">
                  <span class="stat-label">视频：</span>
                  <span class="stat-value">{{ item.videoCount }}</span>
                </div>
                <div class="course-card-stat">
                  <span class="stat-label">讲义：</span>
                  <span class="stat-value">{{ item.lectureCount }}</span>
                </div>
                <div class="course-card-stat">
                  <span class="stat-label">资源：</span>
                  <span class="stat-value">{{ item.resourceCount }}</span>
                </div>
              </div>
            </div>
          </a-card>
        </a-list-item>

        <!-- 分页组件 -->
        <div class="ant-list-pagination" slot="footer">
          <a-pagination showSizeChanger showQuickJumper :current="ipagination.current" :pageSize="ipagination.pageSize"
            :total="ipagination.total" :pageSizeOptions="ipagination.pageSizeOptions"
            :showTotal="(total, range) => `共 ${total} 条记录 第 ${range[0]}-${range[1]} 条`" @change="handlePaginationChange"
            @showSizeChange="handlePaginationSizeChange" />
        </div>
      </a-list>

      <cloud-computing-course-modal ref="courseModal" @ok="handleCourseModalOk"></cloud-computing-course-modal>
    </a-card>
  </div>
</template>

<script>
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import { deleteAction, getAction, postAction, putAction } from '@/api/manage'
import CloudComputingCourseModal from './modules/CloudComputingCourseModal'

export default {
  name: 'PreparationCenter',
  mixins: [JeecgListMixin],
  components: {
    CloudComputingCourseModal
  },

  data() {
    return {
      description: '课程准备中心',
      disableMixinCreated: true,

      // 分类数据
      courseLevels: [],

      // 表格列配置 (用于兼容JeecgListMixin)
      columns: [],

      // 查询参数
      queryParam: {
        courseName: ''
      },

      // 数据源
      dataSource: [],

      // 接口地址配置
      url: {
        list: '/study/cloudComputingCourse/list',
        edit: '/study/cloudComputingCourse/edit',
        queryById: '/study/cloudComputingCourse/queryById',
        counts: '/study/cloudComputingCourseResource/counts',
        courseTypeList: '/study/cloudComputingCourseType/list',
        delete: '/study/cloudComputingCourse/delete',
        updateStatus: '/study/cloudComputingCourse/editCourseStatus',
      },

      // 当前选中的课程级别/分类
      currentLevel: 'all',

      // 列表请求令牌，防止并发覆盖
      listRequestToken: 0,

      // 排序参数
      isorter: {
        column: 'createTime',
        order: 'desc'
      },

      modalVisible: false
    }
  },

  /**
   * 组件创建完成钩子
   * 初始化数据和分类
   */
  created() {
    this.initializeComponent()
  },
  watch: {
    /**
     * 路由变化监听
     * 处理从编辑页面返回时的数据刷新
     */
    '$route'(to, from) {
      if (to.query.refresh === 'true') {
        this.loadData(1)
        this.$nextTick(() => {
          this.$router.replace({
            query: {
              ...to.query,
              refresh: undefined
            }
          })
        })
      }
    }
  },

  methods: {
    /**
     * 初始化组件
     */
    initializeComponent() {
      this.loadData(1)
      this.loadCourseLevels()
    },

    /**
     * 加载课程级别/分类
     */
    loadCourseLevels() {
      getAction(this.url.courseTypeList, { pageNo: 1, pageSize: 9999 })
        .then(res => {
          if (res && res.success) {
            const result = res.result
            const list = result && Array.isArray(result.records)
              ? result.records
              : (Array.isArray(result) ? result : [])

            this.courseLevels = list.map(item => ({
              key: item.id,
              name: item.courseTypeName
            }))
          } else {
            this.courseLevels = []
          }
        })
        .catch(() => {
          this.courseLevels = []
        })
    },

    /**
     * 加载课程列表数据
     * @param {number} arg - 如果为1则重置页码
     */
    loadData(arg) {
      if (arg === 1) {
        this.ipagination.current = 1
      }

      const token = Date.now()
      this.listRequestToken = token
      this.loading = true

      getAction(this.url.list, this.getQueryParams())
        .then(res => {
          if (this.validateListResponse(res)) {
            this.processListData(res.result, token)
          } else {
            this.handleEmptyList()
          }
        })
        .catch(() => {
          this.handleEmptyList()
        })
        .finally(() => {
          this.loading = false
        })
    },

    /**
     * 验证列表响应数据
     * @param {Object} res - API响应
     * @returns {boolean} 是否有效
     */
    validateListResponse(res) {
      return res && res.success
    },

    /**
     * 处理列表数据
     * @param {Object} result - 响应结果
     * @param {number} token - 请求令牌
     */
    processListData(result, token) {
      let records = [], total = 0

      // 适配分页/非分页格式
      if (result && Array.isArray(result.records)) {
        records = result.records
        total = Number(result.total) || records.length
      } else if (Array.isArray(result)) {
        records = result
        total = records.length
      }

      // 标准化课程数据
      this.dataSource = records.map(item => this.normalizeCourseData(item))
      this.ipagination.total = total

      // 加载资源统计
      this.fetchResourceCountsForList(this.dataSource, token)
    },

    /**
     * 处理空列表情况
     */
    handleEmptyList() {
      this.dataSource = []
      this.ipagination.total = 0
    },

    /**
     * 标准化课程数据
     * @param {Object} item - 原始课程数据
     * @returns {Object} 标准化后的数据
     */
    normalizeCourseData(item) {
      return {
        id: item.id,
        courseName: item.courseName,
        courseTypeId: item.courseTypeId,
        courseTypeName: item.courseTypeId_dictText || item.courseTypeName || '',
        courseTag: item.courseTag || '',
        teacherId: item.teacherId || '',
        teacherName: item.teacherId_dictText || item.teacherName || '',
        courseHours: item.courseHours || 0,
        videoCount: item.videoCount != null ? item.videoCount : 0,
        lectureCount: item.lectureCount != null ? item.lectureCount : 0,
        experimentCount: item.experimentCount != null ? item.experimentCount : 0,
        resourceCount: item.resourceCount != null ? item.resourceCount : 0,
        isOpen: item.courseStatus == 1 ? true : false
      }
    },

    /**
     * 为课程列表获取资源统计数据
     * @param {Array} list - 课程列表
     * @param {number} token - 请求令牌
     */
    fetchResourceCountsForList(list, token) {
      if (!Array.isArray(list) || list.length === 0) {
        return Promise.resolve()
      }

      const resourceTypes = {
        '1': 'videoCount',
        '2': 'lectureCount',
        '3': 'resourceCount'
      }

      Promise.all(
        list.map(course =>
          getAction(this.url.counts, { id: course.id })
            .then(res => ({ id: course.id, res }))
            .catch(() => ({ id: course.id, res: null }))
        )
      ).then(results => {
        if (token !== this.listRequestToken) return

        results.forEach(({ id, res }) => {
          if (!res || !res.success || !res.result) return

          const counts = this.calculateResourceCounts(res.result, resourceTypes)
          this.updateCourseResourceCounts(id, counts)
        })
      })
    },

    /**
     * 计算资源数量
     * @param {Object} result - 资源统计结果
     * @param {Object} resourceTypes - 资源类型映射
     * @returns {Object} 资源计数对象
     */
    calculateResourceCounts(result, resourceTypes) {
      const counts = { videoCount: 0, lectureCount: 0, resourceCount: 0 }

      Object.keys(result).forEach(typeId => {
        const count = Number(result[typeId]) || 0
        const field = resourceTypes[typeId] || 'resourceCount'
        counts[field] += count
      })

      return counts
    },

    /**
     * 更新课程资源计数
     * @param {string} courseId - 课程ID
     * @param {Object} counts - 资源计数
     */
    updateCourseResourceCounts(courseId, counts) {
      const index = this.dataSource.findIndex(x => x.id === courseId)
      if (index > -1) {
        const updatedCourse = { ...this.dataSource[index], ...counts }
        this.$set(this.dataSource, index, updatedCourse)
      }
    },

    /**
     * 切换课程开课状态
     * @param {boolean} checked - 是否开课
     * @param {Object} record - 课程记录
     */
    handleOpenStatusChange(checked, record) {
      const prevStatus = checked
      let status = 0;
      if (prevStatus) {
        status = 1;
      }
      this.updateCourseStatus(record.id, status);
      this.initializeComponent()
    },

    /**
     * 更新课程状态（优化版本）
     * @param {string} courseId - 课程ID
     * @param {boolean} status - 课程状态
     */
    updateCourseStatus(courseId, status) {
      postAction("/study/cloudComputingCourse/editCourseStatus/" + courseId + "/" + status, {}).then(res => {
        if (!res || !res.success) {
          throw new Error(res.message || '更新状态失败')
        }
        return res
      })
    },

    /**
     * 删除课程
     * @param {Object} record - 课程记录
     */
    handleDelete(record) {
      this.$confirm({
        title: '确认删除',
        content: `确定要删除课程"${record.courseName}"吗？此操作将同时删除该课程下及资源文件，且不可恢复！`,
        onOk: () => {
          this.executeDeleteCourse(record)
        }
      })
    },

    /**
     * 执行删除课程操作
     * @param {Object} record - 课程记录
     */
    executeDeleteCourse(record) {
      this.loading = true

      deleteAction('/study/cloudComputingCourse/deleteCourse', { courseId: record.id })
        .then(res => {
          if (res.success) {
            this.$message.success('删除成功')
            this.removeCourseFromList(record.id)
          } else {
            this.$message.error(res.message || '删除失败')
          }
        })
        .catch(error => {
          console.error('删除失败:', error)
          this.$message.error('删除失败: ' + (error.message || '网络异常'))
        })
        .finally(() => {
          this.loading = false
        })
    },

    /**
     * 从列表中移除课程
     * @param {string} courseId - 课程ID
     */
    removeCourseFromList(courseId) {
      const index = this.dataSource.findIndex(item => item.id === courseId)
      if (index > -1) {
        this.dataSource.splice(index, 1)
        this.ipagination.total -= 1
      }
    },

    /**
     * 路由跳转
     * @param {string} path - 路由路径
     * @param {Object} query - 查询参数
     */
    navigateTo(path, query = {}) {
      this.$router.push({ path, query })
    },

    /**
     * 查看课程详情
     * @param {Object} record - 课程记录
     */
    handleView(record) {
      this.navigateTo('/study/EditorPreparationCenter', {
        id: record.id,
        mode: 'readonly',
        from: 'admin'
      })
    },

    /**
     * 编辑课程
     * @param {Object} record - 课程记录
     */
    handleEdit(record) {
      this.navigateTo('/study/EditorPreparationCenter', {
        id: record.id,
        from: 'admin'
      })
    },

    /**
     * 班级管理
     * @param {Object} record - 课程记录
     */
    // handleClassManage(record) {
    //   this.navigateTo('/cloud/CloudComputingCourseClassList', { id: record.id })
    // },


    /**
     * 标签切换处理
     * @param {string} key - 标签key
     */
    handleTabChange(key) {
      this.currentLevel = key

      if (key === 'all') {
        delete this.queryParam.courseTypeId
      } else {
        this.queryParam.courseTypeId = key
      }

      this.loadData(1)
    },

    /**
     * 搜索框变化处理
     * @param {Object} e - 事件对象
     */
    onSearchChange(e) {
      if (!e.target.value) {
        this.$nextTick(() => {
          this.loadData(1)
        })
      }
    },

    /**
     * 搜索处理
     * @param {string} value - 搜索值
     */
    onSearch(value) {
      this.queryParam.courseName = value
      this.loadData(1)
    },

    /**
     * 搜索查询
     */
    searchQuery() {
      this.loadData(1)
    },

    /**
     * 搜索重置
     */
    searchReset() {
      const keepTypeId = this.queryParam.courseTypeId
      this.queryParam = { courseName: '' }

      if (keepTypeId) {
        this.queryParam.courseTypeId = keepTypeId
      }

      this.loadData(1)
    },

    /**
     * 分页变化处理
     * @param {number} page - 页码
     */
    handlePaginationChange(page) {
      this.ipagination.current = page
      this.loadData()
    },

    /**
     * 分页大小变化处理
     * @param {number} current - 当前页码
     * @param {number} size - 每页大小
     */
    handlePaginationSizeChange(current, size) {
      this.ipagination.pageSize = size
      this.ipagination.current = 1
      this.loadData(1)
    },

    /**
     * 添加课程
     */
    handleAdd() {
      this.$refs.courseModal.add()
    },

    /**
     * 课程模态框确认回调
     */
    handleCourseModalOk() {
      this.loadData(1)
    },

    /**
     * 获取查询参数
     * @returns {Object} 查询参数
     */
    getQueryParams() {
      const param = {
        pageNo: this.ipagination.current,
        pageSize: this.ipagination.pageSize,
        column: (this.isorter && this.isorter.column) || 'createTime',
        order: (this.isorter && this.isorter.order) || 'desc',
        _t: Date.now()
      }

      if (this.queryParam) {
        const courseName = this.queryParam.courseName && this.queryParam.courseName.trim()
        if (courseName) {
          param.courseName = '*' + courseName + '*'
        }

        if (this.queryParam.courseTypeId) {
          param.courseTypeId = this.queryParam.courseTypeId
        }
      }

      return param
    }
  }
}
</script>

<style lang="less" scoped>
.table-operator {
  margin-bottom: 16px;
}

.ant-list-pagination {
  margin-top: 16px;
  text-align: right;
}

.clickable-title {
  cursor: pointer;
  margin-bottom: 0;
  font-size: 20px;
  font-weight: 500;

  &:hover {
    color: #40a9ff;
  }
}

.single-line-ellipsis {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.course-info-container {
  margin-top: 16px;
}

.course-info-row {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px dashed #f0f0f0;
}

.course-info-item {
  display: flex;
  align-items: center;
  margin-right: 24px;
  margin-bottom: 8px;
  min-width: 120px;
  flex: 1;

  .info-label {
    color: rgba(0, 0, 0, 0.65);
    font-size: 14px;
    margin-right: 8px;
    min-width: 70px;
  }

  .info-value {
    font-weight: 500;
    font-size: 14px;
    color: rgba(0, 0, 0, 0.85);
  }
}

.course-card-stats-container {
  display: flex;
  flex-wrap: wrap;
  padding-top: 8px;
}

.course-card-stat {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-right: 24px;
  margin-bottom: 8px;

  .stat-label {
    color: rgba(0, 0, 0, 0.65);
    font-size: 14px;
    margin-right: 4px;
  }

  .stat-value {
    font-weight: 500;
    font-size: 14px;
    color: #1890ff;
  }
}
</style>