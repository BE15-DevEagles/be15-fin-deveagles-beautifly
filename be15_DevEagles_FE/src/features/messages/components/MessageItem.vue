<script setup>
  import { computed, toRefs } from 'vue';
  import BaseButton from '@/components/common/BaseButton.vue';
  import TrashIcon from '@/components/icons/TrashIcon.vue';
  import EditIcon from '@/components/icons/EditIcon.vue';

  const props = defineProps({
    message: {
      type: Object,
      required: true,
    },
  });

  const emit = defineEmits(['delete', 'show-detail', 'edit', 'resend']);

  // ✅ 안정적인 구조분해 (reactivity 보존)
  const { statusLabel, messageSendingType } = toRefs(props.message);

  const statusText = computed(() => {
    if (statusLabel.value === 'SENT') {
      return messageSendingType.value === 'AUTOMATIC' ? '자동 발송' : '발송 완료';
    }

    if (messageSendingType.value === 'RESERVATION') return '예약 문자';
    if (statusLabel.value === 'FAIL') {
      return '전송 실패';
    }
    return statusLabel.value || '기타';
  });

  function formatDate(dateStr) {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  }
</script>

<template>
  <tr>
    <td>
      <div class="cell-content">{{ props.message.title }}</div>
    </td>
    <td>
      <div class="cell-content text-ellipsis" @click="$emit('show-detail', props.message)">
        {{ props.message.content }}
      </div>
    </td>
    <td>
      <div class="cell-content">{{ props.message.receiverName }}</div>
    </td>
    <td>
      <div class="cell-content">{{ statusText }}</div>
    </td>
    <td>
      <div class="cell-content">{{ formatDate(props.message.date) }}</div>
    </td>
    <td>
      <div class="cell-content">
        <div
          v-if="
            props.message.messageSendingType === 'RESERVATION' &&
            props.message.statusLabel !== 'SENT'
          "
          class="action-buttons"
        >
          <BaseButton
            type="ghost"
            size="sm"
            class="icon-button"
            @click="$emit('edit', props.message)"
          >
            <EditIcon :size="16" />
          </BaseButton>
          <BaseButton
            type="ghost"
            size="sm"
            class="icon-button"
            @click="emit('delete', props.message, $event)"
          >
            <TrashIcon :size="16" color="var(--color-error-600)" />
          </BaseButton>
        </div>
        <div v-else-if="props.message.statusLabel === 'FAIL'" class="action-buttons">
          <BaseButton
            type="ghost"
            size="sm"
            class="icon-button"
            @click="
              () => {
                console.log('🔁 emit');
                emit('resend', props.message);
              }
            "
          >
            <span style="font-size: 14px">🔁</span>
          </BaseButton>
        </div>
      </div>
    </td>
  </tr>
</template>

<style scoped>
  .cell-content {
    text-align: center;
    font-size: 14px;
    padding: 6px 4px;
    line-height: 1.4;
    white-space: nowrap;
    color: var(--color-gray-800);
  }
  .text-ellipsis {
    max-width: 240px;
    margin: 0 auto;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    text-align: center;
    cursor: pointer;
  }
  .action-buttons {
    display: flex;
    justify-content: center;
    gap: 8px;
  }
</style>
