<script setup>
  import { defineProps, watch, ref, nextTick } from 'vue';
  import FullCalendar from '@fullcalendar/vue3';
  import interactionPlugin from '@fullcalendar/interaction';
  import resourceTimeGridPlugin from '@fullcalendar/resource-timegrid';
  import dayGridPlugin from '@fullcalendar/daygrid';
  import timeGridPlugin from '@fullcalendar/timegrid';
  import koLocale from '@fullcalendar/core/locales/ko';
  import tippy from 'tippy.js';
  import 'tippy.js/dist/tippy.css';

  const calendarRef = ref(null);

  const props = defineProps({
    schedules: {
      type: Array,
      default: () => [],
    },
  });

  const emit = defineEmits(['clickSchedule']);

  const staffColors = {
    최민수: 'var(--color-primary-main)',
    김민지: 'var(--color-success-200)',
    이채은: 'var(--color-secondary-100)',
  };

  const statusColors = {
    '예약 확정': 'var(--color-success-100)',
    '예약 대기': 'var(--color-warning-200)',
  };

  const calendarOptions = ref({
    plugins: [interactionPlugin, resourceTimeGridPlugin, dayGridPlugin, timeGridPlugin],
    initialView: 'dayGridMonth',
    editable: true,
    eventDurationEditable: false,
    locale: koLocale,
    resources: [],
    resourceOrder: 'customOrder',

    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'resourceTimeGridDay,timeGridWeek,dayGridMonth',
    },
    views: {
      resourceTimeGridDay: {
        type: 'resourceTimeGrid',
        buttonText: '일',
      },
      timeGridWeek: {
        type: 'timeGrid',
        buttonText: '주',
      },
      dayGridMonth: {
        type: 'dayGridMonth',
        buttonText: '월',
      },
    },

    eventContent({ event, view }) {
      const type = event.extendedProps.type;
      const time = event.start.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      const viewType = view.type;

      return {
        html: `
        <div class="custom-event-style ${type}">
          ${type === 'reservation' || type === 'leave' ? '<div class="left-bar"></div>' : ''}
          <div class="text">
            ${viewType === 'dayGridMonth' ? `<strong>${time}</strong>&nbsp;` : ''}
            ${event.title}
          </div>
        </div>
      `,
      };
    },

    eventClick(info) {
      emit('clickSchedule', info.event.id);
    },

    eventDidMount(info) {
      const { event, el, view } = info;
      const { type, status, staff, customer, service, timeRange, memo } = event.extendedProps;
      const viewType = view.type;
      const borderColor = staffColors[staff?.trim()] || 'var(--color-gray-500)';
      const bgColor =
        type === 'reservation'
          ? statusColors[status] || 'var(--color-gray-200)'
          : type === 'leave'
            ? 'rgba(220, 38, 38, 0.3)'
            : 'var(--color-neutral-white)';

      const box = el.querySelector('.custom-event-style');
      const bar = el.querySelector('.left-bar');

      if (box) {
        box.style.backgroundColor = bgColor;
        if (type === 'event') {
          box.style.border = `2px solid ${borderColor}`;
        }
      }

      if (bar && (type === 'reservation' || type === 'leave')) {
        bar.style.backgroundColor = borderColor;
        if (viewType === 'dayGridMonth') {
          nextTick(() => {
            bar.style.height = `${box.offsetHeight}px`;
          });
        }
      }

      const tooltipLines = [];
      const fallbackTimeRange =
        event.start && event.end
          ? `${event.start.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} - ${event.end.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
          : null;

      if (type === 'reservation') {
        tooltipLines.push(
          customer && `👤 고객: ${customer}`,
          service && `💈 시술: ${service}`,
          (timeRange || fallbackTimeRange) && `🕒 시간: ${timeRange || fallbackTimeRange}`,
          memo && `📝 메모: ${memo}`
        );
      } else if (type === 'event') {
        tooltipLines.push(
          (timeRange || fallbackTimeRange) && `🕒 시간: ${timeRange || fallbackTimeRange}`,
          memo && `📝 메모: ${memo}`
        );
      } else if (type === 'leave') {
        tooltipLines.push(`📅 종일 휴무`, memo && `📝 메모: ${memo}`);
      }

      const tooltip = tooltipLines.filter(Boolean).join('<br/>');

      tippy(el, {
        content: tooltip || '스케줄 정보 없음',
        allowHTML: true,
        placement: 'top',
        animation: 'shift-away',
        delay: [100, 0],
        theme: 'light',
      });
    },
  });

  watch(
    () => props.schedules,
    newVal => {
      const api = calendarRef.value?.getApi();
      const currentView = api?.view?.type || calendarOptions.value.initialView;

      updateResources(currentView);
      updateEvents(currentView);
    },
    { immediate: true, deep: true }
  );
  calendarOptions.value.viewDidMount = arg => {
    const viewType = arg.view.type;
    updateResources(viewType);
    updateEvents(viewType);
  };

  function updateResources(viewType) {
    const staffsFromData = props.schedules.map(s => s.staff?.trim() || '미지정');
    const seen = new Set();
    const uniqueStaffs = [];
    for (const staff of staffsFromData) {
      if (!seen.has(staff)) {
        seen.add(staff);
        uniqueStaffs.push(staff);
      }
    }
    const resultStaffs = uniqueStaffs.filter(s => s !== '미지정');
    resultStaffs.unshift('미지정');

    calendarOptions.value.resources =
      viewType === 'resourceTimeGridDay'
        ? resultStaffs.map((staff, index) => ({
            id: staff,
            title: staff,
            customOrder: index,
          }))
        : [];
  }

  function updateEvents(viewType) {
    calendarOptions.value.events = props.schedules.map(item => {
      const baseEvent = {
        id: item.id,
        title: item.title,
        start: item.start,
        end: item.end,
        extendedProps: {
          type: item.type,
          status: item.status,
          staff: item.staff,
          customer: item.customer,
          service: item.service,
          timeRange: item.timeRange,
          memo: item.memo,
        },
      };
      if (viewType === 'resourceTimeGridDay') {
        baseEvent.resourceId = item.staff?.trim() || '미지정';
      }
      return baseEvent;
    });
  }
</script>

<template>
  <FullCalendar ref="calendarRef" :options="calendarOptions" />
</template>

<style scoped>
  :deep(.fc-event) {
    background-color: transparent !important;
    border: none !important;
    font-weight: 500;
    border-radius: 6px;
    box-shadow: none;
  }

  :deep(.fc-daygrid-event) {
    display: block !important;
    width: 100% !important;
    max-width: 100% !important;
  }

  :deep(.custom-event-style) {
    position: relative;
    display: flex;
    align-items: center;
    font-size: 13px;
    font-weight: 500;
    padding: 6px 8px;
    border-radius: 6px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    height: 100%;
  }

  :deep(.custom-event-style.event) {
    background-color: var(--color-neutral-white);
  }

  :deep(.custom-event-style .left-bar) {
    position: absolute;
    left: 0;
    top: 0;
    width: 6px;
    border-radius: 6px 0 0 6px;
    height: 100%;
  }

  :deep(.custom-event-style .text) {
    padding-left: 10px;
    z-index: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: var(--color-text-primary);
  }

  .tippy-box[data-theme~='light'] {
    background-color: var(--color-neutral-white);
    color: var(--color-text-primary);
    font-size: 13px;
    padding: 8px;
    border-radius: 6px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
</style>
