<script setup lang="ts">
import { Star } from 'lucide-vue-next'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import { useChooseFavoriteBadges } from '@/api/badges'
import { errorMessageKey } from '@/api/errors'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import GgDialog from '@/components/ui/GgDialog.vue'
import { useBadgeText } from '@/composables/useBadgeText'
import {
  isMaxLevel,
  isTiered,
  nextProgress,
  nextTier,
  type Badge,
  type BadgeProgress,
  type BadgeState,
} from '@/lib/badges'
import { formatDate } from '@/lib/format'

import BadgeToken from './BadgeToken.vue'

/**
 * One badge up close (D-106): its level, what it takes, the way to the next level (counted from
 * zero, my own library only), and in my library the switch to show it next to my name.
 */
const props = defineProps<{
  state: BadgeState
  /** My library: the progress bar and the favourites. */
  mine: boolean
  progress?: BadgeProgress
  favorites: readonly Badge[]
}>()

const open = defineModel<boolean>('open', { required: true })

const MAX_FAVORITES = 3

const { t } = useI18n()
const text = useBadgeText()
const choose = useChooseFavoriteBadges()

const tiered = computed(() => isTiered(props.state.badge))
const levelLabel = computed(() => {
  const { badge, level } = props.state
  if (level <= 0) return t('badges.notYet')
  if (!tiered.value) return t('badges.times', { n: level })
  return isMaxLevel(badge, level) ? t('badges.levelMax') : t('badges.level', { n: level })
})
const earnedLine = computed(() => {
  const { level, firstEarnedOn, lastEarnedOn } = props.state
  if (level <= 0 || lastEarnedOn === undefined) return undefined
  const last = formatDate(lastEarnedOn)
  return tiered.value || level === 1
    ? t('badges.earnedOnce', { date: last })
    : t('badges.earnedMany', { n: level, first: formatDate(firstEarnedOn ?? lastEarnedOn), last })
})
const next = computed(() => nextTier(props.state))
const bar = computed(() => (props.mine ? nextProgress(props.state, props.progress) : undefined))
const percent = computed(() =>
  bar.value === undefined ? 0 : Math.min(100, (100 * bar.value.value) / bar.value.target),
)

const favorite = computed(() => props.favorites.includes(props.state.badge))
const full = computed(() => !favorite.value && props.favorites.length >= MAX_FAVORITES)

function toggleFavorite() {
  const badge = props.state.badge
  choose.mutate(
    favorite.value
      ? props.favorites.filter((chosen) => chosen !== badge)
      : [...props.favorites, badge],
  )
}
</script>

<template>
  <GgDialog v-model:open="open" :title="text.name(state.badge)">
    <div class="flex items-center gap-4">
      <BadgeToken :badge="state.badge" :level="state.level" :size="88" />
      <div class="flex min-w-0 flex-col gap-1">
        <p class="text-lg font-semibold" data-testid="badge-level">{{ levelLabel }}</p>
        <p v-if="earnedLine" class="text-sm text-ink-muted">{{ earnedLine }}</p>
      </div>
    </div>

    <p class="mt-4 max-w-prose">{{ text.description(state.badge) }}</p>

    <template v-if="tiered">
      <p v-if="next" class="mt-3 font-semibold">
        {{ t('badges.next', { condition: text.tierCondition(next) }) }}
      </p>
      <p v-else class="mt-3 font-semibold">{{ t('badges.maxReached') }}</p>
      <div v-if="bar" class="mt-2 flex items-center gap-3">
        <div
          class="h-3 flex-1 overflow-hidden rounded-full bg-surface-sunken"
          role="progressbar"
          :aria-label="t('badges.progressLabel')"
          aria-valuemin="0"
          :aria-valuemax="bar.target"
          :aria-valuenow="bar.value"
          :aria-valuetext="t('badges.progress', bar)"
        >
          <div class="h-full rounded-full bg-brand" :style="{ width: `${percent}%` }" />
        </div>
        <span class="shrink-0 text-sm font-semibold tabular-nums" aria-hidden="true">
          {{ bar.value }}/{{ bar.target }}
        </span>
      </div>
    </template>

    <div v-if="mine && state.level > 0" class="mt-5 border-t border-rule pt-4">
      <GgButton
        variant="secondary"
        :disabled="full || choose.isPending.value"
        :aria-pressed="favorite"
        @click="toggleFavorite"
      >
        <Star
          :size="18"
          :fill="favorite ? 'currentColor' : 'none'"
          class="text-brand-ink"
          aria-hidden="true"
        />
        {{ favorite ? t('badges.unfavorite') : t('badges.favorite') }}
      </GgButton>
      <p class="mt-2 text-sm text-ink-muted">
        {{ full ? t('badges.favoriteFull') : t('badges.favoriteHint') }}
      </p>
      <FormAlert v-if="choose.isError.value" class="mt-2">
        {{ t('badges.favoriteError') }} {{ t(errorMessageKey(choose.error.value)) }}
      </FormAlert>
    </div>

    <div class="mt-5 flex justify-end">
      <GgButton variant="ghost" @click="open = false">{{ t('badges.close') }}</GgButton>
    </div>
  </GgDialog>
</template>
