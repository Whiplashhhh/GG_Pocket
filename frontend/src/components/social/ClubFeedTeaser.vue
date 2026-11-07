<script setup lang="ts">
import { Globe } from 'lucide-vue-next'
import { useId } from 'vue'
import { useI18n } from 'vue-i18n'

import AwaitingValidationNotice from '@/components/member/AwaitingValidationNotice.vue'

import ClubFeedJoinButton from './ClubFeedJoinButton.vue'

/**
 * The club tab of the feed for a member who does not take part (D-120): a few blurred templates
 * drawn here, never sessions from the server (nothing real leaves it), and why it is closed: to
 * see, one shows oneself. An account awaiting validation gets the same screen, without the button.
 */
defineProps<{ awaiting: boolean }>()

const { t } = useI18n()
const headingId = useId()

/** Template cards: widths of the lines only, no data. */
const templates = [
  { name: 'w-28', lines: ['w-40', 'w-56'], holds: 4 },
  { name: 'w-20', lines: ['w-48', 'w-32'], holds: 6 },
  { name: 'w-32', lines: ['w-36', 'w-52'], holds: 3 },
]
</script>

<template>
  <!-- One grid cell for both layers: the section is as tall as the taller of the two. -->
  <section class="grid" :aria-labelledby="headingId">
    <div
      class="pointer-events-none flex flex-col gap-3 px-3 pt-3 blur-[3px] select-none [grid-area:1/1] sm:px-4"
      aria-hidden="true"
      data-testid="club-feed-templates"
    >
      <div
        v-for="(card, index) in templates"
        :key="index"
        class="flex flex-col gap-3 rounded-control border border-rule bg-surface p-4"
      >
        <div class="flex items-center gap-3">
          <div class="size-10 shrink-0 rounded-full bg-surface-sunken" />
          <div class="flex flex-col gap-2">
            <div class="h-4 rounded-control bg-ink-muted/40" :class="card.name" />
            <div class="h-3 w-24 rounded-control bg-surface-sunken" />
          </div>
        </div>
        <div
          v-for="line in card.lines"
          :key="line"
          class="h-3 rounded-control bg-surface-sunken"
          :class="line"
        />
        <div class="flex gap-2">
          <div
            v-for="hold in card.holds"
            :key="hold"
            class="size-6 rounded-full"
            :class="hold % 2 === 0 ? 'bg-brand/50' : 'bg-ink-muted/30'"
          />
        </div>
      </div>
    </div>

    <!-- Above the blurred layer, which the filter puts in a stacking context of its own. -->
    <div class="relative z-10 px-4 pt-4 pb-6 [grid-area:1/1]">
      <div
        class="mx-auto flex max-w-md flex-col items-start gap-3 rounded-control border-2 border-ink bg-bg p-5"
      >
        <Globe :size="28" class="text-brand-ink" aria-hidden="true" />
        <h2 :id="headingId" class="text-xl font-title text-balance">
          {{ t('clubFeed.teaserTitle') }}
        </h2>
        <p class="max-w-prose text-ink">{{ t('clubFeed.teaserBody') }}</p>
        <p class="max-w-prose text-sm text-ink-muted">{{ t('clubFeed.teaserPrivacy') }}</p>
        <!-- The header already says why the account waits: only what it means here. -->
        <AwaitingValidationNotice v-if="awaiting" class="w-full" :body="t('awaiting.clubFeed')" />
        <ClubFeedJoinButton v-else />
      </div>
    </div>
  </section>
</template>
