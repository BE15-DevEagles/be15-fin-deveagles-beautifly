<template>
  <BaseItemModal title="2차 분류 상품 수정" @close="$emit('close')" @submit="submit">
    <!-- 1차 분류명 -->
    <div class="form-group">
      <label>1차 분류명</label>
      <BaseForm
        v-model="form.primaryItemId"
        type="select"
        :options="primaryOptions.map(opt => ({ value: opt.id, text: opt.name }))"
        placeholder="1차 분류 선택"
      />
    </div>

    <!-- 2차 분류명 -->
    <div class="form-group">
      <label>2차 분류명</label>
      <BaseForm v-model="form.secondaryItemName" type="text" placeholder="2차 분류명" />
    </div>

    <!-- 상품 금액 -->
    <div class="form-group">
      <label>상품 금액</label>
      <BaseForm
        v-model.number="form.secondaryItemPrice"
        type="number"
        step="100"
        placeholder="금액"
      />
    </div>

    <!-- 시술 시간 -->
    <div v-if="selectedPrimary?.category === 'SERVICE'" class="form-group">
      <label>시술 시간 (분)</label>
      <BaseForm v-model.number="form.duration" type="number" step="5" placeholder="예: 60" />
    </div>

    <!-- 상품 활성화 -->
    <div class="form-group">
      <label>상품 활성화</label>
      <div
        class="toggle-button"
        :class="{ active: form.isActive }"
        @click="form.isActive = !form.isActive"
      >
        <div class="circle" />
      </div>
    </div>

    <!-- 하단 버튼 -->
    <template #footer>
      <div class="footer-buttons">
        <div class="left-group">
          <BaseButton type="primary" outline @click="$emit('close')">취소</BaseButton>
          <BaseButton type="primary" @click="submit">수정</BaseButton>
        </div>
        <div class="right-group">
          <BaseButton type="error" @click="showDeleteModal = true">삭제</BaseButton>
        </div>
      </div>
    </template>

    <!-- 삭제 모달 -->
    <SecondaryDeleteModal
      v-if="showDeleteModal"
      v-model="showDeleteModal"
      :secondary-item-id="form.secondaryItemId"
      @confirm="handleDelete"
    />
  </BaseItemModal>
</template>

<script setup>
  import { computed, ref } from 'vue';
  import BaseItemModal from './BaseItemModal.vue';
  import BaseForm from '@/components/common/BaseForm.vue';
  import BaseButton from '@/components/common/BaseButton.vue';
  import SecondaryDeleteModal from './SecondaryDeleteModal.vue';
  import { updateSecondaryItem } from '@/features/items/api/items.js';

  const props = defineProps({
    modelValue: {
      type: Object,
      required: true,
    },
    primaryOptions: {
      type: Array,
      required: true,
    },
  });

  const emit = defineEmits(['close', 'submit', 'update:modelValue', 'delete']);

  const form = computed({
    get: () => props.modelValue,
    set: val => emit('update:modelValue', val),
  });

  const selectedPrimary = computed(() =>
    props.primaryOptions.find(opt => opt.id === form.value.primaryItemId)
  );

  const showDeleteModal = ref(false);

  const submit = async () => {
    try {
      await updateSecondaryItem({
        secondaryItemId: form.value.secondaryItemId,
        primaryItemId: form.value.primaryItemId,
        secondaryItemName: form.value.secondaryItemName,
        secondaryItemPrice: form.value.secondaryItemPrice,
        timeTaken: form.value.duration,
        active: form.value.isActive,
      });

      emit('submit', form.value);
      emit('close');
    } catch (e) {
      console.error(e);
      emit('close');
    }
  };

  const handleDelete = async () => {
    try {
      // 삭제 후 데이터를 새로 고침 할 수 있게 처리
      emit('delete', form.value);
      emit('close');
    } catch (e) {
      console.error(e);
      emit('close');
    }
  };
</script>

<style scoped>
  .form-group {
    margin-bottom: 16px;
    display: flex;
    flex-direction: column;
  }

  label {
    margin-bottom: 4px;
  }

  .footer-buttons {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .left-group,
  .right-group {
    display: flex;
    gap: 8px;
  }

  /* 커스텀 토글 버튼 스타일 */
  .toggle-button {
    width: 48px;
    height: 24px;
    background-color: #ccc;
    border-radius: 12px;
    position: relative;
    cursor: pointer;
    transition: background-color 0.3s;
  }

  .toggle-button .circle {
    width: 20px;
    height: 20px;
    background-color: white;
    border-radius: 50%;
    position: absolute;
    top: 2px;
    left: 2px;
    transition: left 0.3s;
  }

  .toggle-button.active {
    background-color: #364f6b;
  }

  .toggle-button.active .circle {
    left: 26px;
  }
</style>
