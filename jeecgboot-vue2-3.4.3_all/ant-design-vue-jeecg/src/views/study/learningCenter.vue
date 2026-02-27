<template>
  <div class="learning-center">
    <!-- 顶部搜索区域 -->
    <div class="top-section">
      <div class="title">
        <p>课程列表</p>
      </div>
      <div class="search-wrapper">
        <a-input-search placeholder="请输入课程名称" v-model="searchValue" enter-button @search="handleSearch"
          class="search-input" />
      </div>
    </div>


    <!-- 无数据提示 -->
    <div v-if="!isSearching && hasNoCourseData" class="no-data-container">
      <a-empty :description="noDataDescription" />
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-container">
      <a-spin tip="加载中..." />
    </div>

    <!-- 课程列表 -->
    <div v-else class="course-list">
      <div v-for="category in courseTypes" :key="`category-${category.id}`" class="course-category"
        v-if="categoryData[category.id] && categoryData[category.id].length > 0">
        <!-- 课程分类标题 -->
        <div class="category-title">
          {{ category.courseTypeName }}
          <span class="course-count">（{{ categoryPagination[category.id].total }}个课程）</span>
        </div>

        <!-- 课程内容 -->
        <div class="course-content">
          <!-- 单个课程项 -->
          <div v-for="(lesson, lessonIndex) in categoryData[category.id]" :key="lesson.id" class="lesson-item"
            :class="{ 'disabled': !lesson.available }">
            <!-- 课程头部信息 -->
            <div class="lesson-header">
              <div class="lesson-name">
                <span class="lesson-index">{{ getGlobalIndex(lessonIndex, category.id) }}:</span>
                <span>
                  <a @click="handleCourseNameClick(lesson)">{{ lesson.courseName }}</a>
                </span>
              </div>
              <div class="lesson-tag" :class="lesson.courseTag === '必修' ? 'required' : 'elective'">
                {{ lesson.courseTag }}
              </div>
            </div>

            <!-- 课程资源统计 -->
            <div class="lesson-resources">
              <div v-for="resourceType in resourceTypes" :key="resourceType.key" class="resource-item">
                <a href="javascript:void(0)" @click="handleResourceClick(lesson, resourceType.key)"
                  class="resource-link" :class="{ 'disabled': !hasResources(lesson, resourceType.key) }">
                  <span class="resource-text">{{ resourceType.name }}</span>
                  <span class="resource-count">
                    {{ getResourceCount(lesson, resourceType.key) }}
                  </span>
                </a>
              </div>
            </div>

            <!-- 课程未开始遮罩 -->
            <div v-if="!lesson.available" class="lesson-mask">
              <div class="mask-text">课程暂未开始</div>
            </div>
          </div>
        </div>

        <!-- 分类内部分页组件 -->
        <div class="category-pagination"
          v-if="categoryPagination[category.id] && categoryPagination[category.id].total > categoryPagination[category.id].pageSize">
          <a-pagination v-model="categoryPagination[category.id].pageNo"
            :page-size="categoryPagination[category.id].pageSize" :total="categoryPagination[category.id].total"
            @change="handleCategoryPageChange($event, category.id)" show-quick-jumper
            :show-total="(total, range) => `${range[0]}-${range[1]} 共 ${total} 条`" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getAction } from '@/api/manage'

// 资源类型配置
const RESOURCE_TYPES = [
  { key: 'video', name: '视频', field: 'videoCount' },
  { key: 'lecture', name: '讲义', field: 'lectureCount' },
  { key: 'data', name: '资源', field: 'resourceCount' }
]

// API接口配置
const API_URLS = {
  // courseList: '/study/cloudComputingCourse/studentListClass',
  courseList: '/study/cloudComputingCourse/list',
  courseTypeList: '/study/cloudComputingCourseType/list',
  resourceCounts: '/study/cloudComputingCourseResource/counts'
}

export default {
  name: 'LearningCenter',

  data() {
    return {
      // 搜索相关
      searchValue: '',
      searchFocus: false,
      isSearching: false, // 是否正在搜索
      loading: false, // 加载状态

      // 课程数据
      courseTypes: [],

      // 按分类存储数据和分页状态
      categoryPagination: {},
      categoryData: {},

      // API配置
      urls: API_URLS,
    }
  },

  computed: {
    noDataDescription() {
      return this.searchValue ? '未找到相关课程' : '暂无课程数据'
    },

    resourceTypes() {
      return RESOURCE_TYPES
    },

    // 检查是否有课程数据
    hasNoCourseData() {
      return Object.values(this.categoryData).every(courses => courses.length === 0)
    },

    /**
     * 根据搜索条件过滤显示的课程数据
     */
    displayedItems() {
      if (!this.isSearching || !this.searchValue.trim()) {
        return []
      }

      const searchText = this.searchValue.trim().toLowerCase()
      const results = []

      // 遍历所有分类数据，过滤出匹配的课程
      Object.values(this.categoryData).forEach(courses => {
        courses.forEach(course => {
          if (course.courseName && course.courseName.toLowerCase().includes(searchText)) {
            results.push(course)
          }
        })
      })

      return results
    }
  },

  watch: {
    /**
     * 监听搜索值变化，使用防抖处理
     */
    searchValue: {
      handler(newVal) {
        clearTimeout(this.searchTimer);
        this.searchTimer = setTimeout(() => {
          // 搜索逻辑
          if (newVal.trim()) {
            this.isSearching = true;
          } else {
            this.isSearching = false;
            // 重新加载所有分类数据
            this.loadAllCategoryData();
          }
        }, 500);
      },
      immediate: false
    }
  },

  methods: {
    /**
     * 课程名称跳转
     */
    handleCourseNameClick(lesson) {
      const params = {
        courseId: lesson.id,
        resourceType: 'video'
      }
      this.$router.push({
        path: '/study/CourseDetailsFrom',
        query: params
      })
    },
    /**
     * 获取全局序号（考虑分页）
     */
    getGlobalIndex(lessonIndex, categoryId) {
      if (this.isSearching) {
        return lessonIndex + 1
      }

      // 计算在当前页中的全局序号
      const pagination = this.categoryPagination[categoryId]
      return (pagination.pageNo - 1) * pagination.pageSize + lessonIndex + 1
    },

    handleFocusBlur(e) {
      this.searchFocus = e.target === document.activeElement
    },

    /**
     * 处理搜索操作
     */
    async handleSearch() {
      if (!this.searchValue.trim()) {
        this.clearSearch();
        return;
      }

      this.isSearching = true;


      // 重置所有分类到第一页
      Object.keys(this.categoryPagination).forEach(categoryId => {
        this.categoryPagination[categoryId].pageNo = 1;
      });

      // 重新加载所有分类数据（带搜索条件）
      await this.loadAllCategoryData();
    },

    /**
     * 清除搜索
     */
    async clearSearch() {
      this.searchValue = '';
      this.isSearching = false;

      // 重置所有分类到第一页
      Object.keys(this.categoryPagination).forEach(categoryId => {
        this.categoryPagination[categoryId].pageNo = 1;
      });

      // 重新加载所有分类数据（不带搜索条件）
      await this.loadAllCategoryData();
    },

    hasResources(lesson, resourceType) {
      const typeConfig = RESOURCE_TYPES.find(type => type.key === resourceType)
      return typeConfig ? (lesson[typeConfig.field] || 0) > 0 : false
    },

    getResourceCount(lesson, resourceType) {
      const typeConfig = RESOURCE_TYPES.find(type => type.key === resourceType)
      return typeConfig ? (lesson[typeConfig.field] || 0) : 0
    },

    handleResourceClick(lesson, resourceType) {
      if (!this.hasResources(lesson, resourceType)) {
        this.$message.info('暂无相关资源')
        return
      }

      const params = {
        courseId: lesson.id,
        resourceType: resourceType,
      }



      this.$router.push({
        path: '/study/CourseDetailsFrom',
        query: params
      })
    },

    /**
     * 初始化数据加载
     */
    async initData() {
      this.loading = true;
      try {
        await this.loadCourseTypes()
        await this.loadAllCategoryData()
      } catch (error) {
        console.error('数据初始化失败:', error)
        this.$message.error('数据加载失败，请刷新页面重试')
      } finally {
        this.loading = false;
      }
    },

    /**
     * 加载课程分类数据（不分页）
     */
    async loadCourseTypes() {
      try {
        const response = await getAction(this.urls.courseTypeList, {
          pageNo: 1,
          pageSize: 1000
        })

        if (response && response.success) {
          const result = response.result
          const records = this.extractRecords(result)

          this.courseTypes = records.map(item => ({
            id: item.id,
            courseTypeName: item.courseTypeName
          }))

          // 初始化分页状态
          this.courseTypes.forEach(type => {
            this.$set(this.categoryPagination, type.id, {
              pageNo: 1,
              pageSize: 10,
              total: 0
            })
            this.$set(this.categoryData, type.id, [])
          })


        }
      } catch (error) {
        console.error('加载课程分类失败:', error)
        this.courseTypes = []
      }
    },

    /**
     * 按分类加载课程数据（支持搜索）
     */
    async loadCourseListByCategory(categoryId) {
      try {
        const pagination = this.categoryPagination[categoryId];

        // 构建请求参数，添加分类过滤
        const params = {
          pageNo: pagination.pageNo,
          pageSize: pagination.pageSize,
          courseTypeId: categoryId // 添加分类过滤
        };

        // 如果正在搜索，添加搜索参数
        if (this.isSearching && this.searchValue.trim()) { 
         params.courseName = "*" + this.searchValue.trim() + "*";
        }



        const response = await getAction(this.urls.courseList, params);

        if (response && response.success) {
          const result = response.result;
          const records = this.extractRecords(result);

          // 更新该分类的数据和分页信息
          this.$set(this.categoryData, categoryId, records.map(course => this.formatCourseData(course)));
          this.$set(this.categoryPagination[categoryId], 'total', result.total || 0);



          // 加载资源统计
          this.loadResourceCountsForCategory(categoryId);
        }
      } catch (error) {
        console.error(`加载分类 ${categoryId} 的课程列表失败:`, error);
        this.$set(this.categoryData, categoryId, []);
      }
    },


    /**
     * 初始化所有分类数据
     */
    async loadAllCategoryData() {
      if (this.courseTypes.length === 0) {
        await this.loadCourseTypes();
      }

      // 获取所有分类ID
      const categoryIds = this.courseTypes.map(type => type.id);

      // 并行加载所有分类数据
      await Promise.all(
        categoryIds.map(id => this.loadCourseListByCategory(id))
      );
    },

    extractRecords(result) {
      if (result && Array.isArray(result.records)) {
        return result.records
      } else if (Array.isArray(result)) {
        return result
      }
      return []
    },

    formatCourseData(course) {
      return {
        id: course.id,
        courseName: course.courseName || '未命名课程',
        courseTag: course.courseTag || '选修',
        videoCount: course.videoCount || 0,
        lectureCount: course.lectureCount || 0,
        resourceCount: course.resourceCount || 0,
        courseTypeId_dictText: course.courseTypeId_dictText, // 保存分类信息
        available: course.courseStatus === 1
      }
    },

    /**
     * 按分类加载资源统计数据
     */
    async loadResourceCountsForCategory(categoryId) {
      const courseIds = this.categoryData[categoryId].map(course => course.id);
      if (courseIds.length === 0) return;

      try {
        const results = await Promise.all(
          courseIds.map(courseId => this.fetchResourceCount(courseId))
        );
        this.processResourceCounts(results, categoryId);
      } catch (error) {
        console.error(`加载分类 ${categoryId} 资源统计失败:`, error);
      }
    },

    async fetchResourceCount(courseId) {
      try {
        const response = await getAction(this.urls.resourceCounts, { id: courseId })
        return { courseId, response }
      } catch (error) {
        console.error(`获取课程 ${courseId} 的资源统计失败:`, error)
        return { courseId, response: null }
      }
    },

    processResourceCounts(results, categoryId) {
      const resourceTypeMap = {
        '1': 'videoCount',
        '2': 'lectureCount',
        '3': 'resourceCount',
        '4': 'resourceCount'
      }

      results.forEach(({ courseId, response }) => {
        if (!response || !response.success || !response.result) return

        const counts = this.calculateResourceCounts(response.result, resourceTypeMap)
        this.updateCourseResourceCount(courseId, counts, categoryId)
      })
    },

    calculateResourceCounts(resourceMap, typeMap) {
      const counts = { videoCount: 0, lectureCount: 0, resourceCount: 0 }
      Object.keys(resourceMap).forEach(typeId => {
        const count = Number(resourceMap[typeId]) || 0
        const field = typeMap[typeId] || 'resourceCount'
        counts[field] += count
      })
      return counts
    },

    updateCourseResourceCount(courseId, counts, categoryId) {
      const courses = this.categoryData[categoryId]
      const courseIndex = courses.findIndex(course => course.id === courseId)
      if (courseIndex > -1) {
        const updatedCourse = { ...courses[courseIndex], ...counts }
        this.$set(this.categoryData[categoryId], courseIndex, updatedCourse)
      }
    },

    /**
     * 分类分页事件处理
     */
    handleCategoryPageChange(page, categoryId) {

      this.categoryPagination[categoryId].pageNo = page;
      this.loadCourseListByCategory(categoryId);
      // 滚动到该分类区域
      this.scrollToCategory(categoryId);
    },

    handleCategoryPageSizeChange(current, size, categoryId) {

      this.categoryPagination[categoryId].pageNo = 1;
      this.categoryPagination[categoryId].pageSize = size;
      this.loadCourseListByCategory(categoryId);
    },

    /**
     * 滚动到指定分类区域
     */
    scrollToCategory(categoryId) {
      setTimeout(() => {
        const element = document.querySelector(`.course-category[data-category="${categoryId}"]`);
        if (element) {
          element.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
      }, 100);
    }
  },

  async mounted() {
    document.addEventListener('click', this.handleFocusBlur)
    await this.initData()
  },

  beforeDestroy() {
    document.removeEventListener('click', this.handleFocusBlur)
  }
}
</script>

<style scoped>
/* 样式保持不变，与之前相同 */
.learning-center {
  padding: 0.1vw;
  min-height: 100vh;
}

/* 顶部区域样式 */
.top-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2vh;
  flex-wrap: wrap;
  gap: 1vw;
  background-color: #ffffff;
  border-radius: 4px;
  padding: 0.4rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
}

.top-section .title {
  flex: 1;
  min-width: 120px;
}

.top-section .title p {
  font-size: 1.2rem;
  font-weight: bold;
  margin: 0;
  color: #333;
}

.search-wrapper {
  flex: 0 0 30vw;
  max-width: 30vw;
  min-width: 200px;
}

.search-wrapper .search-input {
  width: 100%;
  font-size: 18px;
}

/* 课程分类样式 */
.course-category {
  width: 100%;
  margin-bottom: 2vh;
  background-color: #ffffff;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
}

.category-title {
  font-size: 1.4rem;
  font-weight: bold;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #e8e8e8;
  color: #333;
}

/* 课程内容网格布局 */
.course-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}

/* 课程项样式 */
.lesson-item {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  position: relative;
  background-color: #fff;
  transition: all 0.3s ease;
}

.lesson-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.lesson-item.disabled {
  opacity: 0.7;
}

/* 课程头部样式 */
.lesson-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.lesson-name {
  font-size: 1rem;
  font-weight: 500;
  color: #333;
  flex: 1;
}

a {
  color: black;
}

.lesson-index {
  margin-right: 0.5rem;
}

.lesson-tag {
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
  white-space: nowrap;
}

.lesson-tag.required {
  background-color: #1890ff;
  color: white;
}

.lesson-tag.elective {
  background-color: #52c41a;
  color: white;
}

/* 资源区域样式 */
.lesson-resources {
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.resource-item {
  display: flex;
  align-items: center;
}

.resource-link {
  text-decoration: none;
  color: inherit;
  display: flex;
  align-items: center;
  transition: color 0.3s;
}

.resource-link:hover:not(.disabled) {
  text-decoration: underline;
  color: #1890ff;
}

.resource-link.disabled {
  color: #ccc;
  cursor: not-allowed;
}

.resource-link.disabled:hover {
  text-decoration: none;
  color: #ccc;
}

.resource-text {
  margin-right: 0.3rem;
  color: #666;
  font-size: 0.9rem;
}

.resource-count {
  color: #333;
  font-weight: 500;
  font-size: 0.9rem;
}

/* 课程遮罩样式 */
.lesson-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
}

.mask-text {
  color: #999;
  font-size: 1rem;
}

/* 分类分页样式 */
.category-pagination {
  display: flex;
  justify-content: center;
  margin-top: 1rem;
  padding: 1rem;
  background-color: #f9f9f9;
  border-radius: 8px;
}

/* 无数据容器 */
.no-data-container {
  text-align: center;
  padding: 4rem 2rem;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
}

.search-tips {
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 6px;
  padding: 12px 16px;
  margin: 1rem 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-count {
  font-size: 0.9rem;
  color: #666;
  font-weight: normal;
  margin-left: 8px;
}

/* 加载中样式 */
.loading-container {
  text-align: center;
  padding: 50px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .course-content {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .top-section {
    flex-direction: column;
    align-items: stretch;
  }

  .search-wrapper {
    max-width: 100%;
    flex: 1;
  }

  .course-content {
    grid-template-columns: 1fr;
  }
}
</style>