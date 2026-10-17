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
  <section class="flex flex-col items-start gap-3 px-4 py-8">
    <div
      class="flex h-28 w-full items-center justify-center rounded-control border border-rule bg-inserts"
      aria-hidden="true"
    >
      <HoldSwatch :color="hold" :size="56" />
    </div>
    <component :is="`h${String(headingLevel)}`" class="text-xl font-title text-balance">{{
      title
    }}</component>
    <p class="max-w-prose text-ink-muted">{{ body }}</p>
    <div v-if="$slots.default" class="mt-2"><slot /></div>
  </section>
</template>
