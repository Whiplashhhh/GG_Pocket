<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import { badgeMark, isTiered, type Badge } from '@/lib/badges'

import BadgeMedal from './BadgeMedal.vue'

/**
 * A medal with its marks: the number of the week and month badges sharing a drawing (top right),
 * and ×N under a repeatable badge earned (bottom right). Decorative, like the medal.
 */
withDefaults(defineProps<{ badge: Badge; level: number; size?: number }>(), { size: 52 })

const { t } = useI18n()
</script>

<template>
  <span class="relative inline-flex shrink-0" aria-hidden="true">
    <BadgeMedal :badge="badge" :level="level" :size="size" />
    <span
      v-if="badgeMark(badge)"
      class="absolute -top-0.5 -right-0.5 min-w-5 rounded-full border border-bg bg-surface px-1 text-center text-micro font-semibold text-ink"
      >{{ badgeMark(badge) }}</span
    >
    <span
      v-if="!isTiered(badge) && level > 0"
      class="absolute -right-1 -bottom-0.5 rounded-control bg-ink px-1 text-micro font-semibold text-bg"
      >{{ t('badges.times', { n: level }) }}</span
    >
  </span>
</template>
