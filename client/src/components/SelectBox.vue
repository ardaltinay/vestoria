<template>
  <div class="select-box-wrapper" ref="selectBoxRef">
    <label v-if="label" class="select-box-label">{{ label }}</label>
    <div 
      class="select-box"
      :class="{ 'select-box--disabled': disabled, 'select-box--open': isOpen }"
      @click="toggleDropdown"
      tabindex="0"
      @keydown.enter.prevent="toggleDropdown"
      @keydown.space.prevent="toggleDropdown"
      @keydown.escape="closeDropdown"
      @keydown.down.prevent="navigateDown"
      @keydown.up.prevent="navigateUp"
    >
      <div class="select-box__display">
        <span v-if="selectedOption" class="select-box__selected">
          {{ selectedOption[labelKey] }}
        </span>
        <span v-else class="select-box__placeholder">
          {{ placeholder }}
        </span>
        <svg 
          class="select-box__arrow" 
          :class="{ 'select-box__arrow--open': isOpen }"
          fill="none" 
          stroke="currentColor" 
          viewBox="0 0 24 24"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
        </svg>
      </div>
    </div>
    
    <Teleport to="body">
      <Transition name="dropdown">
        <div 
          v-if="isOpen" 
          class="select-box__dropdown" 
          :style="dropdownStyle"
          @click.stop
        >
          <div 
            v-for="(option, index) in options" 
            :key="option[valueKey]"
            class="select-box__option"
            :class="{ 
              'select-box__option--selected': isSelected(option),
              'select-box__option--focused': focusedIndex === index
            }"
            @click="selectOption(option)"
            @mouseenter="focusedIndex = index"
          >
            <span class="select-box__option-label">{{ option[labelKey] }}</span>
            <svg 
              v-if="isSelected(option)"
              class="select-box__check" 
              fill="none" 
              stroke="currentColor" 
              viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
            </svg>
          </div>
          <div v-if="options.length === 0" class="select-box__empty">
            Seçenek bulunamadı
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'

const props = defineProps({
  modelValue: {
    type: [String, Number, Object],
    default: null
  },
  options: {
    type: Array,
    required: true
  },
  placeholder: {
    type: String,
    default: 'Seçiniz...'
  },
  label: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  },
  valueKey: {
    type: String,
    default: 'id'
  },
  labelKey: {
    type: String,
    default: 'name'
  }
})

const emit = defineEmits(['update:modelValue'])

const selectBoxRef = ref(null)
const isOpen = ref(false)
const focusedIndex = ref(-1)
const dropdownStyle = ref({})

const selectedOption = computed(() => {
  if (!props.modelValue) return null
  return props.options.find(opt => String(opt[props.valueKey]) === String(props.modelValue))
})

const isSelected = (option) => {
  const optionValue = option[props.valueKey]
  const result = String(props.modelValue) === String(optionValue)
  return result
}

const toggleDropdown = async () => {
  if (props.disabled) return
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    focusedIndex.value = props.options.findIndex(opt => isSelected(opt))
    await nextTick()
    updateDropdownPosition()
  }
}

const closeDropdown = () => {
  isOpen.value = false
  focusedIndex.value = -1
}

const selectOption = (option) => {
  const value = option[props.valueKey]
  emit('update:modelValue', value)
  closeDropdown()
}

const updateDropdownPosition = () => {
  if (!selectBoxRef.value) return
  const rect = selectBoxRef.value.getBoundingClientRect()
  dropdownStyle.value = {
    position: 'fixed',
    top: `${rect.bottom + 8}px`,
    left: `${rect.left}px`,
    width: `${rect.width}px`,
    zIndex: 9999
  }
}

const navigateDown = () => {
  if (!isOpen.value) {
    toggleDropdown()
    return
  }
  focusedIndex.value = Math.min(focusedIndex.value + 1, props.options.length - 1)
}

const navigateUp = () => {
  if (!isOpen.value) return
  focusedIndex.value = Math.max(focusedIndex.value - 1, 0)
}

const handleClickOutside = (event) => {
  if (selectBoxRef.value && !selectBoxRef.value.contains(event.target)) {
    closeDropdown()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  window.addEventListener('scroll', updateDropdownPosition, true)
  window.addEventListener('resize', updateDropdownPosition)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  window.removeEventListener('scroll', updateDropdownPosition, true)
  window.removeEventListener('resize', updateDropdownPosition)
})

watch(() => props.modelValue, () => {
  if (isOpen.value) {
    focusedIndex.value = props.options.findIndex(opt => isSelected(opt))
  }
})
</script>

<style scoped>
.select-box-wrapper {
  width: 100%;
}

.select-box-label {
  display: block;
  font-size: 0.875rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5rem;
}

.select-box {
  position: relative;
  width: 100%;
  cursor: pointer;
  user-select: none;
}

.select-box--disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}

.select-box__display {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1rem;
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 0.75rem;
  transition: all 0.2s ease;
  min-height: 3rem;
}

.select-box:hover .select-box__display {
  border-color: #d1d5db;
}

.select-box--open .select-box__display,
.select-box:focus .select-box__display {
  border-color: #f59e0b;
  box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.1);
  outline: none;
}

.select-box__selected {
  font-weight: 600;
  color: #111827;
  flex: 1;
}

.select-box__placeholder {
  color: #9ca3af;
  flex: 1;
}

.select-box__arrow {
  width: 1.25rem;
  height: 1.25rem;
  color: #6b7280;
  transition: transform 0.2s ease;
  flex-shrink: 0;
  margin-left: 0.5rem;
}

.select-box__arrow--open {
  transform: rotate(180deg);
}

.select-box__dropdown {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 0.75rem;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  max-height: 300px;
  overflow-y: auto;
  padding: 0.5rem;
}

.select-box__option {
  padding: 0.75rem 1rem;
  cursor: pointer;
  border-radius: 0.5rem;
  transition: all 0.15s ease;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.select-box__option:hover,
.select-box__option--focused {
  background: #fef3c7;
}

.select-box__option--selected {
  background: #fde68a;
  font-weight: 600;
}

.select-box__option-label {
  font-weight: 600;
  color: #111827;
  font-size: 0.875rem;
}

.select-box__option-description {
  font-size: 0.75rem;
  color: #6b7280;
  line-height: 1.3;
}

.select-box__check {
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
  width: 1rem;
  height: 1rem;
  color: #f59e0b;
  flex-shrink: 0;
}

.select-box__empty {
  padding: 1rem;
  text-align: center;
  color: #9ca3af;
  font-size: 0.875rem;
}

/* Dropdown animation */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from {
  opacity: 0;
  transform: translateY(-0.5rem);
}

.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-0.5rem);
}

/* Scrollbar styling */
.select-box__dropdown::-webkit-scrollbar {
  width: 0.5rem;
}

.select-box__dropdown::-webkit-scrollbar-track {
  background: #f3f4f6;
  border-radius: 0.25rem;
}

.select-box__dropdown::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 0.25rem;
}

.select-box__dropdown::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}
</style>
