<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="课程名称">
              <a-input placeholder="请输入课程名称" v-model="queryParam.courseName"></a-input>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="课程分类名称">
              <j-dict-select-tag ref="courseTypeSelect" placeholder="请选择课程分类名称"
                v-model="queryParam.courseTypeId" @change="handleCourseTypeChange"
                dictCode="cloud_computing_course_type,course_type_name,id" />
            </a-form-item>
          </a-col>
          <template v-if="toggleSearchStatus">
            <a-col :xl="6" :lg="7" :md="8" :sm="24">
              <a-form-item label="关联负责教师">
                <j-dict-select-tag placeholder="请选择关联负责教师" v-model="queryParam.teacherId"
                  dictCode="sys_user,realname,id" />
              </a-form-item>
            </a-col>
          </template>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
              <a-button type="primary" @click="searchQuery" icon="search">查询</a-button>
              <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">重置</a-button>
              <a @click="handleToggleSearch" style="margin-left: 8px">
                {{ toggleSearchStatus ? '收起' : '展开' }}
                <a-icon :type="toggleSearchStatus ? 'up' : 'down'" />
              </a>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <!-- 查询区域-END -->

    <!-- 操作按钮区域 -->
    <div class="table-operator">
      <a-button @click="handleAdd" type="primary" icon="plus">新增</a-button>
      <a-button type="primary" icon="download" @click="handleExportXls('课程表')">导出</a-button>
      <a-upload name="file" :showUploadList="false" :multiple="false" :headers="tokenHeader" :action="importExcelUrl"
        @change="handleImportExcel">
        <a-button type="primary" icon="import">导入</a-button>
      </a-upload>
      <!-- 高级查询区域 -->
      <j-super-query :fieldList="superFieldList" ref="superQueryModal"
        @handleSuperQuery="handleSuperQuery"></j-super-query>
      <a-dropdown v-if="selectedRowKeys.length > 0">
        <a-menu slot="overlay">
          <a-menu-item key="1" @click="batchDel"><a-icon type="delete" />删除</a-menu-item>
        </a-menu>
        <a-button style="margin-left: 8px"> 批量操作 <a-icon type="down" /></a-button>
      </a-dropdown>
    </div>

    <!-- table区域-begin -->
    <div>
      <div class="ant-alert ant-alert-info" style="margin-bottom: 16px;">
        <i class="anticon anticon-info-circle ant-alert-icon"></i> 已选择 <a style="font-weight: 600">{{
          selectedRowKeys.length
          }}</a>项
        <a style="margin-left: 24px" @click="onClearSelected">清空</a>
      </div>

      <a-table ref="table" size="middle" :scroll="{ x: true }" bordered rowKey="id" :columns="columns"
        :dataSource="dataSource" :pagination="ipagination" :loading="loading"
        :rowSelection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }" class="j-table-force-nowrap"
        @change="handleTableChange">

        <template slot="htmlSlot" slot-scope="text">
          <div v-html="text"></div>
        </template>
        <template slot="imgSlot" slot-scope="text,record">
          <span v-if="!text" style="font-size: 12px;font-style: italic;">无图片</span>
          <img v-else :src="getImgView(text)" :preview="record.id" height="25px" alt=""
            style="max-width:80px;font-size: 12px;font-style: italic;" />
        </template>
        <template slot="fileSlot" slot-scope="text">
          <span v-if="!text" style="font-size: 12px;font-style: italic;">无文件</span>
          <a-button v-else :ghost="true" type="primary" icon="download" size="small" @click="downloadFile(text)">
            下载
          </a-button>
        </template>

        <span slot="action" slot-scope="text, record">
          <a @click="handleEdit(record)">编辑</a>

          <a-divider type="vertical" />
          <a-dropdown>
            <a class="ant-dropdown-link">更多 <a-icon type="down" /></a>
            <a-menu slot="overlay">
              <a-menu-item>
                <a @click="handleDetail(record)">详情</a>
              </a-menu-item>
              <a-menu-item>
                <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)">
                  <a>删除</a>
                </a-popconfirm>
              </a-menu-item>
            </a-menu>
          </a-dropdown>
        </span>

      </a-table>
    </div>

    <cloud-computing-course-modal ref="modalForm" @ok="modalFormOk"></cloud-computing-course-modal>
  </a-card>
</template>

<script>

import '@/assets/less/TableExpand.less'
import { mixinDevice } from '@/utils/mixin'
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import CloudComputingCourseModal from './modules/CloudComputingCourseModal'
import { filterMultiDictText } from '@/components/dict/JDictSelectUtil'

export default {
  name: 'CloudComputingCourseList',
  mixins: [JeecgListMixin, mixinDevice],
  components: {
    CloudComputingCourseModal
  },
  data() {
    return {
      description: '课程表管理页面',
      courseTypeNameKeyword: '',
      // 表头
      columns: [
        {
          title: '#',
          dataIndex: '',
          key: 'rowIndex',
          width: 60,
          align: "center",
          customRender: function (t, r, index) {
            return parseInt(index) + 1;
          }
        },
        {
          title: '课程名称',
          align: "center",
          dataIndex: 'courseName'
        },
        {
          title: '课程分类名称',
          align: "center",
          dataIndex: 'courseTypeId_dictText'
        },
        {
          title: '课程标签',
          align: "center",
          dataIndex: 'courseTag'
        },
        {
          title: '关联负责教师',
          align: "center",
          dataIndex: 'teacherId_dictText'
        },
        {
          title: '总课时（h）',
          align: "center",
          dataIndex: 'courseHours'
        },
        {
          title: '课程状态',
          align: "center",
          dataIndex: 'courseStatus_dictText'
        },
        {
          title: '操作',
          dataIndex: 'action',
          align: "center",
          fixed: "right",
          width: 147,
          scopedSlots: { customRender: 'action' }
        }
      ],
      url: {
        list: "/study/cloudComputingCourse/list",
        delete: "/study/cloudComputingCourse/delete",
        deleteBatch: "/study/cloudComputingCourse/deleteBatch",
        exportXlsUrl: "/study/cloudComputingCourse/exportXls",
        importExcelUrl: "study/cloudComputingCourse/importExcel",

      },
      dictOptions: {},
      superFieldList: [],
    }
  },
  created() {
    this.getSuperFieldList();
  },
  computed: {
    importExcelUrl: function () {
      return `${window._CONFIG['domianURL']}/${this.url.importExcelUrl}`;
    },
  },
  methods: {
    initDictConfig() {
    },
    getSuperFieldList() {
      let fieldList = [];
      fieldList.push({ type: 'string', value: 'courseName', text: '课程名称', dictCode: '' })
      fieldList.push({ type: 'string', value: 'courseTypeId', text: '课程分类名称', dictCode: "cloud_computing_course_type,course_type_name,id" })
      fieldList.push({ type: 'string', value: 'courseTag', text: '课程标签', dictCode: '' })
      fieldList.push({ type: 'string', value: 'teacherId', text: '关联负责教师', dictCode: "sys_user,realname,id" })
      fieldList.push({ type: 'int', value: 'courseHours', text: '总课时（h）', dictCode: '' })
      fieldList.push({ type: 'int', value: 'courseStatus', text: '课程状态', dictCode: 'course_status' })
      this.superFieldList = fieldList
    },
    handleCourseTypeChange(value) {
      if (!value) {
        this.courseTypeNameKeyword = ''
        return
      }
      const options = (this.$refs.courseTypeSelect && this.$refs.courseTypeSelect.getCurrentDictOptions
        ? this.$refs.courseTypeSelect.getCurrentDictOptions()
        : []) || []
      const match = options.find(item => item.value === value)
      const label = match ? (match.text || match.label || '') : ''
      this.courseTypeNameKeyword = label
    },
    getQueryParams() {
      const params = JeecgListMixin.methods.getQueryParams.call(this)
      if (params.courseName && params.courseName.trim()) {
        params.courseName = `*${params.courseName.trim()}*`
      }
      if (this.courseTypeNameKeyword && this.courseTypeNameKeyword.trim()) {
        params.courseTypeName = `*${this.courseTypeNameKeyword.trim()}*`
      }
      return params
    },
    searchReset() {
      this.courseTypeNameKeyword = ''
      JeecgListMixin.methods.searchReset.call(this)
    }
  }
}
</script>
<style scoped>
@import '~@assets/less/common.less';
</style>