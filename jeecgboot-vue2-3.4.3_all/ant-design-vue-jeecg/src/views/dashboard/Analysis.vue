<template>
  <div class="home-dashboard">
    <!-- ================= 顶部统计概览 (循环渲染，便于增删) ================= -->
    <a-row :gutter="16" class="top-stats">
      <transition-group name="fade-scale" tag="div" class="stats-wrapper">
        <a-col v-for="meta in statsMeta" :key="meta.key" :xs="12" :sm="8" :md="6" :lg="6" :xl="4">
          <a-card size="small" :loading="loading" class="stat-card" :bordered="false" :class="'stat-' + meta.key">
            <div class="stat-header">
              <a-icon :type="meta.icon" class="stat-icon" />
              <div class="stat-title">{{ meta.title }}</div>
            </div>
            <div class="stat-value">
              <template v-if="!loading">{{ meta.value() }}</template>
              <a-skeleton v-else title="false" :paragraph="{ rows: 1 }" active />
            </div>
            <div class="stat-extra" v-if="meta.extra()">{{ meta.extra() }}</div>
          </a-card>
        </a-col>
      </transition-group>
    </a-row>

    <!-- 继续学习列表 -->
    <a-row :gutter="16" style="margin-top:16px;">
      <a-col :lg="12" :md="24">
        <a-card :loading="loading" :bordered="false" class="panel learning-panel">
          <template slot="title">
            <span class="panel-title"><a-icon type="play-circle" /> 继续学习</span>
          </template>
          <a-empty v-if="!inProgress.length && !loading" description="暂无进行中的资源" />
          <a-list v-else :data-source="inProgress" :split="false" class="progress-list">
            <a-list-item slot="renderItem" slot-scope="item" class="progress-item">
              <a-list-item-meta :title="item.title" :description="renderLearningDesc(item)"></a-list-item-meta>
              <div class="progress-actions">
                <a-button type="link" size="small" @click="goToResource(item)">继续</a-button>
              </div>
            </a-list-item>
          </a-list>
        </a-card>
      </a-col>
      <a-col :lg="12" :md="24">
        <a-card :loading="loading" :bordered="false" class="panel quick-panel">
          <template slot="title">
            <span class="panel-title"><a-icon type="rocket" /> 课程快速入口</span>
          </template>
          <div class="quick-courses">
            <a-tooltip v-for="c in quickCourses" :key="c.id" :title="isCourseOpen(c) ? '' : courseOpenStatusText(c)">
              <a-tag color="blue" class="quick-course-tag" :class="{ disabled: !isCourseOpen(c) }"
                @click="onClickQuickCourse(c)">
                <a-icon v-if="!isCourseOpen(c)" type="lock" style="margin-right:4px" />
                {{ c.courseName }}
              </a-tag>
            </a-tooltip>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 错误与刷新 -->
    <div v-if="error" class="error-box">
      <a-alert type="error" :message="error" show-icon />
      <a-button type="primary" size="small" style="margin-top:8px" @click="reloadAll">重试</a-button>
    </div>
  </div>
</template>

<script>
import { getAction } from '@/api/manage'

export default {
  name: 'Analysis',
  filters: {
    toFixed1(v) {
      if (!v && v !== 0) return ''
      return Number(v).toFixed(1)
    }
  },
  data() {
    return {
      loading: false,
      error: null,
      // 原始数据集合
      rawCourses: [],
      rawLearning: [],
      rawResourceCounts: {},
      rawResources: [], // 单个课程资源列表（作为讲义/资料统计的备用来源）
      stats: {
        totalCourses: 0,
        totalCategories: 0,
        totalHours: 0,
        avgHours: 0,
        totalLearningSeconds: 0,
        totalLecture: 0,
        totalResource: 0
      },
      inProgress: [],
      quickCourses: [],
      autoRefreshTimer: null,
      // 已移除排行榜逻辑
      // ========= 顶部统计配置 (title/icon/value/extra) =========
      statsMeta: [
        {
          key: 'courses',
          title: '课程总数',
          icon: 'book',
          value: () => this.stats.totalCourses,
          extra: () => (this.stats.totalCategories ? `分类 ${this.stats.totalCategories}` : '')
        },
        {
          key: 'hours',
          title: '总课时(h)',
          icon: 'clock-circle',
          value: () => this.stats.totalHours,
          extra: () => `平均 ${this.stats.avgHours} h/课`
        },
        {
          key: 'learnTotal',
          title: '累计学习(分钟)',
          icon: 'dashboard',
          value: () => this.$options.filters.toFixed1.call(this, this.stats.totalLearningSeconds / 60),
          extra: () => `≈ ${this.formatDuration(this.stats.totalLearningSeconds)}`
        },
        {
          key: 'inProgress',
          title: '进行中资源',
          icon: 'play-circle',
          value: () => this.inProgress.length,
          extra: () => `已完成 ${this.completedCount}`
        },
        {
          key: 'materials',
          title: '讲义/资料',
          icon: 'file-text',
          value: () => this.stats.totalLecture + this.stats.totalResource,
          extra: () => `讲义 ${this.stats.totalLecture} / 资料 ${this.stats.totalResource}`
        },
        {
          key: 'completion',
          title: '完成率',
          icon: 'check-circle',
          value: () => `${this.completionRate}%`,
          extra: () => `资源 ${this.totalLearningRecords} / 已完成 ${this.completedCount}`
        }
      ]
    }
  },
  computed: {
    completedCount() {
      return this.rawLearning.filter(r => r.learningStatus === '1').length
    },
    totalLearningRecords() {
      return this.rawLearning.length
    },
    completionRate() {
      if (!this.totalLearningRecords) return 0
      return ((this.completedCount / this.totalLearningRecords) * 100).toFixed(1)
    }
  },
  created() {
    this.reloadAll()
    this.startAutoRefresh()
  },
  beforeDestroy() {
    this.stopAutoRefresh()
  },
  methods: {
    reloadAll() {
      this.error = null
      this.loading = true
      Promise.all([
        this.fetchCourses(),
        this.fetchLearningRecords(),
        this.fetchResourceCounts().catch(() => { }), // counts接口失败不影响
        this.fetchResourcesList().catch(() => { })
      ]).then(() => {
        this.computeStats()
        this.prepareInProgress()
        this.prepareQuickCourses()
      }).catch(e => {
        console.error(e)
        this.error = '首页数据加载失败'
      }).finally(() => {
        this.loading = false
      })
    },
    startAutoRefresh() {
      // 每5分钟自动刷新一次（可根据需要调整）
      this.stopAutoRefresh()
      this.autoRefreshTimer = setInterval(() => {
        this.reloadAll()
      }, 5 * 60 * 1000)
    },
    stopAutoRefresh() {
      if (this.autoRefreshTimer) {
        clearInterval(this.autoRefreshTimer)
        this.autoRefreshTimer = null
      }
    },
    // ===== API =====
    async fetchCourses() {
      try {
        const res = await getAction('/study/cloudComputingCourse/list', { pageNo: 1, pageSize: 1000 })
        if (res && res.success) {
          const list = this.extractRecords(res.result)
          this.rawCourses = list.map(c => ({
            id: c.id,
            courseName: c.courseName || c.course_name,
            courseHours: Number(c.courseHours) || 0,
            courseTypeId: c.courseTypeId,
            courseTag: c.courseTag,
            // 兼容后端不同命名，默认视为未开课
            courseStatus: Number(c.courseStatus != null ? c.courseStatus : (c.status != null ? c.status : c.course_status)) || 0,
            // 预留开始时间（如果存在，则辅助判断）
            startTime: c.startTime || c.start_time || c.courseStartTime || null
          }))
        } else { throw new Error('课程列表获取失败') }
      } catch (e) { console.warn(e); this.rawCourses = [] }
    },
    async fetchLearningRecords() {
      try {
        const res = await getAction('/study/cloudComputingStudentLearningRecord/list', { pageNo: 1, pageSize: 1000 })
        if (res && res.success) {
          const list = this.extractRecords(res.result)
          this.rawLearning = list.map(r => ({
            id: r.id,
            courseId: r.courseId,
            contentId: r.contentId,
            learningStatus: r.learningStatus,
            learningTime: Number(r.learningTime) || 0,
            lastLearnTime: r.lastLearnTime,
            contentName: r.contentId_dictText || '',
            courseName: r.courseId_dictText || ''
          }))
        } else { throw new Error('学习记录获取失败') }
      } catch (e) { console.warn(e); this.rawLearning = [] }
    },
    async fetchResourceCounts() {
      try {
        const res = await getAction('/study/cloudComputingCourseResource/counts', { pageNo: 1, pageSize: 1000 })
        if (res && res.success) {
          // 假设返回数组按课程或总汇; 这里尝试聚合
          const list = Array.isArray(res.result) ? res.result : this.extractRecords(res.result)
          let lecture = 0, resource = 0
          list.forEach(item => {
            lecture += Number(item.lectureCount) || 0
            resource += Number(item.resourceCount) || 0
          })
          this.rawResourceCounts = { lecture, resource }
        } else { throw new Error('资源统计获取失败') }
      } catch (e) { console.warn(e); this.rawResourceCounts = {} }
    },
    async fetchResourcesList() {
      // 作为 counts 接口的降级，直接拉课程资源列表统计
      try {
        const res = await getAction('/study/cloudComputingCourseResource/list', { pageNo: 1, pageSize: 2000 })
        if (res && res.success) {
          const list = this.extractRecords(res.result)
          this.rawResources = list.map(r => ({
            id: r.id,
            courseId: r.courseId,
            resourceType: r.resourceType,
            resourceName: r.resourceName
          }))
        } else { throw new Error('资源列表获取失败') }
      } catch (e) { console.warn(e); this.rawResources = [] }
    },
    // 兼容 records / array / pagination 格式
    extractRecords(result) {
      if (!result) return []
      if (Array.isArray(result)) return result
      if (result.records && Array.isArray(result.records)) return result.records
      return []
    },
    // ===== 计算与整理 =====
    computeStats() {
      const totalCourses = this.rawCourses.length
      const hours = this.rawCourses.reduce((sum, c) => sum + (Number(c.courseHours) || 0), 0)
      const categories = new Set(this.rawCourses.map(c => c.courseTypeId)).size
      const totalLearningSeconds = this.rawLearning.reduce((sum, r) => sum + (Number(r.learningTime) || 0), 0)
      this.stats.totalCourses = totalCourses
      this.stats.totalCategories = categories
      this.stats.totalHours = hours
      this.stats.avgHours = totalCourses ? (hours / totalCourses).toFixed(1) : 0
      this.stats.totalLearningSeconds = totalLearningSeconds
      // 优先使用 counts 接口统计，若为空则用资源列表降级统计
      if (this.rawResourceCounts.lecture || this.rawResourceCounts.resource) {
        this.stats.totalLecture = this.rawResourceCounts.lecture || 0
        this.stats.totalResource = this.rawResourceCounts.resource || 0
      } else if (this.rawResources.length) {
        this.stats.totalLecture = this.rawResources.filter(r => String(r.resourceType) === '2').length
        this.stats.totalResource = this.rawResources.filter(r => String(r.resourceType) === '3').length
      } else {
        this.stats.totalLecture = 0
        this.stats.totalResource = 0
      }
    },
    prepareInProgress() {
      this.inProgress = this.rawLearning.filter(r => r.learningStatus === '0').sort((a, b) => {
        return new Date(b.lastLearnTime || 0) - new Date(a.lastLearnTime || 0)
      }).slice(0, 10)
    },
    prepareQuickCourses() {
      // 取最近学习过的课程 + 课时多的课程混合
      const learnedCourseIds = new Set(this.rawLearning.map(r => r.courseId))
      const learnedCourses = this.rawCourses.filter(c => learnedCourseIds.has(c.id))
        .sort((a, b) => b.courseHours - a.courseHours).slice(0, 8)
      const extra = this.rawCourses.filter(c => !learnedCourseIds.has(c.id))
        .sort((a, b) => b.courseHours - a.courseHours).slice(0, 4)
      this.quickCourses = [...learnedCourses, ...extra]
    },
    // ===== 展示辅助 =====
    isCourseOpen(course) {
      if (!course) return false
      const statusOpen = Number(course.courseStatus) === 1
      // 如果有开始时间，且在未来，则视为未开课
      if (course.startTime) {
        const start = new Date(course.startTime)
        if (!isNaN(start.getTime()) && start.getTime() > Date.now()) return false
      }
      return statusOpen
    },
    courseOpenStatusText(course) {
      if (!course) return '课程不可用'
      if (course.startTime) {
        const start = new Date(course.startTime)
        if (!isNaN(start.getTime()) && start.getTime() > Date.now()) {
          return `未开课（开始于 ${start.toLocaleString()}）`
        }
      }
      return '未开课'
    },
    formatDuration(seconds) {
      seconds = Number(seconds) || 0
      const h = Math.floor(seconds / 3600)
      const m = Math.floor((seconds % 3600) / 60)
      return `${h}h ${m}m`
    },
    renderLearningDesc(item) {
      return `已学 ${this.formatDuration(item.learningTime)} | 课程：${item.courseName}`
    },
    onClickQuickCourse(c) {
      if (!this.isCourseOpen(c)) {
        this.$message && this.$message.warning(this.courseOpenStatusText(c))
        return
      }
      this.goToCourse(c)
    },
    goToCourse(c) {
      if (!this.isCourseOpen(c)) {
        this.$message && this.$message.warning(this.courseOpenStatusText(c))
        return
      }
      this.$router.push({ path: '/study/CourseDetailsFrom', query: { courseId: c.id, resourceType: 'video' } })
    },
    goToResource(item) {
      this.$router.push({ path: '/study/CourseDetailsFrom', query: { courseId: item.courseId, resourceType: 'video' } })
    }
  }
}
</script>

<style scoped>
.home-dashboard {
  padding: 12px 14px 24px;
  background: linear-gradient(135deg, #fafafa 0%, #fff 60%);
}

.top-stats {
  margin-bottom: 8px;
}

.stats-wrapper {
  display: flex;
  flex-wrap: wrap;
  width: 100%;
}

.stat-card {
  min-height: 108px;
  position: relative;
  overflow: hidden;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, .06);
  transition: transform .25s, box-shadow .25s;
  backdrop-filter: saturate(150%) blur(6px);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, .12);
}

.stat-header {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stat-icon {
  font-size: 16px;
  color: #1890ff;
}

.stat-title {
  font-size: 12px;
  color: #888;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  line-height: 34px;
  margin-top: 4px;
  letter-spacing: .5px;
}

.stat-extra {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.quick-courses {
  min-height: 48px;
}

.quick-course-tag {
  cursor: pointer;
  margin-bottom: 6px;
  transition: all .25s;
}

.quick-course-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, .15);
}

.quick-course-tag.disabled {
  cursor: not-allowed;
  opacity: 0.55;
  box-shadow: none !important;
}

.error-box {
  margin-top: 20px;
}

/* 继续学习列表样式（修正嵌套） */
.progress-list {
  margin-top: 4px;
}

.progress-item {
  transition: background .25s;
  border-radius: 6px;
}

.progress-item:hover {
  background: #f7faff;
}

.progress-actions {
  display: flex;
  align-items: center;
}

/* 过渡动画 */
.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: all .35s ease;
}

.fade-scale-enter {
  opacity: 0;
  transform: scale(.95);
}

.fade-scale-leave-to {
  opacity: 0;
  transform: scale(.95);
}

/* 面板视觉强化与区分 */
.panel {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 6px 18px rgba(0, 0, 0, .06);
}

.panel .panel-title {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

/* .panel .panel-title .anticon {
  color: #1890ff;
}

.learning-panel {
  background: linear-gradient(135deg, #f0f7ff 0%, #ffffff 85%);
  border: 1px solid #e6f2ff;
}

.quick-panel {
  background: linear-gradient(135deg, #fff9f0 0%, #ffffff 85%);
  border: 1px solid #ffeddc;
} */

.learning-panel::before,
.quick-panel::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
}

.learning-panel::before {
  background: linear-gradient(180deg, #69c0ff, #2f54eb);
}

.quick-panel::before {
  background: linear-gradient(180deg, #ffc53d, #fa8c16);
}
</style>