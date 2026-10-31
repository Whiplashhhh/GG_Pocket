<script setup lang="ts">
import { ChevronDown } from 'lucide-vue-next'
import { computed, ref, useId } from 'vue'
import { useI18n } from 'vue-i18n'

import { useBadgeLibrary } from '@/api/badges'
import { errorMessageKey } from '@/api/errors'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import { useBadgeText } from '@/composables/useBadgeText'
import { badgeStates, recentBadges, type Badge } from '@/lib/badges'

import BadgeDialog from './BadgeDialog.vue'
import BadgeToken from './BadgeToken.vue'

/**
 * The badges of a profile, mine without a member id (D-106): how many are unlocked, the last three,
 * and every badge once (its colour is its level) behind a fold. A badge opens its details.
 */
const props = defineProps<{ memberId?: string; title: string; headingId: string }>()

const RECENT = 3

const { t } = useI18n()
const text = useBadgeText()
const library = useBadgeLibrary(() => props.memberId)
const states = computed(() => badgeStates(library.data.value?.badges ?? []))
const earned = computed(() => states.value.filter((state) => state.level > 0).length)
const recent = computed(() => recentBadges(states.value, RECENT))

const gridId = useId()
const expanded = ref(false)
const selected = ref<Badge>()
const selectedState = computed(() => states.value.find((state) => state.badge === selected.value))
const dialogOpen = computed({
  get: () => selected.value !== undefined,
  set: (open) => {
    if (!open) selected.value = undefined
  },
})
</script>

<template>
  <section class="border-t border-rule px-4 pt-5 pb-5" :aria-labelledby="headingId">
    <h2 :id="headingId" class="text-lg font-title">{{ title }}</h2>
    <div v-if="library.isPending.value" class="flex gap-3 pt-3" aria-hidden="true">
      <SkeletonBlock v-for="index in RECENT" :key="index" class="size-13 rounded-full" />
    </div>
    <div v-else-if="library.isError.value" class="flex flex-col items-start gap-3 pt-3">
      <FormAlert>
        {{ t('badges.loadError') }} {{ t(errorMessageKey(library.error.value)) }}
      </FormAlert>
      <GgButton variant="secondary" @click="library.refetch()">{{ t('common.retry') }}</GgButton>
    </div>
    <template v-else-if="library.data.value">
      <p class="text-sm text-ink-muted">
        {{ t('badges.earnedCount', { earned, total: states.length }) }}
      </p>

      <template v-if="recent.length > 0">
        <h3 class="pt-3 text-sm font-semibold">{{ t('badges.recent') }}</h3>
        <ul class="flex gap-3 pt-2">
          <li v-for="state in recent" :key="state.badge">
            <button
              type="button"
              class="flex min-h-11 min-w-11 rounded-control p-1"
              :aria-label="text.label(state.badge, state.level)"
              @click="selected = state.badge"
            >
              <BadgeToken :badge="state.badge" :level="state.level" />
            </button>
          </li>
        </ul>
      </template>
      <p v-else class="max-w-prose pt-2 text-ink-muted">
        {{ memberId === undefined ? t('badges.noneYet') : t('badges.theirNoneYet') }}
      </p>

      <button
        type="button"
        class="mt-3 -ml-2 flex min-h-11 items-center gap-1.5 rounded-control px-2 font-semibold text-brand-ink"
        :aria-expanded="expanded"
        :aria-controls="gridId"
        @click="expanded = !expanded"
      >
        {{ expanded ? t('badges.hideAll') : t('badges.showAll') }}
        <ChevronDown
          :size="18"
          class="transition-transform duration-200 motion-reduce:transition-none"
          :class="expanded ? 'rotate-180' : ''"
          aria-hidden="true"
        />
      </button>
      <ul
        v-show="expanded"
        :id="gridId"
        class="grid grid-cols-5 justify-items-center gap-y-3 pt-2 sm:grid-cols-8"
        :aria-label="title"
      >
        <li v-for="state in states" :key="state.badge" :data-level="state.level">
          <button
            type="button"
            class="flex min-h-11 min-w-11 rounded-control p-1"
            :aria-label="text.label(state.badge, state.level)"
            @click="selected = state.badge"
          >
            <BadgeToken :badge="state.badge" :level="state.level" />
          </button>
        </li>
      </ul>

      <BadgeDialog
        v-if="selectedState"
        v-model:open="dialogOpen"
        :state="selectedState"
        :mine="memberId === undefined"
        :progress="library.data.value.progress"
        :favorites="library.data.value.favorites"
      />
    </template>
  </section>
</template>
