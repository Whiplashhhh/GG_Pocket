<script setup lang="ts">
import { ChevronRight } from 'lucide-vue-next'
import type { RouteLocationRaw } from 'vue-router'

/** One line of a settings list: full width, chevron, 48 px tall. */
defineProps<{ to: RouteLocationRaw }>()
</script>

<template>
  <RouterLink
    :to="to"
    class="flex min-h-12 items-center gap-3 border-b border-rule px-4 py-2 font-semibold hover:bg-surface-sunken"
  >
    <span class="text-ink-muted" aria-hidden="true"><slot name="icon" /></span>
    <span class="flex-1"><slot /></span>
    <ChevronRight :size="20" class="text-ink-muted" aria-hidden="true" />
  </RouterLink>
</template>
