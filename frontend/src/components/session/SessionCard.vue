<script setup lang="ts">
import { Lock } from 'lucide-vue-next'
import { computed, useId } from 'vue'
import { useI18n } from 'vue-i18n'

import type { Session } from '@/api/sessions'
import RouteSummaryLabel from '@/components/topo/RouteSummaryLabel.vue'
import { ascentOutcome, sessionDay } from '@/lib/sessionView'

/**
 * A session as a full-width block, Strava style (docs/DESIGN.md, « Actu »): figures, photo, the
 * routes, the comment. In a list it opens the session; on its own page it shows every ascent.
 * The climber's name joins it with the feed (phase 5): until then, all sessions are mine.
 */
const props = withDefaults(
  defineProps<{ session: Session; linked?: boolean; maxLines?: number }>(),
  {
    linked: false,
    maxLines: undefined,
  },
)

const { t } = useI18n()
const titleId = useId()

const day = computed(() => {
  const value = sessionDay(props.session.climbedOn)
  return value.kind === 'date'
    ? t('session.day.date', { date: value.date })
    : t(`session.day.${value.kind}`)
})
const lines = computed(() =>
  props.maxLines === undefined
    ? props.session.ascents
    : props.session.ascents.slice(0, props.maxLines),
)
const hidden = computed(() => props.session.ascents.length - lines.value.length)
const summary = computed(() => props.session.summary)
</script>

<template>
  <article
    class="relative flex flex-col gap-3 border-b border-rule px-4 py-4"
    :aria-labelledby="titleId"
  >
    <header class="flex items-baseline justify-between gap-3">
      <h2 :id="titleId" class="text-lg font-title">
        <RouterLink
          v-if="linked"
          :to="`/seances/${session.id}`"
          class="after:absolute after:inset-0 hover:underline"
          >{{ t('session.cardTitle', { day }) }}</RouterLink
        >
        <template v-else>{{ t('session.cardTitle', { day }) }}</template>
      </h2>
      <span
        v-if="session.visibility === 'PRIVATE'"
        class="inline-flex shrink-0 items-center gap-1 text-sm text-ink-muted"
      >
        <Lock :size="14" aria-hidden="true" />{{ t('session.visibilityPrivate') }}
      </span>
    </header>

    <p class="dot-list text-ink">
      <span>{{ t('session.summary.ascents', summary.ascentCount) }}</span>
      <span>{{ t('session.summary.topped', summary.toppedCount) }}</span>
      <span>{{ t('session.summary.lead', summary.leadCount) }}</span>
    </p>
    <p
      v-if="summary.maxGrade !== undefined || session.feeling !== undefined"
      class="dot-list -mt-2 text-ink-muted"
    >
      <span v-if="summary.maxGrade !== undefined">{{
        t('session.summary.max', { grade: summary.maxGrade })
      }}</span>
      <span v-if="session.feeling !== undefined">{{
        t('session.summary.feeling', { n: session.feeling })
      }}</span>
    </p>

    <img
      v-if="session.photoUrl"
      :src="session.photoUrl"
      :alt="t('session.photoAlt')"
      loading="lazy"
      class="max-h-80 w-full rounded-control bg-surface-sunken object-cover"
    />

    <ul class="flex flex-col gap-2">
      <li
        v-for="ascent in lines"
        :key="ascent.id"
        class="flex flex-wrap items-center gap-x-3 gap-y-1"
      >
        <RouteSummaryLabel :route="ascent.route" size="sm" />
        <span class="text-sm text-ink-muted">
          {{ ascent.mode === 'LEAD' ? t('session.lead') : t('session.topRope') }} ·
          {{
            t(
              ascentOutcome(ascent).key,
              { n: ascentOutcome(ascent).falls },
              ascentOutcome(ascent).falls,
            )
          }}
          <template v-if="ascent.firstTry"> · {{ t('session.firstTryShort') }}</template>
        </span>
      </li>
    </ul>
    <p v-if="hidden > 0" class="-mt-1 text-sm text-ink-muted">
      {{ t('session.moreLines', hidden) }}
    </p>

    <blockquote v-if="session.comment" class="font-hand text-lg leading-snug">
      {{ t('session.commentQuote', { comment: session.comment }) }}
    </blockquote>
  </article>
</template>
