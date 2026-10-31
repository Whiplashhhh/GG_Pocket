<script setup lang="ts">
import { computed, useId } from 'vue'
import { useI18n } from 'vue-i18n'

import { useBadgeText } from '@/composables/useBadgeText'
import { isMaxLevel, isTiered, unlocks, type BadgeCode, type Unlock } from '@/lib/badges'

import BadgeMedal from './BadgeMedal.vue'

/**
 * The badges a session unlocked, in its card (spec §8.3: « a débloqué : … »): one line per badge,
 * the tiers of a family folded into the highest.
 */
const props = defineProps<{ codes: readonly BadgeCode[]; title: string }>()

const { t } = useI18n()
const text = useBadgeText()
const titleId = useId()
const items = computed(() => unlocks(props.codes))

function detail(unlock: Unlock): string | undefined {
  if (isTiered(unlock.badge)) {
    return isMaxLevel(unlock.badge, unlock.level)
      ? t('badges.levelMax')
      : t('badges.level', { n: unlock.level })
  }
  return unlock.times > 1 ? t('badges.times', { n: unlock.times }) : undefined
}
</script>

<template>
  <div class="rounded-control bg-surface-sunken px-3 py-2.5">
    <p :id="titleId" class="text-sm font-semibold">{{ title }}</p>
    <ul class="mt-1.5 flex flex-wrap gap-x-4 gap-y-2" :aria-labelledby="titleId">
      <li v-for="unlock in items" :key="unlock.badge" class="flex items-center gap-1.5 text-sm">
        <BadgeMedal :badge="unlock.badge" :level="unlock.level" :size="28" />
        <span>
          {{ text.name(unlock.badge) }}
          <span v-if="detail(unlock)" class="text-ink-muted">· {{ detail(unlock) }}</span>
        </span>
      </li>
    </ul>
  </div>
</template>
