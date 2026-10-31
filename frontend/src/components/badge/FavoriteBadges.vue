<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import { useBadgeText } from '@/composables/useBadgeText'
import { formatList } from '@/lib/format'
import type { ShownBadge } from '@/lib/badges'

import BadgeMedal from './BadgeMedal.vue'

/** The favourite badges next to a name (D-106), small, at their level. */
const props = withDefaults(defineProps<{ badges: readonly ShownBadge[]; size?: number }>(), {
  size: 24,
})

const { t } = useI18n()
const text = useBadgeText()
const label = computed(() =>
  t('badges.srFavorites', {
    list: formatList(props.badges.map((shown) => text.label(shown.badge, shown.level))),
  }),
)
</script>

<template>
  <span v-if="badges.length > 0" class="inline-flex shrink-0 items-center gap-0.5">
    <span class="sr-only">{{ label }}</span>
    <BadgeMedal
      v-for="shown in badges"
      :key="shown.badge"
      :badge="shown.badge"
      :level="shown.level"
      :size="size"
    />
  </span>
</template>
