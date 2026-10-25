<script setup lang="ts">
import { computed, useId } from 'vue'
import { useI18n } from 'vue-i18n'

/**
 * "Ressenti" out of 10, optional: a slider whose far left means "not rated". One control instead
 * of ten dots, which would not fit at 360 px with 44 px targets.
 */
defineProps<{ label: string }>()
const model = defineModel<number | null>({ required: true })

const { t } = useI18n()
const id = useId()

const value = computed({
  get: () => model.value ?? 0,
  set: (raw: number) => {
    model.value = raw === 0 ? null : raw
  },
})
const shown = computed(() =>
  model.value === null ? t('session.feelingNone') : t('session.feelingValue', { n: model.value }),
)
</script>

<template>
  <div class="flex flex-col gap-1">
    <div class="flex items-baseline justify-between gap-2">
      <label :for="id" class="font-semibold">{{ label }}</label>
      <span class="text-ink-muted tabular-nums" aria-hidden="true">{{ shown }}</span>
    </div>
    <input
      :id="id"
      v-model.number="value"
      type="range"
      min="0"
      max="10"
      step="1"
      :aria-valuetext="shown"
      class="h-11 w-full cursor-pointer accent-ink"
    />
  </div>
</template>
