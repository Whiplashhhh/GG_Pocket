<script setup lang="ts">
import { Lock, MessageCircle } from 'lucide-vue-next'
import { computed, useId } from 'vue'
import { useI18n } from 'vue-i18n'

import { useCurrentMember } from '@/api/auth'
import type { Session } from '@/api/sessions'
import SessionReactions from '@/components/session/SessionReactions.vue'
import RouteSummaryLabel from '@/components/topo/RouteSummaryLabel.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { ascentOutcome, sessionDay } from '@/lib/sessionView'

/**
 * A session as a full-width block, Strava style (docs/DESIGN.md, « Actu »): who climbed (in the
 * feed), figures, photo, the routes, the comment, then the reactions and the comment count. In a
 * list it opens the session; on its own page it shows every ascent.
 */
const props = withDefaults(
  defineProps<{
    session: Session
    linked?: boolean
    maxLines?: number
    /** h3 under a list heading, h2 on its own page. */
    headingLevel?: 2 | 3
    /** The climber's avatar and name on top (the feed, a friend's session). */
    showAuthor?: boolean
  }>(),
  { linked: false, maxLines: undefined, headingLevel: 2, showAuthor: false },
)

const { t } = useI18n()
const titleId = useId()
const member = useCurrentMember()
const mine = computed(() => member.value?.id === props.session.memberId)
const author = computed(() => props.session.author)
const authorLink = computed(() => (mine.value ? '/profil' : `/membres/${author.value.id}`))

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
    <header v-if="showAuthor" class="flex items-center gap-3">
      <RouterLink
        :to="authorLink"
        class="relative z-10 shrink-0 rounded-full"
        :aria-label="t('feed.authorProfile', { nickname: author.nickname })"
      >
        <UserAvatar
          :first-name="author.nickname"
          last-name=""
          :seed="author.nickname"
          :src="author.avatarUrl"
          :size="44"
        />
      </RouterLink>
      <div class="min-w-0 flex-1">
        <component :is="`h${headingLevel}`" :id="titleId" class="text-lg font-title break-words">
          <RouterLink
            v-if="linked"
            :to="`/seances/${session.id}`"
            class="after:absolute after:inset-0 hover:underline"
            ><span aria-hidden="true">{{ author.nickname }}</span
            ><span class="sr-only">{{
              t('feed.cardTitle', {
                title: t('session.cardTitle', { day }),
                nickname: author.nickname,
              })
            }}</span></RouterLink
          >
          <template v-else>{{ author.nickname }}</template>
        </component>
        <p class="text-sm text-ink-muted" :aria-hidden="linked ? true : undefined">
          {{ t('session.cardTitle', { day }) }}
        </p>
      </div>
      <span
        v-if="session.visibility === 'PRIVATE'"
        class="inline-flex shrink-0 items-center gap-1 text-sm text-ink-muted"
      >
        <Lock :size="14" aria-hidden="true" />{{ t('session.visibilityPrivate') }}
      </span>
    </header>
    <header v-else class="flex items-baseline justify-between gap-3">
      <component :is="`h${headingLevel}`" :id="titleId" class="text-lg font-title">
        <RouterLink
          v-if="linked"
          :to="`/seances/${session.id}`"
          class="after:absolute after:inset-0 hover:underline"
          >{{ t('session.cardTitle', { day }) }}</RouterLink
        >
        <template v-else>{{ t('session.cardTitle', { day }) }}</template>
      </component>
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
      <li v-for="ascent in lines" :key="ascent.id" class="flex flex-col items-start gap-1">
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

    <footer class="relative z-10 flex flex-wrap items-center justify-between gap-2 empty:hidden">
      <SessionReactions :session="session" :mine="mine" />
      <RouterLink
        v-if="linked"
        :to="`/seances/${session.id}`"
        class="-mx-1 inline-flex min-h-11 items-center gap-1.5 rounded-control px-1 text-sm font-semibold text-ink-muted hover:text-ink hover:underline"
      >
        <MessageCircle :size="18" aria-hidden="true" />{{
          t('feed.comments', session.commentCount)
        }}
      </RouterLink>
    </footer>
  </article>
</template>
