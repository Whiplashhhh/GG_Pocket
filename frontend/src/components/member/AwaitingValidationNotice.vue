<script setup lang="ts">
import { Hourglass } from 'lucide-vue-next'

/**
 * Says why a part of the app is closed to an account awaiting validation by the board (D-107), and
 * that it opens by itself once validated.
 */
withDefaults(
  defineProps<{
    /** Already translated; the bare body when absent (a closed corner of a screen). */
    title?: string
    /** Already translated, or the default slot. */
    body?: string
    headingLevel?: 2 | 3
  }>(),
  { title: undefined, body: '', headingLevel: 2 },
)
</script>

<template>
  <section class="flex items-start gap-3 border-l-4 border-brand bg-surface px-4 py-3">
    <Hourglass :size="22" class="mt-0.5 shrink-0 text-brand-ink" aria-hidden="true" />
    <div class="flex flex-col gap-1">
      <component
        :is="`h${String(headingLevel)}`"
        v-if="title"
        class="font-semibold text-balance text-ink"
      >
        {{ title }}
      </component>
      <p class="max-w-prose text-ink-muted">
        <slot>{{ body }}</slot>
      </p>
    </div>
  </section>
</template>
