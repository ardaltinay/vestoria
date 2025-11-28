<template>
  <span class="inline-flex items-center gap-1" :class="className">
    <CurrencyIcon :size="iconSize" :variant="variant" />
    {{ formatCurrency(amount, { showDecimals, compact }) }}
  </span>
</template>

<script setup>
import CurrencyIcon from './CurrencyIcon.vue'
import { formatCurrency } from '../utils/currency'

const props = defineProps({
  amount: {
    type: [Number, String],
    required: true
  },
  iconSize: {
    type: [Number, String],
    default: 16
  },
  variant: {
    type: String,
    default: 'default'
  },
  showDecimals: {
    type: Boolean,
    default: undefined
  },
  compact: {
    type: Boolean,
    default: false
  },
  className: {
    type: String,
    default: ''
  }
})
</script>
