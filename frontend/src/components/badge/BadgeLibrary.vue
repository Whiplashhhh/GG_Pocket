<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import type { BadgeLibraryEntry } from '@/api/badges'
import { useBadgeText } from '@/composables/useBadgeText'
import { badgeMark, badgeSections } from '@/lib/badges'
import { formatDate } from '@/lib/format'

import BadgeMedal from './BadgeMedal.vue'

/**
 * Every badge of the catalogue (spec §8.8): earned ones in colour, the others bare with what it
 * takes. Tiers sit side by side under their family; repeatable badges get a line each, with ×N.
 */
const props = defineProps<{ entries: readonly BadgeLibraryEntry[]; idPrefix: string }>()

const { t } = useI18n()
const text = useBadgeText()
const sections = computed(() => badgeSections(props.entries))

function status(entry: BadgeLibraryEntry): string {
  if (entry.count === 0 || entry.lastEarnedOn === undefined) {
    return `${t('badges.srNotYet')}. ${text.condition(entry.code)}`
  }
  const date = formatDate(entry.lastEarnedOn)
  return entry.repeatable && entry.count > 1
    ? t('badges.srRepeated', { n: entry.count, date })
    : t('badges.srEarned', { date })
}

function earnedLine(entry: BadgeLibraryEntry): string {
  const date = formatDate(entry.lastEarnedOn ?? entry.firstEarnedOn ?? '')
  return entry.count > 1 ? t('badges.lastEarnedOn', { date }) : t('badges.earnedOn', { date })
}
</script>

<template>
  <div v-for="section in sections" :key="section.family" class="pt-4">
    <h3 :id="`${idPrefix}-${section.family}`" class="px-4 font-semibold">
      {{ t(`badges.families.${section.family}.title`) }}
      <span class="text-sm font-normal text-ink-muted">
        {{ section.earned }}/{{ section.entries.length }}
      </span>
    </h3>
    <p class="max-w-prose px-4 text-sm text-ink-muted">
      {{ t(`badges.families.${section.family}.hint`) }}
    </p>

    <ul
      v-if="section.family !== 'REPEATABLE'"
      class="flex flex-wrap gap-y-2 px-2 pt-2"
      :aria-labelledby="`${idPrefix}-${section.family}`"
    >
      <li
        v-for="entry in section.entries"
        :key="entry.code"
        class="flex w-16 flex-col items-center"
        :data-earned="entry.count > 0"
      >
        <BadgeMedal :code="entry.code" :earned="entry.count > 0" />
        <span
          class="text-sm font-semibold"
          :class="entry.count > 0 ? 'text-ink' : 'text-ink-muted'"
          aria-hidden="true"
          >{{ badgeMark(entry.code) }}</span
        >
        <span class="sr-only">{{ text.name(entry.code) }} : {{ status(entry) }}</span>
      </li>
    </ul>

    <ul
      v-else
      class="grid grid-cols-1 gap-x-6 px-4 pt-1 sm:grid-cols-2"
      :aria-labelledby="`${idPrefix}-${section.family}`"
    >
      <li
        v-for="entry in section.entries"
        :key="entry.code"
        class="flex items-start gap-3 py-2.5"
        :data-earned="entry.count > 0"
      >
        <BadgeMedal :code="entry.code" :earned="entry.count > 0" />
        <div class="flex min-w-0 flex-1 flex-col">
          <p class="flex flex-wrap items-baseline gap-x-2">
            <span class="font-semibold" :class="entry.count > 0 ? '' : 'text-ink-muted'">
              {{ text.name(entry.code) }}
            </span>
            <span
              v-if="entry.count > 0"
              class="rounded-control bg-ink px-1.5 text-sm font-semibold text-bg"
              >{{ t('badges.times', { n: entry.count }) }}</span
            >
          </p>
          <p class="text-sm text-ink-muted">{{ text.condition(entry.code) }}</p>
          <p v-if="entry.count > 0" class="text-sm text-ink-muted">{{ earnedLine(entry) }}</p>
        </div>
      </li>
    </ul>
  </div>
</template>
