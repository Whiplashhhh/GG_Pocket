<script setup lang="ts">
import { computed, useId, useTemplateRef } from 'vue'
import { useI18n } from 'vue-i18n'

/** A labelled input with its hint and error, wired for screen readers. */
const props = withDefaults(
  defineProps<{
    label: string
    type?: 'text' | 'email' | 'date' | 'number' | 'password' | 'search'
    autocomplete?: string
    inputmode?: 'text' | 'numeric' | 'email' | 'search'
    hint?: string
    /** Already translated. */
    error?: string
    optional?: boolean
    maxlength?: number
    min?: string | number
    max?: string | number
    placeholder?: string
    /** Nicknames and codes: no automatic capital, no spell check. */
    verbatim?: boolean
  }>(),
  {
    type: 'text',
    autocomplete: 'off',
    inputmode: undefined,
    hint: undefined,
    error: undefined,
    optional: false,
    maxlength: undefined,
    min: undefined,
    max: undefined,
    placeholder: undefined,
    verbatim: false,
  },
)

const model = defineModel<string>({ required: true })

const { t } = useI18n()
const id = useId()
const input = useTemplateRef<HTMLInputElement>('input')
const describedBy = computed(
  () =>
    [props.hint ? `${id}-hint` : null, props.error ? `${id}-error` : null]
      .filter(Boolean)
      .join(' ') || undefined,
)

defineExpose({ focus: () => input.value?.focus() })
</script>

<template>
  <div class="flex flex-col gap-1.5">
    <label :for="id" class="font-semibold">
      {{ label }}
      <span v-if="optional" class="font-normal text-ink-muted">({{ t('common.optional') }})</span>
    </label>
    <div class="relative">
      <input
        :id="id"
        ref="input"
        v-model="model"
        :type="type"
        :autocomplete="autocomplete"
        :inputmode="inputmode"
        :maxlength="maxlength"
        :min="min"
        :max="max"
        :placeholder="placeholder"
        :required="!optional"
        :aria-invalid="error ? true : undefined"
        :aria-describedby="describedBy"
        :autocapitalize="verbatim ? 'none' : undefined"
        :spellcheck="verbatim ? false : undefined"
        class="min-h-12 w-full rounded-control border-2 border-rule bg-surface px-3 text-base text-ink transition-colors duration-150 placeholder:text-ink-muted focus:border-ink focus:outline-none aria-invalid:border-danger"
        :class="{ 'pr-12': $slots.trailing }"
      />
      <div v-if="$slots.trailing" class="absolute inset-y-0 right-0 flex items-center pr-0.5">
        <slot name="trailing" />
      </div>
    </div>
    <p v-if="hint" :id="`${id}-hint`" class="text-sm text-ink-muted">{{ hint }}</p>
    <p v-if="error" :id="`${id}-error`" class="text-sm font-semibold text-danger">{{ error }}</p>
  </div>
</template>
