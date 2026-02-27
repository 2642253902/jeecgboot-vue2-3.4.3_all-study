<template>
  <j-modal :title="title" :width="width" :visible="visible" switchFullscreen @ok="handleOk"
    :okButtonProps="{ class: { 'jee-hidden': disableSubmit } }" @cancel="handleCancel" cancelText="关闭">
    <cloud-computing-course-resource-form ref="realForm" @ok="submitCallback"
      :disabled="disableSubmit"></cloud-computing-course-resource-form>
  </j-modal>
</template>

<script>

import CloudComputingCourseResourceForm from './CloudComputingCourseResourceForm'
export default {
  name: 'CloudComputingCourseResourceModal',
  components: {
    CloudComputingCourseResourceForm
  },
  data() {
    return {
      title: '',
      width: 800,
      visible: false,
      disableSubmit: false
    }
  },
  methods: {
    add(defaults) {
      this.visible = true;
      this.disableSubmit = false; // 确保可提交
      this.title = '添加资源';

      this.$nextTick(() => {
        this.$refs.realForm.add(defaults);
      });
    },
    edit(record) {
      this.visible = true;
      this.disableSubmit = false; // 确保可编辑
      this.title = '编辑资源';

      this.$nextTick(() => {
        this.$refs.realForm.edit(record);
      });
    },
    detail(record) {
      this.visible = true;
      this.disableSubmit = true; // 设置为只读
      this.title = '资源详情';

      this.$nextTick(() => {
        this.$refs.realForm.edit(record); // 仍然调用edit，但通过disableSubmit控制只读
      });
    },
    close() {
      this.$emit('close');
      this.visible = false;
      this.disableSubmit = false; // 重置状态
    },
    handleOk() {
      this.$refs.realForm.submitForm();
    },
    submitCallback() {
      this.$emit('ok');
      this.visible = false;
      this.disableSubmit = false; // 重置状态
    },
    handleCancel() {
      this.close();
    }
  }
}
</script>