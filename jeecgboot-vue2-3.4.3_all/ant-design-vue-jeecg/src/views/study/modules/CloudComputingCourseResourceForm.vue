<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
        <a-row>
          <a-col :span="24">
            <a-form-model-item label="学习内容类型" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="resourceType">
              <j-dict-select-tag type="list" v-model="model.resourceType" dictCode="resource_type"
                :disabled="courseLocked" placeholder="请选择学习内容类型" />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="课程名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="courseId">
              <j-search-select-tag v-model="model.courseId" dict="cloud_computing_course,course_name,id"
                :disabled="courseLocked" />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="资源名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="resourceName">
              <a-input v-model="model.resourceName" placeholder="请输入资源名称"></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="资源存储地址" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="resourceUrl">
              <j-upload v-model="model.resourceUrl" :number="1" :fileType="allowedFileTypes"
                @change="handleUploadChange" :beforeUpload="beforeUpload"></j-upload>
              <div class="file-type-hint" v-if="model.resourceType">
                <a-alert :message="getFileTypeHintMessage()" type="info" show-icon />
              </div>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="资源排序" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="resourceSort">
              <a-input-number v-model="model.resourceSort" placeholder="请输入资源排序" style="width: 100%" />
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
  name: 'CloudComputingCourseResourceForm',
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
        resourceType: '',
        courseId: '',
        resourceName: '',
        resourceUrl: '',
        resourceSort: 1
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
      courseLocked: false, // 用于控制课程名称是否锁定
      validatorRules: {
        resourceType: [
          { required: true, message: '请选择学习内容类型!' },
        ],
        courseId: [
          { required: true, message: '请选择课程名称!' },
        ],
        resourceName: [
          { required: true, message: '请输入资源名称!' },
        ],
        resourceUrl: [
          { required: true, message: '资源上传类型错误!' },
        ],
        resourceSort: [
          { required: true, message: '请输入资源排序!' },
        ],
      },
      url: {
        add: "/study/cloudComputingCourseResource/add",
        edit: "/study/cloudComputingCourseResource/edit",
        queryById: "/study/cloudComputingCourseResource/queryById"
      }
    }
  },
  computed: {
    formDisabled() {
      return this.disabled
    },
    allowedFileTypes() {
      switch (this.model.resourceType) {
        case 1: return '.mp4,.avi,.mov,.wmv,.flv,.mkv';
        case 2: return '.pdf,.pptx';
        case 3: return '.pdf,.doc,.docx,.zip,.rar';
        default: return '.pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.zip,.rar,.jpg,.jpeg,.png,.gif,.mp4,.avi,.mov';
      }
    }
  },
  watch: {
    // 监听资源类型变化，重置文件上传
    'model.resourceType'(newVal, oldVal) {
      if (oldVal && newVal !== oldVal) {
        // 资源类型改变时，清空已上传的文件
        this.model.resourceUrl = '';
      }
    }
  },
  created() {
    //备份model原始值
    this.modelDefault = JSON.parse(JSON.stringify(this.model));
  },
  methods: {
    add(defaults) {
      // 重置锁定状态
      this.courseLocked = false;

      // 使用默认的空模型
      let record = JSON.parse(JSON.stringify(this.modelDefault));

      // 如果有传入默认值，则处理默认值
      if (defaults) {
        // 设置课程信息并锁定
        if (defaults.courseId) {
          record.courseId = defaults.courseId;
          this.courseLocked = true; // 锁定课程选择
        }

        // 设置资源类型
        if (defaults.resourceType) {
          record.resourceType = defaults.resourceType;
        }

        // 合并其他默认值
        this.model = Object.assign({}, record, defaults);
      } else {
        // 没有传入默认值，使用空模型
        this.model = Object.assign({}, record);
      }

      // 确保字段映射正确
      this.model.resourceName = this.model.resourceName || this.model.name || '';
      this.model.resourceUrl = this.model.resourceUrl || this.model.url || '';

      this.visible = true;
    },
    edit(record) {
      // 重置锁定状态
      this.courseLocked = false;

      // 如果有传入记录，则使用记录值
      if (record && Object.keys(record).length > 0) {
        // 确保字段映射正确
        const mappedRecord = {
          ...record,
          resourceName: record.resourceName || record.name || '',
          resourceUrl: record.resourceUrl || record.url || ''
        };
        this.model = Object.assign({}, mappedRecord);
      } else {
        // 否则使用默认空模型
        this.model = Object.assign({}, this.modelDefault);
      }
      this.visible = true;
    },
    /**
     * 获取文件类型提示信息
     * @returns {string} 文件类型提示信息
     */
    getFileTypeHintMessage() {
      const typeMap = {
        1: '支持的视频文件格式: MP4',
        2: '支持的讲义文件格式: pdf, ppt(.pptx)',
        3: '支持的资源文件格式: PDF, Word文档, ZIP/RAR压缩包',
      };
      return typeMap[this.model.resourceType] || '请选择符合要求的文件格式';
    },

    /**
     * 上传前验证文件类型
     * @param {Object} file - 上传的文件对象
     * @returns {boolean|Promise} 验证结果
     */
    beforeUpload(file) {
      // 获取文件扩展名
      const fileName = file.name;
      const fileExt = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();

      // 检查文件类型是否符合要求
      if (!this.allowedFileTypes.includes(fileExt)) {
        this.$message.error(`不支持的文件类型: ${fileExt}，请上传${this.getFileTypeHintMessage()}`);
        return false;
      }

      // 检查文件大小
      const isLt500M = file.size / 1024 / 1024 < 500;
      if (!isLt500M) {
        this.$message.error('文件大小不能超过500MB!');
        return false;
      }

      return true;
    },

    /**
     * 文件上传变化处理
     * @param {Object} info - 上传信息对象
     */
    handleUploadChange(info) {
      if (info && info.file && info.file.status === 'error') {
        this.$message.error(`${info.file.name} 文件上传失败.`);
      }
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

          // 确保 resourceUrl 字段正确传递
          const submitData = {
            ...this.model,
            resourceUrl: this.model.resourceUrl || '' // 确保不为 undefined
          };



          httpAction(httpurl, submitData, method).then((res) => {

            if (res.success) {
              that.$message.success(res.message);
              that.$emit('ok');
            } else {
              that.$message.warning(res.message);
            }
          }).catch(error => {
            console.error('保存失败:', error);
            that.$message.error('保存失败');
          }).finally(() => {
            that.confirmLoading = false;
          })
        }
      })
    },
  }
}
</script>
