<script setup lang="ts">
import type { HoldColor } from '@/lib/route'

import HoldSwatch from './HoldSwatch.vue'

withDefaults(
  defineProps<{
    title: string
    body: string
    /** The lonely hold bolted on the bare wall of the illustration. */
    hold?: HoldColor
    /** h1 when the empty state is the whole page (404), h2 under a page title otherwise. */
    headingLevel?: 1 | 2
  }>(),
  { hold: () => ({ label: '', hex: '#f9561a' }), headingLevel: 2 },
)
</script>

<template>
  <section class="flex flex-col items-start gap-3 pt-4 pb-8">
    <div
      class="flex h-32 w-full items-center justify-center border-y border-rule bg-inserts"
      aria-hidden="true"
    >
      <!-- Nudged so the bolt hole lands on the central insert of the grid. -->
      <HoldSwatch
        :color="hold"
        :size="56"
        class="translate-x-[-1.4px] translate-y-[0.5px] -rotate-12"
      />
    </div>
    <div class="flex flex-col items-start gap-3 px-4">
      <component :is="`h${String(headingLevel)}`" class="text-xl font-title text-balance">{{
        title
      }}</component>
      <p class="max-w-prose text-ink-muted">{{ body }}</p>
      <div v-if="$slots.default" class="mt-2"><slot /></div>
    </div>
  </section>
</template>
