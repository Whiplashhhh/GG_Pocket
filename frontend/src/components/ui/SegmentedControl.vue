<script setup lang="ts" generic="T extends string">
import { RadioGroupItem, RadioGroupRoot } from 'reka-ui'

/**
 * Big grouped choice buttons (Mouli / Tête, Oui / Non). Radio semantics: one value is always
 * selected, arrow keys move the selection.
 */
defineProps<{
  /** Accessible name of the group. */
  label: string
  options: { value: T; label: string }[]
}>()

const model = defineModel<T>({ required: true })

function select(value: unknown) {
  // Reka types the value as a generic AcceptableValue; only our own options can reach here.
  model.value = value as T
}
</script>

<template>
  <RadioGroupRoot
    :model-value="model"
    :aria-label="label"
    orientation="horizontal"
    class="inline-flex rounded-control border-2 border-ink p-0.5"
    @update:model-value="select"
  >
    <RadioGroupItem
      v-for="option in options"
      :key="option.value"
      :value="option.value"
      class="min-h-11 min-w-20 rounded-[2px] px-4 font-semibold text-ink transition-colors duration-150 data-[state=checked]:bg-ink data-[state=checked]:text-bg"
    >
      {{ option.label }}
    </RadioGroupItem>
  </RadioGroupRoot>
</template>
