<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
        </a-row>
      </a-form>
    </div>
    <!-- 查询区域-END -->

    <!-- 操作按钮区域 -->
    <div class="table-operator">
      <a-button @click="handleAdd" type="primary" icon="plus">新增</a-button>
      <a-button type="primary" icon="download" @click="handleExportXls('学生学习记录表')">导出</a-button>
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
          selectedRowKeys.length }}</a>项
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

    <cloud-computing-student-learning-record-modal ref="modalForm"
      @ok="modalFormOk"></cloud-computing-student-learning-record-modal>
  </a-card>
</template>

<script>

import '@/assets/less/TableExpand.less'
import { mixinDevice } from '@/utils/mixin'
import { JeecgListMixin } from '@/mixins/JeecgListMixin'
import CloudComputingStudentLearningRecordModal from './modules/CloudComputingStudentLearningRecordModal'
import { filterMultiDictText } from '@/components/dict/JDictSelectUtil'

export default {
  name: 'CloudComputingStudentLearningRecordList',
  mixins: [JeecgListMixin, mixinDevice],
  components: {
    CloudComputingStudentLearningRecordModal
  },
  data() {
    return {
      description: '学生学习记录表管理页面',
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
          dataIndex: 'courseId_dictText'
        },
        {
          title: '学生名称',
          align: "center",
          dataIndex: 'userId_dictText'
        },
        {
          title: '学习资源名称',
          align: "center",
          dataIndex: 'contentId_dictText'
        },
        {
          title: '学习状态',
          align: "center",
          dataIndex: 'learningStatus_dictText'
        },
        {
          title: '累计学习时长(秒)',
          align: "center",
          dataIndex: 'learningTime'
        },
        {
          title: '最后一次学习时间',
          align: "center",
          dataIndex: 'lastLearnTime',
          customRender: function (text) {
            return !text ? "" : (text.length > 10 ? text.substr(0, 10) : text)
          }
        },
        {
          title: '记录创建日期',
          align: "center",
          dataIndex: 'createTime',
          customRender: function (text) {
            return !text ? "" : (text.length > 10 ? text.substr(0, 10) : text)
          }
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
        list: "/study/cloudComputingStudentLearningRecord/list",
        delete: "/study/cloudComputingStudentLearningRecord/delete",
        deleteBatch: "/study/cloudComputingStudentLearningRecord/deleteBatch",
        exportXlsUrl: "/study/cloudComputingStudentLearningRecord/exportXls",
        importExcelUrl: "study/cloudComputingStudentLearningRecord/importExcel",

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
      fieldList.push({ type: 'sel_search', value: 'courseId', text: '课程名称', dictTable: "cloud_computing_course", dictText: 'course_name', dictCode: 'id' })
      fieldList.push({ type: 'sel_search', value: 'userId', text: '学生名称', dictTable: "sys_user", dictText: 'realname', dictCode: 'id' })
      fieldList.push({ type: 'sel_search', value: 'contentId', text: '学习资源名称', dictTable: "cloud_computing_course_resource", dictText: 'resource_name', dictCode: 'id' })
      fieldList.push({ type: 'string', value: 'learningStatus', text: '学习状态', dictCode: 'learning_status' })
      fieldList.push({ type: 'int', value: 'learningTime', text: '累计学习时长(秒)', dictCode: '' })
      fieldList.push({ type: 'date', value: 'lastLearnTime', text: '最后一次学习时间' })
      fieldList.push({ type: 'date', value: 'createTime', text: '记录创建日期' })
      this.superFieldList = fieldList
    }
  }
}
</script>
<style scoped>
@import '~@assets/less/common.less';
</style>