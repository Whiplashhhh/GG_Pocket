<script setup lang="ts">
import { ArrowLeft } from 'lucide-vue-next'
import type { RouteLocationRaw } from 'vue-router'

import GgIconButton from '@/components/ui/GgIconButton.vue'

/** Title of a screen reached from another one, with the way back. */
defineProps<{ back: RouteLocationRaw; backLabel: string }>()
</script>

<template>
  <div class="flex items-center gap-1 px-2 pt-3">
    <GgIconButton :to="back" :label="backLabel">
      <ArrowLeft :size="24" aria-hidden="true" />
    </GgIconButton>
    <h1 class="min-w-0 text-xl font-title wrap-anywhere"><slot /></h1>
  </div>
</template>
