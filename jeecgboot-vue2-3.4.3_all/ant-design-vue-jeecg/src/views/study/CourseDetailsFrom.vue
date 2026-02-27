<template>
  <!-- 课程详情页面主容器 -->
  <div class="course-container">
    <!-- ========== 顶部导航栏 ========== -->
    <div class="header">
      <!-- 返回按钮 -->
      <a-icon class="back-icon" type="left-circle" theme="filled" @click="handleBack" />
      <!-- 课程标题 - 显示动态课程名称 -->
      <h1 class="course-title-text">
        {{ courseDetails.course_name || '课程详情' }}
      </h1>
      <!-- 前进按钮 -->
      <!-- <a-icon class="forward-icon" type="right-circle" theme="filled" @click="handleForward" /> -->
    </div>

    <!-- ========== 加载状态 ========== -->
    <div v-if="isLoading" class="loading-container">
      <a-spin size="large" />
      <p>加载中...</p>
    </div>

    <!-- ========== 错误状态 ========== -->
    <div v-else-if="hasError" class="error-container">
      <a-alert message="加载失败" :description="error" type="error" show-icon />
      <a-button @click="retryLoad">重试</a-button>
    </div>

    <!-- ========== 主内容区 ========== -->
    <div v-else class="main-content">
      <!-- 左侧导航菜单：学习模块切换 -->
      <div class="sidebar">
        <ul class="menu-list">
          <li v-for="item in menuItems" :key="item.key" :class="{ active: currentModule === item.key }"
            @click="switchModule(item.key)">
            <a-icon :type="item.icon" theme="filled" />
            <span>{{ item.label }}</span>
          </li>
        </ul>
      </div>

      <!-- 右侧资源列表面板 -->
      <div class="right-panel">
        <div class="panel-header">
          <h3>{{ panelTitle }}</h3>
          <a-icon type="ellipsis" class="more-icon" />
        </div>
        <div class="panel-content">
          <ul class="item-list">
            <li v-for="(item, index) in currentList" :key="index"
              :class="{ 'list-item-active': currentItemIndex === index }" @click="selectItem(index)">
              <a-checkbox v-model="item.checked" disabled></a-checkbox>
              <span class="item-text">{{ item.title }}</span>
            </li>
          </ul>
        </div>
      </div>

      <!-- 主内容展示区：根据当前模块动态渲染 -->
      <div class="content-area">
        <!-- 视频模块 -->
        <div v-if="currentModule === 'video'">
          <!-- 视频播放器，自动切换资源 -->
          <div class="video-player-wrapper">
            <video-player ref="videoPlayer" class="video-player" :options="videoPlayerOptions" @ended="handleVideoEnded"
              @play="handleVideoPlay" @pause="handleVideoPause" @seeked="handleVideoSeeked"
              @loadeddata="handleVideoLoaded" @loadedmetadata="handleVideoLoadedMetadata" />
          </div>
          <!-- 视频导航 -->
          <div class="video-navigation">
            <a-icon type="left" @click="navigatePrev" />
            <span>{{ currentItemIndex + 1 }}/{{ currentList.length }}</span>
            <a-icon type="right" @click="navigateNext" />
          </div>
        </div>

        <!-- 讲义模块 -->
        <div v-if="currentModule === 'lecture'" class="lecture-content">
          <!-- 文档预览区域 -->
          <div class="doc-container" @scroll="handleDocScroll">
            <!-- 动态组件根据文件类型显示不同预览器 -->
            <component :is="previewComponent" ref="officeViewer"
              v-if="previewComponent && currentLecture.src && currentLecture.type !== 'unknown'"
              :key="currentLecture.id" :src="currentLecture.src" @rendered="renderedHandler" @error="handlePreviewError"
              class="doc-preview" :style="{ transform: `scale(${zoomLevel / 100})`, transformOrigin: 'center' }" />

            <!-- 不支持的文件类型提示 -->
            <div v-else-if="currentLecture.type === 'unknown'" class="unknown-file">
              <a-icon type="file-unknown" />
              <p>不支持的文件格式: {{ getFileExtension(currentLecture.src) }}</p>
              <p>仅支持 .pptx, .pdf 格式（DOCX 不支持在线预览）</p>
            </div>

            <!-- 无文件提示 -->
            <div v-else class="no-file">
              <a-icon type="file-exclamation" />
              <p>暂无讲义文件</p>
            </div>
          </div>

          <!-- 导航控制 -->
          <div class="doc-navigation">
            <a-icon type="left" @click="navigatePrev" />
            <span class="page-info">{{ currentItemIndex + 1 }}/{{ currentList.length }}</span>
            <a-icon type="right" @click="navigateNext" />
          </div>

          <!-- 文档分页控制（紧凑悬浮，与缩放控件样式一致） -->
          <div class="doc-pagination" aria-label="文档分页控制">
            <a-icon class="doc-page-btn" :class="{ disabled: currentPage <= 1 }" type="left" @click="prevPage" />
            <span class="page-indicator">{{ currentPage }}/{{ totalPages }}</span>
            <a-icon class="doc-page-btn" :class="{ disabled: currentPage >= totalPages }" type="right"
              @click="nextPage" />
          </div>

          <!-- 缩放控制 -->
          <div class="zoom-controls">
            <a-button :disabled="zoomLevel <= 50" @click="zoomOut">
              <a-icon type="minus" />
            </a-button>
            <span class="zoom-level">{{ zoomLevel }}%</span>
            <a-button :disabled="zoomLevel >= 150" @click="zoomIn">
              <a-icon type="plus" />
            </a-button>
            <a-button @click="resetZoom">
              <a-icon type="reload" />
              重置
            </a-button>
            <a-button @click="toggleFullscreen">
              <a-icon :type="isFullscreen ? 'fullscreen-exit' : 'fullscreen'" />
              {{ isFullscreen ? '退出全屏' : '全屏' }}
            </a-button>
          </div>
        </div>

        <!-- 测试模块 -->
        <div v-if="currentModule === 'data'" class="data-content">
          <!-- 文档预览区域 -->
          <div class="doc-container" @scroll="handleDocScroll">
            <!-- 动态组件根据文件类型显示不同预览器 -->
            <component :is="previewComponent" ref="officeViewer" :key="currentData.id" :src="currentData.src"
              @rendered="renderedHandler" @error="handlePreviewError" class="doc-preview"
              :style="{ transform: `scale(${zoomLevel / 100})`, transformOrigin: 'center' }" />


          </div>

          <!-- 导航控制 -->
          <div class="doc-navigation">
            <a-icon type="left" @click="navigatePrev" />
            <span class="page-info">{{ currentItemIndex + 1 }}/{{ dataList.length }}</span>
            <a-icon type="right" @click="navigateNext" />
          </div>

          <!-- 资料信息显示 -->
          <div class="data-info" v-if="currentData.title">
            <h3>{{ currentData.title }}</h3>
            <div class="data-meta" v-if="currentData.originalData">
              <span class="meta-item">创建时间: {{ currentData.originalData.createTime }}</span>
              <span class="meta-item">创建者: {{ currentData.originalData.createBy }}</span>
            </div>
            <a-button type="primary" icon="download" @click="downloadDocument(currentItemIndex)">下载资料</a-button>
          </div>
        </div>

        <!-- 详情模块 -->
        <div v-if="currentModule === 'Details'" class="details-content">
          <div class="details-container">
            <h2>{{ currentDetails.title || '课程详情' }}</h2>
            <div class="course-info" v-if="courseDetails.course_name">
              <p><strong>课程名称：</strong>{{ courseDetails.course_name }}</p>
              <p><strong>课程标签：</strong>{{ courseDetails.courseTag || '未设置' }}</p>
              <p><strong>课程时长：</strong>{{ courseDetails.courseHours ? courseDetails.courseHours + '小时' : '未设置'
                }}</p>
              <p><strong>课程状态：</strong>{{ courseDetails.courseStatus === 1 ? '启用' : '禁用' }}</p>
              <p><strong>创建时间：</strong>{{ courseDetails.createTime || '未设置' }}</p>
            </div>
            <div v-else class="no-data">
              <p>请选择左侧详情项目查看内容</p>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
/**
 * 课程详情页面组件
 *
 * 功能特性：
 * - 多模块学习资源管理（视频、讲义、资料、详情）
 * - 视频播放器集成，支持进度保存和断点续学
 * - Office文档在线预览（PPTX、PDF、DOCX）
 * - 学习记录自动保存
 * - 响应式布局设计
 */

// ========== 第三方依赖导入 ==========
import VueVideoPlayer from 'vue-video-player'
import 'video.js/dist/video-js.css'
import { getAction, postAction, putAction } from '@/api/manage'
// import store from '@/store'
// Office文档预览组件
import VueOfficePptx from '@vue-office/pptx'
import VueOfficePdf from '@vue-office/pdf'

// 兼容 vue-video-player 的不同导出方式
const VideoPlayer = (VueVideoPlayer && VueVideoPlayer.videoPlayer) ? VueVideoPlayer.videoPlayer : VueVideoPlayer

export default {
  // ========== 组件名称和依赖 ==========
  name: 'CourseDetailsFrom',
  components: {
    VideoPlayer,
    VueOfficePptx,
    VueOfficePdf
    // VueOfficeDocx removed: DOCX preview not supported
  },
  data() {
    return {
      // ========== API接口配置 ==========
      url: {
        get: '/study/cloudComputingCourseResource/queryById', // 课程详情
        getCourseResource: '/study/cloudComputingCourseResource/listWithStatus', // 课程资源列表（包含学习状态）
        getCourseById: '/study/cloudComputingCourse/queryById', // 查询课程信息
        getLearningRecord: '/study/cloudComputingStudentLearningRecord/list', // 查询学习记录
        addLearningRecord: '/study/cloudComputingStudentLearningRecord/add', // 添加学习记录
        editLearningRecord: '/study/cloudComputingStudentLearningRecord/edit' // 编辑学习记录
      },

      // ========== 页面状态管理 ==========
      courseId: '', // 当前课程ID
      currentModule: 'video', // 当前激活的学习模块
      currentItemIndex: 0, // 当前选中的资源索引
      loading: false, // 全局加载状态
      error: null, // 错误信息

      // ========== 视频播放器配置 ==========
      videoPlayerOptions: {
        controls: true,
        preload: 'metadata', // 只预加载元数据，避免进度条问题
        fluid: true,
        aspectRatio: '16:9',
        poster: '',
        autoplay: false,
        html5: {
          hls: {
            enableLowInitialPlaylist: true,
            smoothQualityChange: true,
            overrideNative: true
          }
        }
      },

      // ========== 讲义文档相关 ==========
      zoomLevel: 100, // 文档缩放级别
      currentPage: 1, // 当前页码
      totalPages: 23, // 总页数
      isProgrammaticScrolling: false, // 当通过按钮触发滚动时为 true，防止联动处理
      isFullscreen: false, // 全屏状态

      // ========== 学习资源数据 ==========
      // 各模块的资源列表（动态从后端加载）
      videoList: [], // 视频资源列表
      lectureList: [], // 讲义资源列表
      dataList: [], // 资料资源列表
      DetailsList: [], // 详情资源列表

      // 讲义资源详细信息（包含文件类型等）
      lectureDetails: [],

      // ========== 课程信息 ==========
      courseDetails: {}, // 课程基本信息

      // ========== 学习记录管理 ==========
      learningRecord: {
        id: null, // 学习记录主键ID
        courseId: null, // 课程ID
        contentId: null, // 学习资源ID
        learningStatus: '0', // 学习状态：0未完成, 1已完成
        learningTime: 0, // 累计学习时长(秒)
        lastLearnTime: null, // 最后一次学习时间
        createTime: null // 记录创建时间
      },

      // ========== 资源统计信息 ==========
      // resourceCounts: {
      //   video: 0,
      //   lecture: 0,
      //   experiment: 0,
      //   data: 0
      // },

      // ========== 定时器管理 ==========
      progressSaveTimer: null, // 学习进度自动保存定时器

      // ========== 学习时长累加 ==========
      playStartTime: null, // 播放开始时间戳
      accumulatedTime: 0, // 已累加的学习时长(秒)

      // ========== Office文档滚动监听 ==========
      scrollCheckTimer: null, // 滚动检查定时器
      lectureCompleted: false, // 讲义完成状态
      lastScrollPercentage: 0, // 上次滚动百分比（用于减少日志输出）
      lastScrollElement: null // 上次检查的滚动元素（用于检测元素变化）
    }
  },

  // ============ 生命周期钩子 ============
  created() {
    // 页面创建时初始化数据
    this.initializePage()
  },

  mounted() {
    // 监听全屏状态变化
    document.addEventListener('fullscreenchange', this.handleFullscreenChange)
    document.addEventListener('webkitfullscreenchange', this.handleFullscreenChange)
    document.addEventListener('mozfullscreenchange', this.handleFullscreenChange)
    document.addEventListener('MSFullscreenChange', this.handleFullscreenChange)
  },

  beforeDestroy() {
    // 组件销毁前清理定时器
    this.stopProgressSaveTimer()
    // 清理vue-office滚动监听器
    this.removeOfficeScrollListener()
    // 移除全屏监听器
    document.removeEventListener('fullscreenchange', this.handleFullscreenChange)
    document.removeEventListener('webkitfullscreenchange', this.handleFullscreenChange)
    document.removeEventListener('mozfullscreenchange', this.handleFullscreenChange)
    document.removeEventListener('MSFullscreenChange', this.handleFullscreenChange)
  },

  // ============ 计算属性 ============
  computed: {
    // ========== 动态数据计算 ==========
    /**
     * 根据当前模块返回对应的资源列表
     * @returns {Array} 当前模块的资源列表
     */
    currentList() {
      const listMap = {
        video: this.videoList,
        lecture: this.lectureList,
        data: this.dataList,
        Details: this.DetailsList
      }
      return listMap[this.currentModule] || []
    },

    /**
     * 获取当前选中资源的详细信息
     * @returns {Object} 当前选中资源的对象
     */
    currentDetails() {
      return this.currentList[this.currentItemIndex] || {}
    },

    // ========== 各模块当前资源 ==========
    /**
     * 当前选中视频的详细信息
     * @returns {Object} 当前视频资源对象
     */
    currentVideo() {
      return this.currentList[this.currentItemIndex] || {}
    },

    /**
     * 当前选中讲义的详细信息
     * @returns {Object} 当前讲义资源对象
     */
    currentLecture() {
      return this.lectureDetails[this.currentItemIndex] || {}
    },



    /**
     * 当前选中资料的详细信息
     * @returns {Object} 当前资料资源对象
     */
    currentData() {
      return this.dataList[this.currentItemIndex] || {}
    },

    // ========== UI界面相关 ==========
    /**
     * 根据文件扩展名返回对应的预览组件
     * @returns {String|null} 组件名称或null
     */
    previewComponent() {
      let currentItem = null
      if (this.currentModule === 'lecture') {
        currentItem = this.currentLecture
      } else if (this.currentModule === 'data') {
        currentItem = this.currentData
      }

      if (!currentItem || !currentItem.type) return null
      switch (currentItem.type) {
        case 'pptx':
          return 'VueOfficePptx'
        case 'pdf':
          return 'VueOfficePdf'
        case 'docx':
          // DOCX 不支持在线预览，返回 null 以触发不支持提示
          return null
        default:
          return null
      }
    },

    /**
     * 右侧面板的标题
     * @returns {String} 面板标题
     */
    panelTitle() {
      const titleMap = {
        video: '视频',
        lecture: '讲义',
        data: '资料',
        Details: '详情'
      }
      return titleMap[this.currentModule] || ''
    },

    /**
     * 左侧菜单项配置
     * @returns {Array} 菜单项数组
     */
    menuItems() {
      return [
        { key: 'video', icon: 'play-circle', label: '视频' },
        { key: 'lecture', icon: 'file-text', label: '讲义' },
        { key: 'data', icon: 'folder-open', label: '资料' },
        { key: 'Details', icon: 'container', label: '详情' }
      ]
    },

    // ========== 状态计算 ==========
    /**
     * 加载状态
     * @returns {Boolean} 是否正在加载
     */
    isLoading() {
      return this.loading
    },

    /**
     * 错误状态
     * @returns {Boolean} 是否有错误
     */
    hasError() {
      return !!this.error
    }
  },

  // ============ 监听器 ============
  watch: {
    /**
     * 当模块切换时，重置相关状态
     * @param {String} newModule - 新模块
     * @param {String} oldModule - 旧模块
     */
    currentModule(newModule, oldModule) {
      // 重置相关状态
      this.currentItemIndex = 0
      this.currentPage = 1
      this.zoomLevel = 100
    },

    /**
     * 当选中索引改变时，更新相关状态
     * @param {Number} newVal - 新索引
     * @param {Number} oldVal - 旧索引
     */
    currentItemIndex(newVal, oldVal) {
      // 索引改变的逻辑在selectItem方法中处理
    }
  },

  // ============ 方法 ============
  methods: {


    // ============ 页面初始化与生命周期 ============
    /**
     * 初始化页面数据
     * 从路由参数获取课程ID并加载资源
     */
    initializePage() {
      const { courseId, resourceType } = this.$route.query
      this.courseId = courseId
      this.resourceType = resourceType

      // 获取当前用户信息
      // this.getCurrentUser()

      // 加载课程基本信息和资源列表
      Promise.all([
        this.loadCourseDataById(this.courseId),
        this.loadCourseResources(this.courseId)
      ]).then(() => {
        // 数据加载完成后，根据resourceType切换到对应模块
        if (this.resourceType) {
          this.switchModule(this.resourceType, true) // 标记为初始化加载
        } else {
          // 默认切换到视频模块
          this.switchModule('video', true) // 标记为初始化加载
        }
      }).catch((error) => {
        console.error('初始化页面数据失败:', error)
        this.$message.error('加载数据失败，请刷新页面重试')
      })
    },

    // ============ 学习记录管理 ============
    /**
     * 查询学习记录
     * 根据课程ID、用户ID和资源ID查询学习记录
     * @param {string} contentId - 学习资源ID
     */
    async queryLearningRecord(contentId) {
      if (!this.courseId || !contentId) {
        return
      }

      try {
        const res = await getAction(this.url.getLearningRecord, {
          courseId: this.courseId,
          contentId: contentId,
          pageNo: 1,
          pageSize: 1
        })

        if (res && res.success && res.result && Array.isArray(res.result.records) && res.result.records.length > 0) {
          const record = res.result.records[0]
          this.learningRecord = {
            id: record.id,
            courseId: record.courseId,
            contentId: record.contentId,
            learningStatus: record.learningStatus,
            learningTime: record.learningTime,
            lastLearnTime: record.lastLearnTime,
            createTime: record.createTime
          }
          this.syncCurrentItemLearningRecord(this.learningRecord)
          // 验证学习时间的合理性，防止数据库中存储了错误的大值
          const maxReasonableTime = 24 * 60 * 60 // 24小时的秒数
          const learningTime = this.learningRecord.learningTime || 0

          if (learningTime > maxReasonableTime) {
            console.warn(`检测到异常的学习时间: ${learningTime}秒 (${Math.floor(learningTime / 3600)}小时)，重置为0`)
            this.accumulatedTime = 0
            // 同时更新数据库中的错误数据
            this.updateLearningRecord({
              learningTime: 0,
              learningStatus: '0',
              lastLearnTime: this.formatDateTime(new Date())
            })
          } else {
            this.accumulatedTime = learningTime
          }
          this.playStartTime = null
        } else {
          await this.createLearningRecord(contentId)
        }
      } catch (err) {
        console.error('查询学习记录失败:', err)
        await this.createLearningRecord(contentId)
      }
    },

    /**
     * 创建学习记录
     * @param {string} contentId - 学习资源ID
     */
    async createLearningRecord(contentId) {
      if (!this.courseId || !contentId) {
        return
      }

      try {
        const recordData = {
          courseId: this.courseId,
          contentId: contentId,
          learningStatus: '0',
          learningTime: 0,
          lastLearnTime: this.formatDateTime(new Date()),
          createTime: this.formatDateTime(new Date())
        }

        const res = await postAction(this.url.addLearningRecord, recordData)

        if (res && res.success) {
          let recordId = null
          if (res.result && typeof res.result === 'object' && res.result.id) {
            recordId = res.result.id
          } else if (typeof res.result === 'string' && /^\d+$/.test(res.result)) {
            recordId = res.result
          } else {
            try {
              const queryRes = await getAction(this.url.getLearningRecord, {
                courseId: this.courseId,
                contentId,
                pageNo: 1,
                pageSize: 1
              })
              if (queryRes && queryRes.success && Array.isArray(queryRes.result.records)) {
                const matchedRecord = queryRes.result.records.find(r => r.contentId === contentId)
                if (matchedRecord) {
                  recordId = matchedRecord.id
                  recordData.learningStatus = matchedRecord.learningStatus
                  recordData.learningTime = matchedRecord.learningTime
                  recordData.lastLearnTime = matchedRecord.lastLearnTime
                  recordData.createTime = matchedRecord.createTime
                }
              }
            } catch (queryErr) {
              console.error('查询学习记录失败:', queryErr)
            }
          }

          if (!recordId) {
            this.$message.warning('创建学习记录成功但未获取记录ID，数据可能不同步')
            return
          }

          this.learningRecord = {
            id: recordId,
            ...recordData
          }
          this.accumulatedTime = 0
          this.playStartTime = null
          this.syncCurrentItemLearningRecord(this.learningRecord)
          this.$message.success('学习记录已创建')
        } else {
          throw new Error('创建学习记录失败')
        }
      } catch (err) {
        console.error('创建学习记录失败:', err)
        this.$message.error('创建学习记录失败')
      }
    },

    /**
     * 更新学习记录
     * @param {Object} updateData - 要更新的数据
     */
    async updateLearningRecord(updateData) {
      if (!this.learningRecord.id) {
        return
      }

      try {
        const requestData = {
          id: this.learningRecord.id,
          courseId: this.learningRecord.courseId,
          contentId: this.learningRecord.contentId,
          learningStatus: this.learningRecord.learningStatus,
          learningTime: this.learningRecord.learningTime,
          lastLearnTime: this.learningRecord.lastLearnTime,
          ...updateData,
          updateTime: this.formatDateTime(new Date())
        }

        let updateUrl = this.url.editLearningRecord || this.url.addLearningRecord
        const requestFn = updateUrl === this.url.editLearningRecord ? putAction : postAction
        const res = await requestFn(updateUrl, requestData)

        if (res && res.success) {
          this.learningRecord = {
            ...this.learningRecord,
            ...updateData,
            lastLearnTime: this.formatDateTime(new Date())
          }
          this.syncCurrentItemLearningRecord(this.learningRecord)
          this.updateLocalResourceStatus(updateData)
        } else {
          throw new Error('更新学习记录失败')
        }
      } catch (err) {
        console.error('更新学习记录失败:', err)
        this.$message.error('更新学习记录失败')
      }
    },

    /**
     * 同步更新本地资源列表的学习状态
     * @param {Object} updateData - 更新的数据
     */
    updateLocalResourceStatus(updateData) {
      // 只有当学习状态更新为已完成时才需要同步更新本地数据
      if (!updateData.learningStatus || updateData.learningStatus !== '1') {
        return
      }

      // 根据当前模块确定要更新的列表
      let targetList = null
      switch (this.currentModule) {
        case 'video':
          targetList = this.videoList
          break
        case 'lecture':
          targetList = this.lectureList
          break
        case 'data':
          targetList = this.dataList
          break
        case 'details':
          targetList = this.DetailsList
          break
        default:
          console.warn('未知的模块类型:', this.currentModule)
          return
      }

      if (!targetList || targetList.length === 0) {
        console.warn('目标列表为空或不存在')
        return
      }

      // 找到当前选中的资源项并更新其状态
      const currentItem = targetList[this.currentItemIndex]
      if (currentItem) {
        // 更新选中状态和学习记录状态
        currentItem.checked = true
        if (currentItem.learningRecord) {
          currentItem.learningRecord.learningStatus = '1'
          currentItem.learningRecord.lastLearnTime = this.formatDateTime(new Date())
          if (updateData.learningTime !== undefined) {
            currentItem.learningRecord.learningTime = updateData.learningTime
          }
        } else {
          currentItem.learningRecord = {
            id: this.learningRecord.id,
            learningStatus: '1',
            learningTime: updateData.learningTime !== undefined ? updateData.learningTime : this.learningRecord.learningTime,
            lastLearnTime: this.formatDateTime(new Date())
          }
        }
      }
    },

    /**
     * 将当前学习记录同步到资源列表项，避免后续更新使用旧的记录ID
     * @param {Object} record - 当前学习记录
     */
    syncCurrentItemLearningRecord(record) {
      if (!record) return
      const currentItem = this.currentList && this.currentList[this.currentItemIndex]
      if (!currentItem) return

      currentItem.learningRecord = {
        ...(currentItem.learningRecord || {}),
        id: record.id,
        learningStatus: record.learningStatus,
        learningTime: record.learningTime,
        lastLearnTime: record.lastLearnTime,
        recordCreateTime: record.createTime || (currentItem.learningRecord && currentItem.learningRecord.recordCreateTime)
      }
    },

    // ============ 数据加载相关方法 ============

    /**
     * 加载课程资源列表
     * @param {string} courseId - 课程ID
     */
    async loadCourseResources(courseId) {
      if (!courseId) {
        return
      }

      this.loading = true
      this.error = null

      try {
        const res = await getAction(this.url.getCourseResource, {
          courseId: courseId
        })

        if (res && res.success && Array.isArray(res.result)) {
          const resources = res.result
          this.processResourcesWithStatus(resources)
        } else {
          throw new Error('获取资源数据失败')
        }
      } catch (err) {
        console.error('加载课程资源失败:', err)
        this.error = err.message || '加载课程资源失败'
        this.$message.error('加载课程资源失败')
      } finally {
        this.loading = false
      }
    },

    /**
     * 加载实验资源数据
     * 从API获取实验资源并按课程名称过滤
     */
    async loadTestResources() {
      if (!this.courseDetails.course_name) {
        console.warn('课程名称为空，无法加载实验资源')
        return
      }

      try {
        const res = await getAction(this.url.getTestResource, {
          course_name: this.courseDetails.course_name,
          pageNo: 1,
          pageSize: 1000
        })


        if (res && res.success && res.result && Array.isArray(res.result.examlist)) {
          this.testList = res.result.examlist.map(item => ({
            id: item.experiment_id || item.exam_id,
            title: item.experiment_name || item.exam_name || '未命名实验',
            url: item.exam_url || '',
            checked: item.experiment_status === 1 || item.exam_status === 2, // 根据实验或考试状态设置完成状态
            completable: true,
            originalData: item
          }))
          console.log('实验资源加载成功:', this.testList.length, '个项目')
        } else {
          console.warn('获取实验资源失败:', (res && res.message) || '未知错误')
          this.testList = []
        }
      } catch (err) {
        console.error('加载实验资源失败:', err)
        this.testList = []
      }
    },

    /**
     * 加载考试资源数据
     * 从API获取考试资源并按课程名称过滤
     */
    async loadExamResources() {
      if (!this.courseDetails.course_name) {
        console.warn('课程名称为空，无法加载考试资源')
        return
      }

      try {
        const res = await getAction(this.url.getExamResource, {
          exam_type: '3', // 测试类型
          exam_name: '',
          course_name: this.courseDetails.course_name,
          pageNo: 1,
          pageSize: 1000
        })

        if (res && res.success && res.result && Array.isArray(res.result.list)) {


          this.examList = res.result.list.map(item => ({
            id: item.exam_id || item.id,
            title: item.exam_name || item.name || '未命名考试',
            url: item.exam_url || '',
            checked: item.exam_status === 2, // 根据考试状态设置完成状态
            completable: true,
            originalData: item
          }))
          console.log('考试资源加载成功:', this.examList.length, '个项目')
        } else {
          console.warn('获取考试资源失败:', (res && res.message) || '未知错误')
          this.examList = []
        }
      } catch (err) {
        console.error('加载考试资源失败:', err)
        this.examList = []
      }
    },

    /**
     * 查询课程表信息
     * @param {string} courseId - 课程ID
     */
    async loadCourseDataById(courseId) {
      if (!courseId) return

      this.loading = true
      this.error = null

      try {
        const res = await getAction(this.url.getCourseById, {
          id: courseId
        })

        if (res && res.success && res.result) {
          const courseData = res.result
          this.processCourseData(courseData)
        } else {
          throw new Error('获取课程数据失败')
        }
      } catch (err) {
        console.error('加载课程数据失败:', err)
        this.error = err.message || '加载课程数据失败'
        this.$message.error('加载课程数据失败')
      } finally {
        this.loading = false
      }
    },

    /**
     * 处理后端返回的课程数据
     * 更新课程详情信息
     * @param {Object} courseData - 课程数据对象
     */
    processCourseData(courseData) {
      if (courseData) {
        // 更新课程详情信息
        this.courseDetails = {
          course_name: courseData.courseName || courseData.name || '未命名课程',
          course_id: courseData.id || this.courseId,
          courseTag: courseData.courseTag || '未设置',
          courseHours: courseData.courseHours || 0,
          courseStatus: courseData.courseStatus || 0,
          createTime: courseData.createTime || '未设置',
          teacherId: courseData.teacherId || '未设置',
          ...courseData // 保留其他字段
        }

        // 更新详情列表中的课程信息
        this.DetailsList = this.DetailsList.map(item => ({
          ...item,
          course_name: this.courseDetails.course_name,
          course_id: this.courseDetails.course_id,
          description: item.title === '课程概述' ? `课程标签: ${this.courseDetails.courseTag}, 课程时长: ${this.courseDetails.courseHours}小时` : item.description,
          duration: `${this.courseDetails.courseHours}小时`,
          level: this.courseDetails.courseStatus === 1 ? '启用' : '禁用'
        }))

        this.$message.success('课程信息加载成功')
      }
    },

    /**
     * 处理后端返回的资源数据
     * 将资源按类型分类并构造完整的URL
     * @param {Array} resources - 资源数组
     */
    processResources(resources) {
      // 清空现有数据
      this.videoList = []
      this.lectureList = []
      this.dataList = []
      this.DetailsList = []
      this.lectureDetails = [] // 清空讲义详情

      // 预定义基础URL，避免循环中重复计算
      const baseUrl = process.env.VUE_APP_API_BASE_URL

      resources.forEach(r => {
        // 构造完整的资源URL
        let resourcePath = r.resourceUrl
        const resourceUrl = resourcePath ? `${baseUrl}/sys/common/static/${resourcePath}` : r.url

        // 构造资源项对象
        const item = {
          title: r.resourceName || r.name || '未命名',
          url: resourceUrl,
          poster: r.poster || r.thumbnail || '',
          checked: false,
          completable: r.resourceType === 1, // 视频类型可完成
          originalData: r // 保存原始数据
        }

        // 根据资源类型分类存储
        switch (Number(r.resourceType)) {
          case 1: // 视频
            this.videoList.push(item)
            break
          case 2: // 讲义
            this.lectureList.push(item)
            // 同时添加到讲义详情中，包含文件类型信息
            const extension = this.getFileExtension(r.resourceUrl)
            let type = ''
            switch (extension) {
              case 'pptx':
                type = 'pptx'
                break
              case 'pdf':
                type = 'pdf'
                break
              case 'docx':
                type = 'docx'
                break
              default:
                type = 'unknown'
            }
            this.lectureDetails.push({
              id: r.id,
              title: r.resourceName || r.name || '未命名',
              src: resourceUrl,
              type: type,
              sort: r.resourceSort
            })
            break
          case 3: // 资料
            this.dataList.push(item)
            break
          default:
            this.dataList.push(item)
        }
      })

      // 如果有视频资源，初始化播放器
      if (this.currentModule === 'video' && this.videoList.length > 0) {
        this.currentItemIndex = 0
        this.updateVideoSource()
        // 初始化时查询学习记录
        this.selectItem(0)
      }
    },

    /**
     * 处理包含学习状态的资源数据
     * 将资源按类型分组，并设置学习记录状态
     * @param {Array} resources - 资源数组（包含学习记录信息）
     */
    processResourcesWithStatus(resources) {
      // 清空现有数据
      this.videoList = []
      this.lectureList = []
      this.dataList = []
      this.DetailsList = []
      this.lectureDetails = [] // 清空讲义详情

      // 预定义基础URL，避免循环中重复计算
      const baseUrl = process.env.VUE_APP_API_BASE_URL

      resources.forEach(r => {
        // 构造完整的资源URL
        let resourcePath = r.resourceUrl
        const resourceUrl = resourcePath ? `${baseUrl}/sys/common/static/${resourcePath}` : r.url

        // 构造资源项对象，包含学习记录信息
        const item = {
          id: r.id,
          title: r.resourceName || r.name || '未命名',
          url: resourceUrl,
          poster: r.poster || r.thumbnail || '',
          checked: r.learningStatus === '1', // 根据学习状态设置选中状态
          completable: r.resourceType === 1, // 视频类型可完成
          originalData: r, // 保存原始数据
          // 添加学习记录相关信息
          learningRecord: {
            id: r.recordId,
            learningStatus: r.learningStatus || '0',
            learningTime: r.learningTime,
            lastLearnTime: r.lastLearnTime,
            recordCreateTime: r.recordCreateTime
          }
        }

        // 根据资源类型分类存储
        switch (Number(r.resourceType)) {
          case 1: // 视频
            this.videoList.push(item)
            break
          case 2: // 讲义
            this.lectureList.push(item)
            // 同时添加到讲义详情中，包含文件类型信息
            const extension = this.getFileExtension(r.resourceUrl)
            let type = ''
            switch (extension) {
              case 'pptx':
                type = 'pptx'
                break
              case 'pdf':
                type = 'pdf'
                break
              case 'docx':
                type = 'docx'
                break
              default:
                type = 'unknown'
            }
            this.lectureDetails.push({
              id: r.id,
              title: r.resourceName || r.name || '未命名',
              src: resourceUrl,
              type: type,
              sort: r.resourceSort,
              learningStatus: r.learningStatus // 添加学习状态
            })
            break
          case 3: // 资料
            this.dataList.push(item)
            break
          default:
            this.dataList.push(item)
        }
      })

      // 移除自动初始化逻辑，由 switchModule 统一处理
      // 如果有视频资源，初始化播放器
      // if (this.currentModule === 'video' && this.videoList.length > 0) {
      //   this.currentItemIndex = 0
      //   this.updateVideoSource()
      //   // 初始化时查询学习记录
      //   this.selectItem(0)
      // }
    },

    // ============ 视频播放相关方法 ============
    /**
     * 更新视频播放器源
     * 动态切换视频源并重新初始化播放器，同时启动进度保存定时器
     */
    updateVideoSource() {
      const selected = this.currentList[this.currentItemIndex]
      if (!selected || !selected.url) return

      // 停止当前的进度保存定时器
      this.stopProgressSaveTimer()

      this.$nextTick(() => {
        const playerComp = this.$refs.videoPlayer
        if (playerComp && playerComp.player) {
          try {
            // 暂停当前播放
            if (!playerComp.player.paused()) {
              playerComp.player.pause()
            }

            // 设置新的视频源
            playerComp.player.src({ src: selected.url, type: 'video/mp4' })

            // 等待播放器准备就绪
            playerComp.player.ready(() => {
              // 启动学习进度自动保存定时器
              this.startProgressSaveTimer()
            })
          } catch (e) {
            console.warn('更新播放器 source 失败', e)
          }
        }
      })
    },

    /**
     * 视频播放完成事件处理
     * 标记学习完成，停止进度保存定时器并可在此处记录学习进度
     */
    handleVideoEnded() {
      // 获取视频总时长，确保这是真正的播放完成
      const totalTime = (this.$refs.videoPlayer && this.$refs.videoPlayer.player && this.$refs.videoPlayer.player.duration) ? this.$refs.videoPlayer.player.duration() : 0
      const currentTime = (this.$refs.videoPlayer && this.$refs.videoPlayer.player && this.$refs.videoPlayer.player.currentTime) ? this.$refs.videoPlayer.player.currentTime() : 0

      // 保存最后一次观看的进度（避免重复累加时间）
      if (this.playStartTime) {
        this.saveLearningProgress(true)
      }

      // 只有当当前时间接近总时长（允许1秒误差）时才认为是真正的播放完成
      if (totalTime > 0 && Math.abs(currentTime - totalTime) <= 1) {
        // 停止学习进度自动保存定时器
        this.stopProgressSaveTimer()

        // 更新学习记录状态为已完成，并设置learningTime为累计时长
        if (this.learningRecord.id) {
          this.updateLearningRecord({
            learningTime: this.accumulatedTime, // 设置为累计学习时长
            learningStatus: '1', // 已完成
            lastLearnTime: this.formatDateTime(new Date())
          })
        }

        this.$message.success('学习完成！')
      }
    },

    /**
     * 视频播放事件处理
     * 当视频开始播放时启动进度保存定时器
     */
    handleVideoPlay() {
      // 记录播放开始时间，用于累加学习时长
      this.playStartTime = Date.now()
      // 启动学习进度自动保存定时器
      this.startProgressSaveTimer()
    },

    /**
     * 视频暂停事件处理
     * 当视频暂停时停止进度保存定时器，并保存当前观看进度
     */
    handleVideoPause() {
      // 保存当前观看进度（避免重复累加时间）
      if (this.playStartTime) {
        this.saveLearningProgress(true)
      }
      // 停止学习进度自动保存定时器
      this.stopProgressSaveTimer()
    },

    /**
     * 视频进度条拖拽事件处理
     * 当用户拖拽进度条时，不保存进度（因为learningTime现在用于累计时长）
     */
    handleVideoSeeked() {
      // 不保存进度，只记录拖拽事件
    },

    /**
     * 视频数据加载完成事件处理
     * 当视频数据加载完成后，确保播放器正常工作
     */
    handleVideoLoaded() {
      // 确保播放器控件正常显示
      const playerComp = this.$refs.videoPlayer
      if (playerComp && playerComp.player) {
        // 设置流式布局
        if (playerComp.player.fluid) {
          playerComp.player.fluid(true)
        }
      }
    },

    /**
     * 视频元数据加载完成事件处理
     * 当视频元数据加载完成后，不恢复播放进度（因为learningTime现在用于累计时长）
     */
    handleVideoLoadedMetadata() {
      // 不恢复播放进度，从头开始播放
    },

    /**
     * 启动学习进度自动保存定时器
     * 每30秒自动保存一次学习进度
     */
    startProgressSaveTimer() {
      // 清除之前的定时器
      this.stopProgressSaveTimer()

      // 启动新的定时器，每30秒执行一次
      this.progressSaveTimer = setInterval(() => {
        this.saveLearningProgress()
      }, 30000) // 30秒 = 30000毫秒
    },

    /**
     * 停止学习进度自动保存定时器
     */
    stopProgressSaveTimer() {
      if (this.progressSaveTimer) {
        clearInterval(this.progressSaveTimer)
        this.progressSaveTimer = null
      }
    },

    /**
     * 保存学习进度
     * 累加当前观看时间并更新累计学习时长
     */
    saveLearningProgress(isEndSession = false) {
      // 累加当前观看时间
      if (this.playStartTime) {
        const watchedSeconds = Math.floor((Date.now() - this.playStartTime) / 1000)

        // 验证单次观看时间的合理性（不应该超过1小时）
        const maxSingleSessionTime = 60 * 60 // 1小时
        if (watchedSeconds > maxSingleSessionTime) {
          console.warn(`检测到异常的单次观看时间: ${watchedSeconds}秒，可能是时间戳计算错误，忽略此次累加`)
        } else if (watchedSeconds > 0) {
          this.accumulatedTime += watchedSeconds
        }

        // 如果是结束会话（暂停/结束），则重置为null；如果是定时保存，则重置为当前时间继续计时
        if (isEndSession) {
          this.playStartTime = null
        } else {
          this.playStartTime = Date.now()
        }
      }

      // 更新学习记录
      if (this.learningRecord.id && this.accumulatedTime > 0) {
        this.updateLearningRecord({
          learningTime: this.accumulatedTime, // 保存累计学习时长
          learningStatus: '0', // 进行中，不是已完成
          lastLearnTime: this.formatDateTime(new Date())
        })

        this.$message.success(`学习进度已保存！累计观看 ${Math.floor(this.accumulatedTime / 60)}:${String(this.accumulatedTime % 60).padStart(2, '0')}`)
      }
    },
    /**
     * 通用上一项导航
     * 在当前模块的资源列表中向前导航
     */
    navigatePrev() {
      if (this.currentItemIndex > 0) {
        this.selectItem(this.currentItemIndex - 1)
      }
    },

    /**
     * 通用下一项导航
     * 在当前模块的资源列表中向后导航
     */
    navigateNext() {
      if (this.currentItemIndex < this.currentList.length - 1) {
        this.selectItem(this.currentItemIndex + 1)
      }
    },

    /**
     * 上一页（讲义分页）
     * 在讲义模块中向前翻页 - 通过滚动位置模拟分页
     */
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--
        this.scrollToPage(this.currentPage)
      }
    },

    /**
     * 下一页（讲义分页）
     * 在讲义模块中向后翻页 - 通过滚动位置模拟分页
     */
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++
        this.scrollToPage(this.currentPage)
      }
    },

    /**
     * 滚动到指定页面
     * @param {number} pageNum - 页码（从1开始）
     */
    scrollToPage(pageNum) {
      // 延迟执行，确保DOM完全更新
      setTimeout(() => {
        this._performScroll(pageNum)
      }, 100)
    },

    /**
     * 执行滚动操作
     * @private
     */
    _performScroll(pageNum) {
      // 标记正在进行程序化滚动，防止滚动事件触发联动处理
      try {
        this.isProgrammaticScrolling = true
      } catch (e) {
      }
      const container = document.querySelector('.doc-container')
      if (!container) {
        try {
          this.isProgrammaticScrolling = false
        } catch (e) {
        }
        return
      }

      const containerHeight = container.clientHeight

      // 查找所有可能的滚动元素
      const allElements = container.querySelectorAll('*')
      let scrollElement = container
      let maxScrollableHeight = 0

      // 找到最可能的可滚动元素
      for (let element of allElements) {
        const scrollHeight = element.scrollHeight
        const clientHeight = element.clientHeight
        const computedStyle = getComputedStyle(element)

        // 检查元素是否可滚动且没有overflow: hidden
        if (scrollHeight > clientHeight &&
          clientHeight > 0 &&
          computedStyle.overflow !== 'hidden' &&
          computedStyle.overflowY !== 'hidden') {
          const scrollableHeight = scrollHeight - clientHeight
          if (scrollableHeight > maxScrollableHeight) {
            maxScrollableHeight = scrollableHeight
            scrollElement = element
          }
        }
      }

      if (maxScrollableHeight === 0) {
        try {
          this.isProgrammaticScrolling = false
        } catch (e) {
        }
        return
      }

      // 计算目标滚动位置
      const pageHeight = containerHeight
      const targetScrollTop = (pageNum - 1) * pageHeight
      const maxScrollTop = maxScrollableHeight
      const finalScrollTop = Math.min(targetScrollTop, maxScrollTop)

      // 使用精简的滚动方法：优先尝试平滑 scrollTo，再作为后备直接设置 scrollTop
      const scrollMethods = [
        () => scrollElement.scrollTo({ top: finalScrollTop, behavior: 'smooth' }),
        () => {
          scrollElement.scrollTop = finalScrollTop
        }
      ]

      let methodIndex = 0
      const tryScroll = () => {
        if (methodIndex >= scrollMethods.length) {
          // 后备策略 1：如果是 iframe，尝试滚动 iframe 的 contentWindow
          try {
            if (scrollElement.tagName === 'IFRAME' && scrollElement.contentWindow) {
              try {
                scrollElement.contentWindow.scrollTo(0, finalScrollTop)
                // iframe滚动后清理标志
                try {
                  this.isProgrammaticScrolling = false
                } catch (e) {
                }
                return // 成功则返回，不继续后备
              } catch (e) {
              }
            }
          } catch (e) {
          }

          // 后备策略 2：按子元素定位并直接滚动到子元素位置
          try {
            const candidateChildren = Array.from(scrollElement.querySelectorAll('*')).filter(c => c.clientHeight > 20)
            if (candidateChildren.length > 0) {
              // 找到第一个 offsetTop >= finalScrollTop 的子元素
              let targetChild = candidateChildren.find(c => c.offsetTop >= finalScrollTop)
              if (!targetChild) {
                // 如果没有找到，则按页码近似选择
                const approxIndex = Math.min(Math.max(0, pageNum - 1), candidateChildren.length - 1)
                targetChild = candidateChildren[approxIndex]
              }

              if (targetChild) {
                try {
                  scrollElement.scrollTop = targetChild.offsetTop
                  // 立即清理标志，因为这是最后的后备策略
                  try {
                    this.isProgrammaticScrolling = false
                  } catch (e) {
                  }
                } catch (e) {
                  console.warn('子元素 offsetTop 滚动失败:', e)
                  // 失败时也清理
                  try {
                    this.isProgrammaticScrolling = false
                  } catch (e) {
                  }
                }
              }
            } else {
              console.warn('未找到合适的子元素用于 offsetTop 滚动')
            }
          } catch (e) {
            console.warn('按子元素后备滚动时出错:', e)
          }

          // 等待短时后检查是否滚动成功
          setTimeout(() => {
            const actualScrollTop = scrollElement.scrollTop
            const diff = Math.abs(actualScrollTop - finalScrollTop)
            if (diff < 20) {
            } else {
            }

            // 清理程序化滚动标志
            try {
              this.isProgrammaticScrolling = false
            } catch (e) {
            }
          }, 250)

          return
        }

        try {
          scrollMethods[methodIndex]()

          // 检查滚动是否生效
          setTimeout(() => {
            const actualScrollTop = scrollElement.scrollTop
            const diff = Math.abs(actualScrollTop - finalScrollTop)

            if (diff < 10) {
              // 清理程序化滚动标志
              try {
                this.isProgrammaticScrolling = false
              } catch (e) {
              }
            } else {
              methodIndex++
              tryScroll()
            }
          }, 200)
        } catch (e) {
          methodIndex++
          tryScroll()
        }
      }

      tryScroll()
    },

    // ============ 用户交互方法 ============
    /**
     * 切换学习模块
     * @param {string} module - 模块名称 ('video', 'lecture', 'experiment', 'test', 'data', 'details')
     * @param {boolean} isInitialLoad - 是否为页面初始化时的切换
     */
    switchModule(module, isInitialLoad = false) {
      // 如果从视频模块切换到其他模块，停止进度保存定时器
      if (this.currentModule === 'video' && module !== 'video') {
        this.stopProgressSaveTimer()
      }

      // 如果离开讲义模块，清理滚动监听器
      if (this.currentModule === 'lecture' && module !== 'lecture') {
        this.removeOfficeScrollListener()
      }

      this.currentModule = module

      // 如果切换到讲义模块，设置滚动监听器并重置完成状态
      if (module === 'lecture') {
        this.lectureCompleted = false // 重置完成状态
        this.$nextTick(() => {
          this.setupOfficeScrollListener()
        })
      }

      // 如果切换到实验模块，加载测试资源
      if (module === 'experiment') {
        this.loadTestResources()
      }

      // 如果切换到测试模块，加载考试资源
      if (module === 'test') {
        this.loadExamResources()
      }

      // 自动选择第一个资源（用于显示）
      this.$nextTick(() => {
        const listMap = {
          video: this.videoList,
          lecture: this.lectureList,
          experiment: this.testList,
          test: this.examList,
          data: this.dataList,
          Details: this.DetailsList
        }
        const targetList = listMap[module]
        if (targetList && targetList.length > 0) {
          // 检查当前是否已经有该模块的学习记录
          const hasLearningRecord = targetList.some(item =>
            item.learningRecord && item.learningRecord.id
          )

          if (isInitialLoad || !hasLearningRecord) {
            // 初始化加载或该模块还没有学习记录时，创建学习记录
            this.selectItem(0)
          } else {
            // 手动切换且已有学习记录时，只设置索引和更新显示
            this.currentItemIndex = 0
            // 如果是视频模块，需要更新播放器源
            if (module === 'video') {
              this.updateVideoSource()
            }
          }
        }
      })
    },

    /**
     * 选择列表项
     * @param {number} index - 选中项的索引
     */
    async selectItem(index) {
      this.currentItemIndex = index

      // 使用资源中已包含的学习记录信息
      const selectedItem = this.currentList[index]
      if (selectedItem && selectedItem.learningRecord && selectedItem.learningRecord.id) {
        // 更新当前学习记录状态（已有学习记录）
        this.learningRecord = {
          id: selectedItem.learningRecord.id,
          courseId: this.courseId,
          contentId: selectedItem.originalData.id,
          learningStatus: selectedItem.learningRecord.learningStatus || '0',
          learningTime: selectedItem.learningRecord.learningTime || 0,
          lastLearnTime: selectedItem.learningRecord.lastLearnTime,
          createTime: selectedItem.learningRecord.recordCreateTime
        }
        this.syncCurrentItemLearningRecord(this.learningRecord)
      } else {
        // 如果没有学习记录信息或记录ID为空，查询或创建新的
        await this.queryLearningRecord(selectedItem.originalData.id)
      }

      // 验证并初始化累计学习时长
      const maxReasonableTime = 24 * 60 * 60 // 24小时的秒数
      const learningTime = this.learningRecord.learningTime || 0

      if (learningTime > maxReasonableTime) {
        console.warn(`检测到异常的学习时间: ${learningTime}秒 (${Math.floor(learningTime / 3600)}小时)，重置为0`)
        this.accumulatedTime = 0
        // 同时更新数据库中的错误数据
        this.updateLearningRecord({
          learningTime: 0,
          learningStatus: '0',
          lastLearnTime: this.formatDateTime(new Date())
        })
      } else {
        this.accumulatedTime = learningTime
      }
      this.playStartTime = null // 重置播放开始时间

      // 然后更新视频源，这样handleVideoLoadedMetadata可以使用正确的learningRecord
      if (this.currentModule === 'video') {
        this.updateVideoSource()
      }

      // 如果是讲义模块，重新设置滚动监听器并重置完成状态
      if (this.currentModule === 'lecture') {
        this.lectureCompleted = false // 重置完成状态
        this.lastScrollPercentage = 0 // 重置滚动进度
        this.lastScrollElement = null // 重置滚动元素引用
        this.currentPage = 1 // 重置页码到第一页
        // 强制清理之前的滚动监听器
        this.removeOfficeScrollListener()
        this.$nextTick(() => {
          // 再次清理确保完全重置
          this.removeOfficeScrollListener()
          // 延迟设置，确保vue-office组件完全重新渲染
          setTimeout(() => {
            this.setupOfficeScrollListener()
          }, 500)
        })
      }
    },

    /**
     * 缩小显示（讲义缩放）
     * 减小讲义文档的显示比例
     */
    zoomOut() {
      if (this.zoomLevel > 50) {
        this.zoomLevel -= 10
      }
    },

    /**
     * 放大显示（讲义缩放）
     * 增大讲义文档的显示比例
     */
    zoomIn() {
      if (this.zoomLevel < 150) {
        this.zoomLevel += 10
      }
    },

    /**
     * 下载文档
     * @param {number} index - 资料列表中的索引
     */
    async downloadDocument(index) {
      const doc = this.dataList[index]
      if (!doc || !doc.url) {
        this.$message.error('文档链接无效')
        return
      }

      try {
        // 触发下载
        window.open(doc.url, '_blank')
        this.$message.success('文档下载中...')

        // 创建或更新学习记录，标记为已完成
        if (doc.originalData && doc.originalData.id) {
          await this.queryLearningRecord(doc.originalData.id)

          // 更新学习记录为已完成状态
          if (this.learningRecord.id) {
            await this.updateLearningRecord({
              learningStatus: '1', // 已完成
              lastLearnTime: this.formatDateTime(new Date())
            })
          }
        }
      } catch (error) {
        // 下载仍然继续，不影响用户体验
      }
    },

    /**
     * 返回上一页
     * 导航回上一级页面
     */
    handleBack() {
      this.$router.go(-1)
    },

    /**
     * 前进到下一级
     * 预留的导航方法
     */
    // handleForward() {
    //   this.$message.info('前进到下一级')
    // },

    /**
     * 下载全部资料
     * 批量下载当前模块的所有资料文档
     */
    async downloadAllDocuments() {
      if (!this.dataList || this.dataList.length === 0) {
        this.$message.warning('暂无资料可下载')
        return
      }

      // 依次下载所有文档
      this.dataList.forEach((doc, index) => {
        if (doc && doc.url) {
          // 延迟下载，避免浏览器阻止多个下载
          setTimeout(async () => {
            window.open(doc.url, '_blank')

            // 为每个下载的文档创建学习记录
            try {
              if (doc.originalData && doc.originalData.id) {
                await this.queryLearningRecord(doc.originalData.id)
                if (this.learningRecord.id) {
                  await this.updateLearningRecord({
                    learningStatus: '1', // 已完成
                    lastLearnTime: this.formatDateTime(new Date())
                  })
                }
              }
            } catch (error) {
              console.error('批量下载创建学习记录失败:', error)
            }
          }, index * 500) // 每个下载间隔500ms
        }
      })

      this.$message.success(`开始下载 ${this.dataList.length} 个文档`)
    },

    /**
     * 格式化日期时间为后端期望的格式
     * @param {Date} date - 日期对象
     * @returns {string} 格式化后的日期字符串 (yyyy-MM-dd HH:mm:ss)
     */
    formatDateTime(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      const seconds = String(date.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    },

    // ============ 讲义相关方法 ============
    /**
     * 获取文件扩展名
     * @param {string} filename - 文件名
     * @returns {string} 文件扩展名
     */
    getFileExtension(filename) {
      if (!filename) return ''
      const parts = filename.split('.')
      return parts.length > 1 ? parts[parts.length - 1].toLowerCase() : ''
    },

    /**
     * 获取文件类型名称
     * @param {string} type - 文件类型
     * @returns {string} 文件类型名称
     */
    getFileTypeName(type) {
      switch (type) {
        case 'pptx':
          return 'PPT文档'
        case 'pdf':
          return 'PDF文档'
        case 'docx':
          return 'Word文档'
        default:
          return '未知类型'
      }
    },

    /**
     * 统一的预览错误处理
     * @param {Error} error - 错误对象
     */
    handlePreviewError(error) {
      console.error('文档加载失败:', error)
      const typeName = this.getFileTypeName(this.currentLecture.type)
      this.$message.error(`${typeName}加载失败，请检查文件是否存在`)
    },

    /**
     * 重置缩放
     */
    resetZoom() {
      this.zoomLevel = 100
    },

    /**
     * 切换全屏模式
     */
    toggleFullscreen() {
      const lectureContent = this.$el.querySelector('.lecture-content')
      if (!lectureContent) return

      if (!this.isFullscreen) {
        // 进入全屏
        if (lectureContent.requestFullscreen) {
          lectureContent.requestFullscreen()
        } else if (lectureContent.webkitRequestFullscreen) {
          lectureContent.webkitRequestFullscreen()
        } else if (lectureContent.msRequestFullscreen) {
          lectureContent.msRequestFullscreen()
        }
      } else {
        // 退出全屏
        if (document.exitFullscreen) {
          document.exitFullscreen()
        } else if (document.webkitExitFullscreen) {
          document.webkitExitFullscreen()
        } else if (document.msExitFullscreen) {
          document.msExitFullscreen()
        }
      }
    },

    /**
     * 处理全屏状态变化
     */
    handleFullscreenChange() {
      this.isFullscreen = !!(document.fullscreenElement ||
        document.webkitFullscreenElement ||
        document.mozFullScreenElement ||
        document.msFullscreenElement)
    },

    /**
     * 文档渲染完成事件处理
     */
    renderedHandler(event) {
      // 多次尝试获取文档信息，确保组件完全渲染
      let attempts = 0
      const maxAttempts = 8

      const computeDocHeight = (docElement, container) => {
        // 基本高度候选
        var candidates = []
        try {
          candidates.push(docElement.scrollHeight || 0)
          candidates.push(docElement.offsetHeight || 0)
          candidates.push(docElement.getBoundingClientRect().height || 0)
        } catch (e) {
        }

        // 检查子元素（针对 pptx 渲染 wrapper / slides）
        try {
          var children = Array.prototype.slice.call(docElement.querySelectorAll('*'))
          var maxBottom = 0
          for (var i = 0; i < children.length; i++) {
            var c = children[i]
            if (!c) continue
            var h = c.offsetHeight || c.clientHeight || 0
            var t = c.offsetTop || 0
            var bottom = t + h
            if (bottom > maxBottom) maxBottom = bottom
          }
          if (maxBottom > 0) candidates.push(maxBottom)
        } catch (e) {
          // ignore
        }

        // 最终取最大值
        var docHeight = Math.max.apply(null, candidates.concat([0]))
        return docHeight
      }

      const checkDimensions = () => {
        attempts++
        var container = document.querySelector('.doc-container')
        var docElement = null
        if (container) {
          docElement = container.querySelector('.doc-preview') || container.querySelector('.pptx-preview-wrapper') || container.querySelector('.pptx-preview') || container.querySelector('.pptx')
        }

        if (container && docElement) {
          var containerHeight = container.clientHeight
          var docHeight = computeDocHeight(docElement, container)

          // 如果文档高度明显大于容器高度，说明内容已加载
          if (docHeight > containerHeight * 1.2 || attempts >= maxAttempts) {
            // 计算总页数：文档高度 / 容器高度
            this.totalPages = Math.max(1, Math.ceil(docHeight / Math.max(1, containerHeight)))

            // 设置vue-office滚动监听器
            if (this.currentModule === 'lecture') {
              this.setupScrollListener()
            }
            return
          }
        }

        // 如果还没获取到或者内容还没完全加载，继续尝试（指数回退）
        if (attempts < maxAttempts) {
          var delay = 500 + attempts * 300
          setTimeout(checkDimensions, delay)
        } else {
          console.warn('无法获取文档尺寸，使用默认值')
          this.totalPages = 1
          if (this.currentModule === 'lecture') {
            this.setupScrollListener()
          }
        }
      }

      // 延迟开始检查
      setTimeout(checkDimensions, 300)
    },

    /**
     * 设置滚动监听器
     */
    setupScrollListener() {
      // 确保滚动状态完全重置
      this.lectureCompleted = false
      this.lastScrollPercentage = 0
      // 清理之前的监听器
      this.removeOfficeScrollListener()
      // 延迟设置滚动监听器，确保DOM完全更新
      this.$nextTick(() => {
        setTimeout(() => {
          this.setupOfficeScrollListener()
        }, 200)
      })
    },

    /**
     * 监听文档容器滚动事件
     * @param {Event} event - 滚动事件
     */
    handleDocScroll(event) {
      // 如果当前是程序化滚动（上一页/下一页触发），忽略此事件以防止联动
      if (this.isProgrammaticScrolling) return

      const container = event.target
      const { scrollTop, scrollHeight, clientHeight } = container

      // 根据滚动位置计算当前页码
      if (this.totalPages > 1 && clientHeight > 0) {
        const currentPage = Math.floor(scrollTop / clientHeight) + 1
        const clampedPage = Math.max(1, Math.min(currentPage, this.totalPages))

        if (clampedPage !== this.currentPage) {
          this.currentPage = clampedPage
        }
      }

      // 计算滚动百分比
      const scrollPercentage = (scrollTop + clientHeight) / scrollHeight

      // 如果滚动到底部80%以上，标记为完成
      if (scrollPercentage > 0.8 && this.currentModule === 'lecture') {
      }
    },

    /**
     * 为vue-office组件设置滚动监听器
     * 简化的定时检查方案
     */
    setupOfficeScrollListener() {
      this.$nextTick(() => {
        // 清理之前的监听器
        this.removeOfficeScrollListener()

        // 延迟执行，确保组件完全渲染
        setTimeout(() => {
          // 启动定时检查
          this.startScrollCheckTimer()
        }, 1000)
      })
    },

    /**
     * 启动滚动进度检查定时器
     */
    startScrollCheckTimer() {
      // 清理之前的定时器
      this.stopScrollCheckTimer()

      // 每300ms检查一次滚动位置
      this.scrollCheckTimer = setInterval(() => {
        if (this.currentModule === 'lecture' && !this.lectureCompleted) {
          this.checkScrollProgress()
        }
      }, 300)
    },

    /**
     * 停止滚动检查定时器
     */
    stopScrollCheckTimer() {
      if (this.scrollCheckTimer) {
        clearInterval(this.scrollCheckTimer)
        this.scrollCheckTimer = null
      }
    },

    /**
     * 检查滚动进度（核心逻辑）
     */
    checkScrollProgress() {
      try {
        // 在程序化滚动期间跳过检查，避免联动逻辑干扰
        if (this.isProgrammaticScrolling) return

        const docContainer = this.$el.querySelector('.doc-container')
        if (!docContainer) return

        // 查找vue-office组件内部的滚动元素
        let scrollElement = null

        // 优先查找可能有滚动的元素
        const selectors = [
          '.vue-office-pdf',
          '.vue-office-pptx',
          'iframe',
          'canvas',
          '[style*="overflow"]'
        ]

        for (const selector of selectors) {
          const elements = docContainer.querySelectorAll(selector)
          for (const element of elements) {
            // 检查元素是否有滚动内容
            if (element.scrollHeight > element.clientHeight ||
              element.scrollWidth > element.clientWidth) {
              scrollElement = element
              break
            }
          }
          if (scrollElement) break
        }

        // 如果没找到特殊元素，使用容器本身
        if (!scrollElement) {
          scrollElement = docContainer
        }

        const { scrollTop, scrollHeight, clientHeight } = scrollElement
        const scrollPercentage = scrollHeight > 0 ? (scrollTop + clientHeight) / scrollHeight : 0

        // 如果滚动元素发生变化（比如切换了文档），重置进度跟踪
        if (scrollElement !== this.lastScrollElement) {
          this.lastScrollElement = scrollElement
          this.lastScrollPercentage = 0
        }

        // 只有当滚动百分比发生显著变化时才输出日志
        if (Math.abs(scrollPercentage - (this.lastScrollPercentage || 0)) > 0.05) {
          this.lastScrollPercentage = scrollPercentage
        }

        // 如果滚动到底部80%以上，标记为完成
        if (scrollPercentage >= 0.8) {
          this.markLectureComplete()
        }
      } catch (error) {
        console.warn('检查滚动进度失败:', error)
      }
    },

    /**
     * 标记讲义阅读完成
     */
    markLectureComplete() {
      if (this.lectureCompleted) return // 避免重复标记

      this.lectureCompleted = true

      // 显示成功消息
      this.$message.success('讲义阅读完成！')

      // 更新学习记录
      if (this.learningRecord.id) {
        this.updateLearningRecord({
          learningStatus: '1', // 已完成
          lastLearnTime: this.formatDateTime(new Date())
        })
      }

      // 停止滚动检查
      this.stopScrollCheckTimer()
    },

    /**
     * 清理vue-office滚动监听器
     */
    removeOfficeScrollListener() {
      // 停止定时器
      this.stopScrollCheckTimer()

      // 重置状态
      this.lectureCompleted = false
      this.lastScrollPercentage = 0
    },

    /**
     * 开始实验
     * @param {Object} testItem - 测试项目对象
     */
    startTest(testItem) {
      if (!testItem || !testItem.url) {
        this.$message.warning('实验链接无效')
        return
      }

      this.$router.push({
        path: '/study/',
        query: params
      })

    },

    /**
     * 开始考试
     * @param {Object} examItem - 考试项目对象
     */
    startExam(examItem) {
      if (!examItem || !examItem.url) {
        this.$message.warning('考试链接无效')
        return
      }

      this.$router.push({
        path: '/study/',
        query: params
      })

    },

    /**
     * 创建或更新学习记录的通用方法
     * @param {string} contentId - 内容ID
     * @param {string} moduleType - 模块类型
     */
    async createOrUpdateLearningRecord(contentId, moduleType) {
      try {
        // 先查询是否已有学习记录
        await this.queryLearningRecord(contentId)

        const recordData = {
          courseId: this.courseId,
          contentId: contentId,
          learningStatus: '1', // 开始后标记为进行中
          lastLearnTime: this.formatDateTime(new Date())
        }

        if (this.learningRecord.id) {
          // 更新现有记录
          await this.updateLearningRecord(recordData)
        } else {
          // 创建新记录
          await this.addLearningRecord(recordData)
        }

        console.log(`${moduleType}学习记录已更新`)
      } catch (error) {
        console.error(`创建${moduleType}学习记录失败:`, error)
      }
    }
  }
}
</script>

<style scoped>
/* 课程详情页样式分区说明：
   - 头部 header
   - 主体 main-content（左侧菜单 sidebar、右侧资源列表 right-panel、主内容区 content-area）
   - 各模块内容区（视频、讲义、实验、测试、资料、详情）
*/
.course-container {
  background-color: #000;
  color: #fff;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  align-items: center;
  padding: 15px 30px;
  border-bottom: 1px solid #333;
}

.back-icon,
.forward-icon {
  font-size: 24px;
  color: #00aaff;
  cursor: pointer;
}

.course-title-text {
  margin: 0 20px;
  font-size: 18px;
  font-weight: 600;
  flex: 1;
}

.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  width: 80px;
  background-color: #1a1a1a;
  display: flex;
  flex-direction: column;
}

.menu-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.menu-list li {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
  cursor: pointer;
  border-bottom: 1px solid #333;
  transition: all 0.3s;
}

.menu-list li.active {
  background-color: #0078d7;
}

.menu-list li:hover:not(.active) {
  background-color: #333;
}

.menu-list li a-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.menu-list li span {
  font-size: 14px;
}

/* 主内容区 */
.content-area {
  flex: 1;
  background-color: #f5f5f5;
  overflow: auto;
  position: relative;
}

/* 视频内容区 */
.video-content {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.video-player-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.video-player {
  width: 100% !important;
  height: 100% !important;
  max-width: none;
  background-color: #000;
  position: relative;
}

.video-player .video-js,
.video-player video {
  width: 100% !important;
  height: 100% !important;
}

.video-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.video-poster {
  max-width: 80%;
  max-height: 70%;
  object-fit: contain;
}

.video-title {
  color: #fff;
  font-size: 24px;
  margin: 20px 0;
}

.video-navigation,
.pdf-navigation {
  display: flex;
  align-items: center;
  margin: 10px 0;
}

.page-info {
  margin: 0 15px;
  color: #fff;
}

.zoom-controls {
  display: flex;
  align-items: center;
  position: absolute;
  right: 20px;
  bottom: 20px;
  background-color: rgba(0, 0, 0, 0.5);
  padding: 5px 10px;
  border-radius: 4px;
}

.zoom-level {
  margin: 0 10px;
  color: #fff;
}

/* 讲义内容区 */
.lecture-content {
  width: 100%;
  height: 93%;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
  position: relative;
}

.doc-container {
  flex: 1;
  overflow: auto;
  background-color: #fff;
  margin: 30px;
  border: 1px solid #ddd;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  /* 移除flex布局，让内容自然排列 */
  /* display: flex;
  justify-content: center;
  align-items: center; */
}

.doc-preview {
  width: 100%;
  height: 100%;
  transition: transform 0.3s ease;
}

/* 文档分页控制 */
.doc-pagination {
  position: absolute;
  right: 30px;
  bottom: 90px;
  /* 在缩放控件之上 */
  display: flex;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 4px;
  padding: 8px 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 11;
  gap: 8px;
}

.page-indicator {
  margin: 0 8px;
  font-weight: 500;
  color: #1890ff;
  min-width: 48px;
  text-align: center;
}

.doc-page-btn {
  font-size: 18px;
  color: rgba(0, 0, 0, 0.65);
  cursor: pointer;
  padding: 4px;
}

.doc-page-btn.disabled {
  color: rgba(0, 0, 0, 0.25);
  cursor: not-allowed;
}

/* 缩放控制 */
.zoom-controls {
  position: absolute;
  right: 30px;
  bottom: 30px;
  display: flex;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 4px;
  padding: 8px 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 10;
}

.zoom-level {
  margin: 0 10px;
  min-width: 40px;
  text-align: center;
}

/* 未知文件类型提示 */
.unknown-file,
.no-file {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #999;
  font-size: 16px;
}

.unknown-file a-icon,
.no-file a-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.unknown-file p,
.no-file p {
  margin: 8px 0;
}

/* 实验内容区 */
.experiment-content,
.test-content {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #1a1a1a;
}

.experiment-container,
.test-container {
  width: 90%;
  height: 90%;
  background-color: #2d2d2d;
  border-radius: 8px;
  padding: 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-image: linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 20px 20px;
}

.experiment-title,
.test-title {
  color: #fff;
  font-size: 22px;
  margin-bottom: 30px;
  text-align: center;
}

.experiment-info,
.test-info {
  color: #ccc;
  margin-bottom: 40px;
  text-align: center;
}

.info-label {
  font-weight: bold;
  color: #fff;
}

.start-btn {
  padding: 12px 30px;
  font-size: 18px;
}

/* 资料内容区 */
.data-content {
  width: 100%;
  height: 100%;
  background-color: #fff;
  padding: 30px;
  overflow: auto;
}

.data-title {
  color: #333;
  font-size: 20px;
  margin-bottom: 20px;
}

.document-list {
  list-style: none;
  padding: 0;
  margin-bottom: 30px;
}

.document-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  border-bottom: 1px solid #eee;
  color: #333;
}

.document-info {
  flex: 1;
  margin-left: 15px;
}

.document-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.document-meta {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #666;
}

.meta-item {
  display: inline-block;
}

.document-item a-icon:first-child {
  margin-right: 15px;
  color: #0078d7;
}

.download-icon {
  margin-left: auto;
  color: #0078d7;
  cursor: pointer;
}

.document-note {
  color: #666;
  padding: 15px;
  background-color: #f5f5f5;
  border-radius: 4px;
  line-height: 1.6;
}

.data-actions {
  margin-top: 30px;
  text-align: center;
}

.data-actions .start-btn {
  padding: 12px 30px;
  font-size: 18px;
}

.right-panel {
  width: 320px;
  background-color: #f9f9f9;
  border-left: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 15px 20px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #f0f0f0;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.more-icon {
  color: #666;
  cursor: pointer;
}

.panel-content {
  flex: 1;
  overflow: auto;
}

.item-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.item-list li {
  padding: 12px 20px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: background-color 0.2s;
}

.item-list li:hover {
  background-color: #f0f0f0;
}

.item-list li.list-item-active {
  background-color: #e6f7ff;
  border-left: 3px solid #1890ff;
}

.item-text {
  margin-left: 10px;
  color: #333;
  flex: 1;
  font-size: 14px;
}

/* 底部操作区 */
.footer-actions {
  position: absolute;
  bottom: 20px;
  right: 340px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.menu-icon,
.help-icon {
  font-size: 20px;
  color: #666;
  cursor: pointer;
  margin-bottom: 5px;
}

.details-text,
.help-text {
  font-size: 12px;
  color: #666;
}

/* 详情内容区 */
.details-content {
  width: 100%;
  height: 100%;
  background-color: #fff;
  padding: 30px;
  overflow: auto;
}

.details-container {
  width: 100%;
  height: 100%;
}

.details-container h2 {
  color: #333;
  margin-bottom: 20px;
}

.course-info {
  color: #666;
}

/* 全屏模式样式 */
.lecture-content:fullscreen {
  background-color: #000;
}

.lecture-content:-webkit-full-screen {
  background-color: #000;
}

.lecture-content:-moz-full-screen {
  background-color: #000;
}

.lecture-content:-ms-fullscreen {
  background-color: #000;
}

.lecture-content:fullscreen .doc-container {
  margin: 0;
  background-color: transparent;
  border: none;
  box-shadow: none;
}

.lecture-content:-webkit-full-screen .doc-container {
  margin: 0;
  background-color: transparent;
  border: none;
  box-shadow: none;
}

.lecture-content:-moz-full-screen .doc-container {
  margin: 0;
  background-color: transparent;
  border: none;
  box-shadow: none;
}

.lecture-content:-ms-fullscreen .doc-container {
  margin: 0;
  background-color: transparent;
  border: none;
  box-shadow: none;
}

.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  color: rgba(255, 255, 255, 0.7);
}
</style>
