<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
        <a-row>
          <a-col :span="24">
            <a-form-model-item label="课程名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="courseName">
              <a-input v-model="model.courseName" placeholder="请输入课程名称"></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="课程分类名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="courseTypeId">
              <j-dict-select-tag type="list" v-model="model.courseTypeId"
                dictCode="cloud_computing_course_type,course_type_name,id" placeholder="请选择课程分类名称" />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="课程标签" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="courseTag">
              <a-input v-model="model.courseTag" placeholder="请输入课程标签"></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="关联负责教师" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="teacherId">
              <j-dict-select-tag type="list" v-model="model.teacherId" dictCode="sys_user,realname,id"
                placeholder="请选择关联负责教师" />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="总课时（h）" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="courseHours">
              <a-input-number v-model="model.courseHours" placeholder="请输入总课时（h）" style="width: 100%" />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="课程状态" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="courseStatus">
              <j-dict-select-tag type="radio" v-model="model.courseStatus" dictCode="course_status"
                placeholder="请选择课程状态" />
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
  name: 'CloudComputingCourseForm',
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
        courseName: [
          { required: true, message: '请输入课程名称!' },
        ],
        teacherId: [
          { required: true, message: '请输入关联负责教师!' },
        ],
        courseHours: [
          { required: true, message: '请输入总课时（h）!' },
        ],
        courseStatus: [
          { required: true, message: '请输入课程状态!' },
        ],
      },
      url: {
        add: "/study/cloudComputingCourse/add",
        edit: "/study/cloudComputingCourse/edit",
        queryById: "/study/cloudComputingCourse/queryById"
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