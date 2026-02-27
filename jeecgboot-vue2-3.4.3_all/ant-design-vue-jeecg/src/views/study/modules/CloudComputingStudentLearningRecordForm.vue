<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
        <a-row>
          <a-col :span="24">
            <a-form-model-item label="课程名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="courseId">
              <j-search-select-tag v-model="model.courseId" dict="cloud_computing_course,course_name,id" />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="学生名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="userId">
              <j-search-select-tag v-model="model.userId" dict="sys_user,realname,id" />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="学习资源名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="contentId">
              <j-search-select-tag v-model="model.contentId" dict="cloud_computing_course_resource,resource_name,id" />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="学习状态" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="learningStatus">
              <j-dict-select-tag type="list" v-model="model.learningStatus" dictCode="learning_status"
                placeholder="请选择学习状态" />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="累计学习时长(秒)" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="learningTime">
              <a-input-number v-model="model.learningTime" placeholder="请输入累计学习时长(秒)" style="width: 100%" />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="最后一次学习时间" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="lastLearnTime">
              <j-date placeholder="请选择最后一次学习时间" v-model="model.lastLearnTime" style="width: 100%" />
            </a-form-model-item>
          </a-col>
        </a-row>
      </a-form-model>
    </j-form-container>
  </a-spin>
</template>

<script>

import { httpAction, getAction } from '@/api/manage'
import { validateDuplicateValue } from '@/utils/util'

export default {
  name: 'CloudComputingStudentLearningRecordForm',
  components: {
  },
  props: {
    //表单禁用
    disabled: {
      type: Boolean,
      default: false,
      required: false
    }
  },
  data() {
    return {
      model: {
      },
      labelCol: {
        xs: { span: 24 },
        sm: { span: 5 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 16 },
      },
      confirmLoading: false,
      validatorRules: {
        courseId: [
          { required: true, message: '请输入课程名称!' },
        ],
        userId: [
          { required: true, message: '请输入学生名称!' },
        ],
        contentId: [
          { required: true, message: '请输入学习资源名称!' },
        ],
        learningStatus: [
          { required: true, message: '请输入学习状态!' },
        ],
        learningTime: [
          { required: true, message: '请输入累计学习时长(秒)!' },
        ],
      },
      url: {
        add: "/study/cloudComputingStudentLearningRecord/add",
        edit: "/study/cloudComputingStudentLearningRecord/edit",
        queryById: "/study/cloudComputingStudentLearningRecord/queryById"
      }
    }
  },
  computed: {
    formDisabled() {
      return this.disabled
    },
  },
  created() {
    //备份model原始值
    this.modelDefault = JSON.parse(JSON.stringify(this.model));
  },
  methods: {
    add() {
      this.edit(this.modelDefault);
    },
    edit(record) {
      this.model = Object.assign({}, record);
      this.visible = true;
    },
    submitForm() {
      const that = this;
      // 触发表单验证
      this.$refs.form.validate(valid => {
        if (valid) {
          that.confirmLoading = true;
          let httpurl = '';
          let method = '';
          if (!this.model.id) {
            httpurl += this.url.add;
            method = 'post';
          } else {
            httpurl += this.url.edit;
            method = 'put';
          }
          httpAction(httpurl, this.model, method).then((res) => {
            if (res.success) {
              that.$message.success(res.message);
              that.$emit('ok');
            } else {
              that.$message.warning(res.message);
            }
          }).finally(() => {
            that.confirmLoading = false;
          })
        }

      })
    },
  }
}
</script>