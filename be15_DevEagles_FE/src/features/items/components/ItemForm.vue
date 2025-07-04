<template>
  <div class="item-manage-wrapper">
    <!-- 헤더 -->
    <div class="header-row">
      <h1 class="page-title">상품 관리</h1>
      <div class="register-wrapper relative">
        <BaseButton class="register-button" @click="toggleRegisterDropdown"> 상품 등록 </BaseButton>
        <div v-if="showRegisterDropdown" ref="registerDropdownRef" class="dropdown-menu below-left">
          <div class="dropdown-item" @click="openPrimaryRegisterModal">1차 상품 등록</div>
          <div class="dropdown-item" @click="openSecondaryRegisterModal">2차 상품 등록</div>
        </div>
      </div>
    </div>

    <!-- 탭 -->
    <div class="tab-filter-row">
      <div class="tab-group">
        <div class="tab" :class="{ active: activeTab === '시술' }" @click="activeTab = '시술'">
          시술
        </div>
        <div class="tab" :class="{ active: activeTab === '상품' }" @click="activeTab = '상품'">
          상품
        </div>
      </div>
    </div>

    <!-- 리스트 -->
    <div v-for="(item, index) in filteredItems" :key="item.primaryItemId" class="card-section">
      <BaseCard>
        <div class="card-title-row">
          <span>{{ item.primaryItemName }}</span>
          <div class="text-right relative">
            <button class="icon-button" @click.stop="toggleDropdown(index)">···</button>
            <div
              v-if="showDropdownIndex === index"
              :ref="el => setDropdownRef(index, el)"
              class="dropdown-menu below-left"
            >
              <div class="dropdown-item" @click="openSecondaryRegisterModal(item)">
                2차 상품 등록
              </div>
              <div class="dropdown-item" @click="openPrimaryEditModal(item)">1차 상품 수정</div>
              <div class="dropdown-item" @click="deletePrimaryItem(item)">1차 상품 삭제</div>
            </div>
          </div>
        </div>

        <div class="card-table">
          <div class="card-row header">
            <div>2차 상품명</div>
            <div class="text-right">가격</div>
            <div class="text-right">시술 시간</div>
            <div class="text-right">활성화 상태</div>
          </div>
          <div v-for="sub in item.subItems" :key="sub.secondaryItemId" class="card-row">
            <div class="clickable" @click="openSecondaryEditModal(item, sub)">
              {{ sub.secondaryItemName }}
            </div>
            <div class="text-right clickable" @click="openSecondaryEditModal(item, sub)">
              {{ formatPrice(sub.secondaryItemPrice) }}
            </div>
            <div class="text-right clickable" @click="openSecondaryEditModal(item, sub)">
              {{ sub.duration ? sub.duration + '분' : '-' }}
            </div>
            <div class="text-right clickable" @click="openSecondaryEditModal(item, sub)">
              <div class="readonly-toggle" :class="{ active: sub.isActive }">
                <div class="circle" />
              </div>
            </div>
          </div>
        </div>
      </BaseCard>
    </div>

    <!-- 모달들 -->
    <PrimaryRegistModal
      v-if="showPrimaryRegisterModal"
      v-model="primaryForm"
      @submit="handlePrimarySubmit"
      @close="closeModals"
    />
    <SecondaryRegistModal
      v-if="showSecondaryRegisterModal"
      v-model="secondaryForm"
      :primary-options="primaryOptions"
      @submit="handleSecondarySubmit"
      @close="closeModals"
    />
    <PrimaryEditModal
      v-if="showPrimaryEditModal"
      v-model="primaryEditForm"
      @submit="handlePrimaryEdit"
      @close="closeModals"
    />
    <SecondaryEditModal
      v-if="showSecondaryEditModal"
      v-model="secondaryEditForm"
      :primary-options="primaryOptions"
      @submit="handleSecondaryEdit"
      @close="closeModals"
      @success="msg => toastRef.value?.success(msg)"
    />
    <PrimaryDeleteModal
      v-if="showPrimaryDeleteModal"
      v-model="showPrimaryDeleteModal"
      :primary-item-id="selectedProduct?.primaryItemId"
      @confirm="handlePrimaryDelete"
      @error="msg => toastRef.value?.error(msg)"
    />

    <BaseToast ref="toastRef" />
  </div>
</template>

<script setup>
  import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue';
  import BaseButton from '@/components/common/BaseButton.vue';
  import BaseCard from '@/components/common/BaseCard.vue';
  import BaseToast from '@/components/common/BaseToast.vue';
  import PrimaryRegistModal from '@/features/items/components/PrimaryRegistModal.vue';
  import SecondaryRegistModal from '@/features/items/components/SecondaryRegistModal.vue';
  import PrimaryEditModal from '@/features/items/components/PrimaryEditModal.vue';
  import SecondaryEditModal from '@/features/items/components/SecondaryEditModal.vue';
  import PrimaryDeleteModal from '@/features/items/components/PrimaryDeleteModal.vue';
  import { getPrimaryItems, getAllSecondaryItems } from '@/features/items/api/items.js';

  const toastRef = ref(null);
  const showDropdownIndex = ref(null);
  const dropdownRefsMap = new Map();
  const registerDropdownRef = ref(null);

  const setDropdownRef = (index, el) => {
    if (el) dropdownRefsMap.set(index, el);
    else dropdownRefsMap.delete(index);
  };

  const toggleDropdown = async index => {
    showDropdownIndex.value = showDropdownIndex.value === index ? null : index;
    await nextTick();
  };

  const handleClickOutside = event => {
    const dropdownEl = dropdownRefsMap.get(showDropdownIndex.value);
    const registerEl = registerDropdownRef.value;
    if (showDropdownIndex.value !== null && dropdownEl && !dropdownEl.contains(event.target)) {
      showDropdownIndex.value = null;
    }
    if (
      showRegisterDropdown.value &&
      registerEl &&
      !registerEl.contains(event.target) &&
      !event.target.closest('.register-button')
    ) {
      showRegisterDropdown.value = false;
    }
  };

  const showRegisterDropdown = ref(false);
  const toggleRegisterDropdown = () => {
    showRegisterDropdown.value = !showRegisterDropdown.value;
  };

  const activeTab = ref('시술');
  const showPrimaryRegisterModal = ref(false);
  const showSecondaryRegisterModal = ref(false);
  const showPrimaryEditModal = ref(false);
  const showSecondaryEditModal = ref(false);
  const showPrimaryDeleteModal = ref(false);
  const selectedProduct = ref(null);

  const primaryForm = ref({ category: 'SERVICE', primaryItemName: '' });
  const secondaryForm = ref({ primaryItemId: '', secondaryItemName: '', secondaryItemPrice: '' });
  const primaryEditForm = ref({ primaryItemId: '', category: '', primaryItemName: '' });
  const secondaryEditForm = ref({
    primaryItemId: '',
    secondaryItemName: '',
    secondaryItemPrice: '',
    isActive: '',
  });

  const items = ref([]);

  const fetchPrimaryAndSecondaryItems = async () => {
    try {
      const [primaryRes, secondaryRes] = await Promise.all([
        getPrimaryItems(),
        getAllSecondaryItems(),
      ]);

      const secondaryGrouped = secondaryRes.reduce((acc, item) => {
        if (!acc[item.primaryItemId]) acc[item.primaryItemId] = [];
        acc[item.primaryItemId].push({
          secondaryItemId: item.secondaryItemId,
          secondaryItemName: item.secondaryItemName,
          secondaryItemPrice: item.secondaryItemPrice,
          duration: item.timeTaken,
          isActive: Boolean(item.active),
        });
        return acc;
      }, {});

      items.value = primaryRes.map(item => ({
        primaryItemId: item.primaryItemId,
        primaryItemName: item.primaryItemName,
        category: item.category,
        subItems: secondaryGrouped[item.primaryItemId] || [],
      }));
    } catch (error) {
      toastRef.value?.error('상품 목록을 불러오지 못했습니다.');
    }
  };

  const formatPrice = val => `${val.toLocaleString('ko-KR')} 원`;

  const filteredItems = computed(() =>
    items.value.filter(
      item => item.category === (activeTab.value === '시술' ? 'SERVICE' : 'PRODUCT')
    )
  );

  const primaryOptions = computed(() =>
    items.value.map(item => ({
      id: item.primaryItemId,
      name: item.primaryItemName,
      category: item.category,
    }))
  );

  const openPrimaryRegisterModal = () => {
    showRegisterDropdown.value = false;
    showDropdownIndex.value = null;
    showPrimaryRegisterModal.value = true;
  };

  const openSecondaryRegisterModal = item => {
    showRegisterDropdown.value = false;
    showDropdownIndex.value = null;
    secondaryForm.value = {
      primaryItemId: item.primaryItemId,
      primaryItemName: item.primaryItemName, // 1차 상품명 전달
    };
    showSecondaryRegisterModal.value = true;
  };

  const openPrimaryEditModal = item => {
    showRegisterDropdown.value = false;
    showDropdownIndex.value = null;
    primaryEditForm.value = {
      primaryItemId: item.primaryItemId,
      category: item.category,
      primaryItemName: item.primaryItemName,
    };
    selectedProduct.value = item;
    showPrimaryEditModal.value = true;
  };

  const openSecondaryEditModal = (item, sub) => {
    showRegisterDropdown.value = false;
    showDropdownIndex.value = null;

    secondaryEditForm.value = {
      secondaryItemId: sub.secondaryItemId,
      primaryItemId: item.primaryItemId,
      secondaryItemName: sub.secondaryItemName,
      secondaryItemPrice: sub.secondaryItemPrice,
      duration: sub.duration ?? null,
      isActive: sub.isActive ?? true,
    };

    selectedProduct.value = sub;
    showSecondaryEditModal.value = true;
  };

  const deletePrimaryItem = item => {
    showRegisterDropdown.value = false;
    showDropdownIndex.value = null;
    selectedProduct.value = item;
    showPrimaryDeleteModal.value = true;
  };

  const closeModals = () => {
    showPrimaryRegisterModal.value = false;
    showSecondaryRegisterModal.value = false;
    showPrimaryEditModal.value = false;
    showSecondaryEditModal.value = false;
    selectedProduct.value = null;
  };

  const handlePrimarySubmit = () => {
    toastRef.value?.success('1차 상품이 등록되었습니다.');
    fetchPrimaryAndSecondaryItems();
    closeModals();
  };

  const handleSecondarySubmit = () => {
    toastRef.value?.success('2차 상품이 등록되었습니다.');
    fetchPrimaryAndSecondaryItems();
    closeModals();
  };

  const handlePrimaryEdit = () => {
    toastRef.value?.warning('1차 상품이 수정되었습니다.');
    fetchPrimaryAndSecondaryItems();
    closeModals();
  };

  const handleSecondaryEdit = () => {
    toastRef.value?.warning('2차 상품이 수정되었습니다.');
    fetchPrimaryAndSecondaryItems();
    closeModals();
  };

  const handlePrimaryDelete = () => {
    toastRef.value?.success('1차 상품이 삭제되었습니다.');
    fetchPrimaryAndSecondaryItems();
    selectedProduct.value = null;
  };

  onMounted(() => {
    fetchPrimaryAndSecondaryItems();
    window.addEventListener('mousedown', handleClickOutside);
  });

  onBeforeUnmount(() => {
    showRegisterDropdown.value = false;
    showDropdownIndex.value = null;
    window.removeEventListener('mousedown', handleClickOutside);
  });
</script>

<style scoped>
  .item-manage-wrapper {
    padding: 2rem;
  }
  .header-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.5rem;
  }
  .register-wrapper {
    position: relative;
  }
  .tab-filter-row {
    margin-bottom: 1rem;
  }
  .tab-group {
    display: flex;
    width: 220px;
    border: 1px solid #364f6b;
    border-radius: 6px;
    overflow: hidden;
  }
  .tab {
    flex: 1;
    text-align: center;
    padding: 0.5rem 0;
    font-size: 14px;
    cursor: pointer;
    background-color: white;
    color: #364f6b;
    font-weight: bold;
    border-right: 1px solid #364f6b;
  }
  .tab:last-child {
    border-right: none;
  }
  .tab.active {
    background-color: #364f6b;
    color: white;
  }
  .card-section {
    margin-bottom: 24px;
  }
  .card-table {
    width: 100%;
    display: flex;
    flex-direction: column;
  }
  .card-row {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr 1fr;
    padding: 12px 8px;
    font-size: 14px;
    border-bottom: 1px solid #eee;
  }
  .card-row.header {
    font-weight: bold;
    background-color: #f9f9f9;
  }
  .clickable {
    cursor: pointer;
  }
  .text-right {
    text-align: right;
  }
  .relative {
    position: relative;
    display: inline-block;
  }
  .icon-button {
    background: none;
    border: none;
    font-size: 25px;
    cursor: pointer;
  }
  .dropdown-menu {
    position: absolute;
    min-width: 120px;
    background-color: #fff;
    border: 1px solid #364f6b;
    border-radius: 12px;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    z-index: 1000;
    padding: 0;
  }
  .dropdown-menu.below-left {
    top: calc(100% + 4px);
    right: 0;
  }
  .dropdown-item {
    padding: 10px 16px;
    font-size: 14px;
    color: #364f6b;
    background-color: white;
    cursor: pointer;
    transition: background-color 0.2s;
  }
  .dropdown-item:hover {
    background-color: #f5f5f5;
  }
  .card-title-row {
    font-size: 20px;
    font-weight: bold;
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  /* Readonly 토글 */
  .readonly-toggle {
    width: 40px;
    height: 20px;
    background-color: #ccc;
    border-radius: 10px;
    position: relative;
    transition: background-color 0.3s;
    pointer-events: none;
    margin-left: auto;
  }
  .readonly-toggle .circle {
    width: 16px;
    height: 16px;
    background-color: white;
    border-radius: 50%;
    position: absolute;
    top: 2px;
    left: 2px;
    transition: left 0.3s;
  }
  .readonly-toggle.active {
    background-color: #364f6b;
  }
  .readonly-toggle.active .circle {
    left: 22px;
  }
</style>
