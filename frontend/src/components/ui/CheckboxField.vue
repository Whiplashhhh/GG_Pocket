<script setup lang="ts">
import { Check } from 'lucide-vue-next'
import { computed, useId } from 'vue'

/** A native checkbox with a big target: the whole line toggles it. */
const props = withDefaults(
  defineProps<{
    hint?: string
    /** Already translated. */
    error?: string
  }>(),
  { hint: undefined, error: undefined },
)

const model = defineModel<boolean>({ required: true })
const id = useId()
const describedBy = computed(
  () =>
    [props.hint ? `${id}-hint` : null, props.error ? `${id}-error` : null]
      .filter(Boolean)
      .join(' ') || undefined,
)
</script>

<template>
  <div class="flex flex-col gap-1">
    <label :for="id" class="flex min-h-11 cursor-pointer items-start gap-3 py-2">
      <span class="relative mt-0.5 inline-flex size-6 shrink-0 items-center justify-center">
        <input
          :id="id"
          v-model="model"
          type="checkbox"
          :aria-invalid="error ? true : undefined"
          :aria-describedby="describedBy"
          class="peer absolute inset-0 size-6 cursor-pointer appearance-none rounded-control border-2 border-ink bg-surface checked:bg-ink aria-invalid:border-danger"
        />
        <Check
          :size="18"
          :stroke-width="3"
          class="pointer-events-none relative text-bg opacity-0 peer-checked:opacity-100"
          aria-hidden="true"
        />
      </span>
      <span class="leading-snug"><slot /></span>
    </label>
    <p v-if="hint" :id="`${id}-hint`" class="pl-9 text-sm text-ink-muted">{{ hint }}</p>
    <p v-if="error" :id="`${id}-error`" class="pl-9 text-sm font-semibold text-danger">
      {{ error }}
    </p>
  </div>
</template>
