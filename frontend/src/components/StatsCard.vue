<template>
  <div class="stats-card" :class="`stats-card--${variant}`">
    <div class="stats-card__body">
      <div class="stats-card__info">
        <span class="stats-card__label">{{ label }}</span>
        <span class="stats-card__value">{{ formattedValue }}</span>
        <span v-if="trend !== undefined" class="stats-card__trend" :class="trendClass">
          <i :class="trendIcon"></i>
          {{ trendText }}
        </span>
      </div>
      <div class="stats-card__icon">
        <i :class="icon"></i>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  label: { type: String, required: true },
  value: { type: [Number, String], default: 0 },
  icon: { type: String, default: 'pi pi-chart-bar' },
  variant: { type: String, default: 'primary', validator: v => ['primary', 'success', 'warning', 'danger', 'info'].includes(v) },
  trend: { type: Number, default: undefined },
  format: { type: String, default: 'number' }
})

const formattedValue = computed(() => {
  if (props.format === 'currency') {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(props.value)
  }
  if (props.format === 'percent') {
    return `${Number(props.value).toFixed(1)}%`
  }
  if (typeof props.value === 'number') {
    return new Intl.NumberFormat('vi-VN').format(props.value)
  }
  return props.value
})

const trendClass = computed(() => props.trend >= 0 ? 'trend-up' : 'trend-down')
const trendIcon = computed(() => props.trend >= 0 ? 'pi pi-arrow-up' : 'pi pi-arrow-down')
const trendText = computed(() => props.trend !== undefined ? `${Math.abs(props.trend)}%` : '')
</script>

<style scoped>
.stats-card {
  background: var(--surface-card);
  border-radius: 12px;
  border: 1px solid var(--surface-border);
  padding: 1.25rem;
  transition: all 0.2s ease;
}

.stats-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.stats-card__body {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.stats-card__info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.stats-card__label {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.stats-card__value {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
}

.stats-card__trend {
  font-size: 0.8125rem;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.trend-up {
  color: var(--success-color);
}

.trend-down {
  color: var(--danger-color);
}

.stats-card__icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  flex-shrink: 0;
}

.stats-card--primary .stats-card__icon {
  background: var(--primary-light);
  color: var(--primary-color);
}

.stats-card--success .stats-card__icon {
  background: #d1fae5;
  color: var(--success-color);
}

.stats-card--warning .stats-card__icon {
  background: #fef3c7;
  color: var(--warning-color);
}

.stats-card--danger .stats-card__icon {
  background: #fee2e2;
  color: var(--danger-color);
}

.stats-card--info .stats-card__icon {
  background: #cffafe;
  color: var(--info-color);
}
</style>
