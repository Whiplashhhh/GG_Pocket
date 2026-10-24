<script setup lang="ts" generic="T extends string">
import { RadioGroupItem, RadioGroupRoot } from 'reka-ui'

/**
 * Big grouped choice buttons (Mouli / Tête, Oui / Non). Radio semantics: one value is always
 * selected, arrow keys move the selection.
 */
withDefaults(
  defineProps<{
    /** Accessible name of the group. */
    label: string
    options: { value: T; label: string }[]
    /** lg (48 px) for the session entry, used with chalky hands. */
    size?: 'md' | 'lg'
    disabled?: boolean
    /** Fills the width of its parent, the options sharing it equally. */
    stretch?: boolean
  }>(),
  { size: 'md', disabled: false, stretch: false },
)

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
    :disabled="disabled"
    orientation="horizontal"
    class="rounded-control border-2 border-ink p-0.5"
    :class="[stretch ? 'flex w-full' : 'inline-flex', { 'opacity-45': disabled }]"
    @update:model-value="select"
  >
    <RadioGroupItem
      v-for="option in options"
      :key="option.value"
      :value="option.value"
      :class="[
        size === 'lg' ? 'min-h-12 text-lg' : 'min-h-11',
        stretch ? 'min-w-0 flex-1 px-2' : 'min-w-20 px-4',
      ]"
      class="rounded-[2px] font-semibold text-ink transition-colors duration-150 disabled:cursor-not-allowed data-[state=checked]:bg-ink data-[state=checked]:text-bg"
    >
      {{ option.label }}
    </RadioGroupItem>
  </RadioGroupRoot>
</template>
