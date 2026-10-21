<script setup lang="ts">
import { Check } from 'lucide-vue-next'
import { computed, useId } from 'vue'
import { useI18n } from 'vue-i18n'

import type { HoldColorConfig } from '@/api/topo'
import HoldSwatch from '@/components/ui/HoldSwatch.vue'

/**
 * One hold colour among the club's, as big radio chips: the swatch and its name side by side
 * (the colour is never the only clue, docs/DESIGN.md §2). '' means none.
 */
const props = withDefaults(
  defineProps<{
    legend: string
    colors: HoldColorConfig[]
    /** Offers « none » first, for an optional choice. */
    noneLabel?: string
    hint?: string
    /** Already translated. */
    error?: string
  }>(),
  { noneLabel: undefined, hint: undefined, error: undefined },
)

const model = defineModel<string>({ required: true })

const { t } = useI18n()
const id = useId()
const describedBy = computed(
  () =>
    [props.hint && !props.error ? `${id}-hint` : null, props.error ? `${id}-error` : null]
      .filter(Boolean)
      .join(' ') || undefined,
)

const CHIP =
  'inline-flex min-h-11 cursor-pointer items-center gap-2 rounded-control border-2 border-rule px-3 font-semibold hover:border-ink has-checked:border-ink has-checked:bg-surface-sunken has-focus-visible:outline-3 has-focus-visible:outline-offset-2 has-focus-visible:outline-focus'
</script>

<template>
  <fieldset
    tabindex="-1"
    :aria-describedby="describedBy"
    :data-invalid="error ? 'true' : undefined"
    class="flex flex-col gap-1.5 outline-none"
  >
    <legend class="font-semibold">
      {{ legend }}
      <span v-if="noneLabel" class="font-normal text-ink-muted">({{ t('common.optional') }})</span>
    </legend>
    <p v-if="hint && !error" :id="`${id}-hint`" class="text-sm text-ink-muted">{{ hint }}</p>
    <div class="mt-1 flex flex-wrap gap-2">
      <label v-if="noneLabel" :class="CHIP">
        <input v-model="model" type="radio" :name="id" value="" class="sr-only" />
        <Check v-if="model === ''" :size="18" :stroke-width="3" aria-hidden="true" />
        {{ noneLabel }}
      </label>
      <label v-for="color in colors" :key="color.id" :class="CHIP">
        <input v-model="model" type="radio" :name="id" :value="color.id" class="sr-only" />
        <Check v-if="model === color.id" :size="18" :stroke-width="3" aria-hidden="true" />
        <HoldSwatch :color="color" :size="24" />
        {{ color.active ? color.label : t('routeForm.colorInactive', { label: color.label }) }}
      </label>
    </div>
    <p v-if="error" :id="`${id}-error`" class="text-sm font-semibold text-danger">{{ error }}</p>
  </fieldset>
</template>
