<script setup lang="ts">
import { computed, useId } from 'vue'
import { useI18n } from 'vue-i18n'

/** A labelled native select with its hint and error, like TextField. Options go in the slot. */
const props = withDefaults(
  defineProps<{
    label: string
    hint?: string
    /** Already translated. */
    error?: string
    optional?: boolean
  }>(),
  { hint: undefined, error: undefined, optional: false },
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
</script>

<template>
  <div class="flex flex-col gap-1.5">
    <label :for="id" class="font-semibold">
      {{ label }}
      <span v-if="optional" class="font-normal text-ink-muted">({{ t('common.optional') }})</span>
    </label>
    <select
      :id="id"
      v-model="model"
      :required="!optional"
      :aria-invalid="error ? true : undefined"
      :aria-describedby="describedBy"
      class="min-h-12 w-full rounded-control border-2 border-rule bg-surface px-3 text-base text-ink focus:border-ink focus:outline-none aria-invalid:border-danger"
    >
      <slot />
    </select>
    <p v-if="hint && !error" :id="`${id}-hint`" class="text-sm text-ink-muted">{{ hint }}</p>
    <p v-if="error" :id="`${id}-error`" class="text-sm font-semibold text-danger">{{ error }}</p>
  </div>
</template>
