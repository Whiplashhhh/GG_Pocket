<script setup lang="ts">
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { computed, onBeforeUnmount, ref, useId, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import { useCurrentMember } from '@/api/auth'
import { errorMessageKey } from '@/api/errors'
import { useRanking, useRankingMonths, type RankingLine } from '@/api/rankings'
import PageTitle from '@/components/layout/PageTitle.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SegmentedControl from '@/components/ui/SegmentedControl.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { clubToday } from '@/lib/age'
import {
  canShowNext,
  canShowPrevious,
  formatMonth,
  parseMonthQuery,
  shiftMonth,
  type Month,
} from '@/lib/planning'
import { belowPodium, formatFreezeDay, isTie, podiumSteps, profileLink } from '@/lib/podium'

/**
 * Spec §8.7: the two rankings of a month, the top three on a podium, then everyone else. My place
 * stays in sight: when my line scrolls away, a bar above the tab bar recalls it.
 */
type Board = 'sessions' | 'ascents'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const member = useCurrentMember()
const today = clubToday()
const monthTitleId = useId()
const restTitleId = useId()

const month = computed(() => parseMonthQuery(route.query, today))
const board = computed<Board>(() => (route.query.classement === 'voies' ? 'ascents' : 'sessions'))
const ranking = useRanking(month)
const months = useRankingMonths()

function show(next: { month?: Month; board?: Board }) {
  const shownMonth = next.month ?? month.value
  const shownBoard = next.board ?? board.value
  void router.replace({
    path: '/podium',
    query: {
      ...(shownMonth === today.slice(0, 7) ? {} : { mois: shownMonth }),
      ...(shownBoard === 'ascents' ? { classement: 'voies' } : {}),
    },
  })
}

const boardModel = computed<Board>({
  get: () => board.value,
  set: (value) => {
    show({ board: value })
  },
})
const boardOptions = computed(() => [
  { value: 'sessions' as const, label: t('podium.boards.sessions') },
  { value: 'ascents' as const, label: t('podium.boards.ascents') },
])

/** The oldest month with rankings: no going further back. */
const firstMonth = computed(() => months.data.value?.at(-1)?.month)
const previousAllowed = computed(
  () =>
    canShowPrevious(month.value) &&
    (firstMonth.value === undefined || month.value > firstMonth.value),
)

/** While the next month loads, the previous one stays: show nothing of it as if it were this one. */
const current = computed(() =>
  ranking.data.value?.month === month.value ? ranking.data.value : undefined,
)
const shown = computed(() => current.value?.[board.value])
const lines = computed(() => shown.value?.lines ?? [])
const steps = computed(() => podiumSteps(lines.value))
const rest = computed(() => belowPodium(lines.value))
const me = computed(() => shown.value?.me)

const statusText = computed(() => {
  const value = current.value
  if (value === undefined) return ''
  if (value.status === 'PROVISIONAL' && value.freezesAt) {
    return t('podium.status.PROVISIONAL', { day: formatFreezeDay(value.freezesAt) })
  }
  return t(`podium.status.${value.status}`)
})

function rankLabel(rank: number): string {
  return rank === 1 ? t('podium.rankFirst') : t('podium.rank', { rank })
}

function valueLabel(value: number): string {
  return t(`podium.value.${board.value}`, value)
}

function isMine(line: RankingLine): boolean {
  return line.member.id === member.value?.id
}

/** My line, on the podium or in the list: watched to know when it leaves the screen. */
const myLine = ref<HTMLElement | null>(null)
const myLineInSight = ref(true)
let observer: IntersectionObserver | undefined

watch(
  myLine,
  (element) => {
    observer?.disconnect()
    myLineInSight.value = true
    if (element === null || typeof IntersectionObserver === 'undefined') return
    observer = new IntersectionObserver(
      ([entry]) => {
        myLineInSight.value = entry?.isIntersecting ?? true
      },
      // The tab bar covers the bottom 4rem.
      { rootMargin: '0px 0px -64px 0px' },
    )
    observer.observe(element)
  },
  { flush: 'post' },
)
onBeforeUnmount(() => observer?.disconnect())

function setMyLine(line: RankingLine) {
  return (element: unknown) => {
    if (isMine(line)) myLine.value = element instanceof HTMLElement ? element : null
  }
}

const stepClass: Record<string, string> = {
  first: 'h-24 bg-brand text-on-brand',
  second: 'h-16 bg-olive text-on-olive',
  third: 'h-12 bg-surface-sunken text-ink',
}
const columnClass: Record<number, string> = {
  1: 'col-start-1',
  2: 'col-start-2',
  3: 'col-start-3',
}
</script>

<template>
  <PageTitle>{{ t('podium.title') }}</PageTitle>

  <section class="px-4 pt-3" :aria-labelledby="monthTitleId">
    <div class="flex items-center justify-between gap-2">
      <button
        type="button"
        class="inline-flex size-11 shrink-0 items-center justify-center rounded-control border-2 border-rule text-ink hover:border-ink disabled:opacity-40"
        :aria-label="t('podium.previous')"
        :disabled="!previousAllowed"
        @click="show({ month: shiftMonth(month, -1) })"
      >
        <ChevronLeft :size="24" aria-hidden="true" />
      </button>
      <h2 :id="monthTitleId" class="text-lg font-title first-letter:uppercase">
        {{ formatMonth(month) }}
      </h2>
      <button
        type="button"
        class="inline-flex size-11 shrink-0 items-center justify-center rounded-control border-2 border-rule text-ink hover:border-ink disabled:opacity-40"
        :aria-label="t('podium.next')"
        :disabled="!canShowNext(month, today)"
        @click="show({ month: shiftMonth(month, 1) })"
      >
        <ChevronRight :size="24" aria-hidden="true" />
      </button>
    </div>
    <p class="mt-1 min-h-10 text-center text-sm text-ink-muted" aria-live="polite">
      {{ statusText }}
    </p>
    <SegmentedControl
      v-model="boardModel"
      class="mt-2"
      :label="t('podium.boardChoice')"
      :options="boardOptions"
      stretch
    />
  </section>

  <div v-if="ranking.isPending.value" class="px-4 pt-6" aria-hidden="true">
    <p class="sr-only" role="status">{{ t('podium.loading') }}</p>
    <SkeletonBlock class="h-40 w-full" />
  </div>
  <div v-else-if="ranking.isError.value" class="flex flex-col items-start gap-3 px-4 py-6">
    <FormAlert>{{ t(errorMessageKey(ranking.error.value)) }}</FormAlert>
    <GgButton variant="secondary" @click="ranking.refetch()">{{ t('common.retry') }}</GgButton>
  </div>

  <template v-else-if="current">
    <EmptyState
      v-if="lines.length === 0"
      :title="t('podium.emptyTitle')"
      :body="current.status === 'LIVE' ? t('podium.emptyBody') : t('podium.emptyPastBody')"
      :hold="{ label: '', hex: '#f5c518' }"
    />

    <template v-else>
      <ol
        class="mt-4 grid grid-cols-3 items-end gap-2 border-b border-rule bg-inserts px-4 pt-4"
        :aria-label="t('podium.podiumLabel')"
      >
        <li
          v-for="(step, index) in steps"
          :key="`${String(index)}-${step.line.member.id}`"
          :ref="setMyLine(step.line)"
          class="row-start-1 flex min-w-0 flex-col items-center gap-1"
          :class="columnClass[step.column]"
        >
          <component
            :is="profileLink(step.line, member?.id) ? RouterLink : 'span'"
            :to="profileLink(step.line, member?.id)"
            class="flex min-h-11 max-w-full min-w-0 flex-col items-center gap-1 rounded-control"
            :aria-label="
              profileLink(step.line, member?.id)
                ? t('podium.profileOf', { nickname: step.line.member.nickname })
                : undefined
            "
          >
            <UserAvatar
              :first-name="step.line.member.nickname"
              last-name=""
              :seed="step.line.member.nickname"
              :src="step.line.member.avatarUrl"
              :size="step.height === 'first' ? 64 : 52"
              :class="isMine(step.line) ? 'ring-2 ring-brand-ink ring-offset-2 ring-offset-bg' : ''"
            />
            <span class="max-w-full truncate text-base font-semibold">
              {{ step.line.member.nickname }}
            </span>
          </component>
          <span v-if="isMine(step.line)" class="text-micro font-semibold text-brand-ink">
            {{ t('podium.me') }}
          </span>
          <span class="text-sm text-ink-muted">{{ valueLabel(step.line.value) }}</span>
          <div
            class="flex w-full items-start justify-center rounded-t-control pt-1"
            :class="stepClass[step.height]"
          >
            <span class="text-2xl font-cord">{{ rankLabel(step.line.rank) }}</span>
            <span class="sr-only">
              <template v-if="isTie(lines, step.line)">{{ t('podium.tie') }}</template>
            </span>
          </div>
        </li>
      </ol>

      <section v-if="rest.length > 0" class="mt-4" :aria-labelledby="restTitleId">
        <h2 :id="restTitleId" class="sr-only">{{ t('podium.restLabel') }}</h2>
        <ol class="border-t border-rule">
          <li
            v-for="(line, index) in rest"
            :key="`${String(index)}-${line.member.id}`"
            :ref="setMyLine(line)"
            class="flex min-h-14 items-center gap-3 border-b border-rule px-4 py-2"
            :class="isMine(line) ? 'border-l-4 border-l-brand bg-surface pl-3' : ''"
          >
            <span class="w-10 shrink-0 text-lg font-cord tabular-nums">
              {{ rankLabel(line.rank) }}
            </span>
            <component
              :is="profileLink(line, member?.id) ? RouterLink : 'span'"
              :to="profileLink(line, member?.id)"
              class="flex min-h-11 min-w-0 flex-1 items-center gap-3 rounded-control"
            >
              <UserAvatar
                :first-name="line.member.nickname"
                last-name=""
                :seed="line.member.nickname"
                :src="line.member.avatarUrl"
                :size="40"
              />
              <span class="min-w-0 truncate text-base font-semibold">
                {{ line.member.nickname }}
              </span>
              <span v-if="isMine(line)" class="text-micro font-semibold text-brand-ink">
                {{ t('podium.me') }}
              </span>
              <span v-if="isTie(lines, line)" class="shrink-0 text-micro text-ink-muted">
                {{ t('podium.tie') }}
              </span>
            </component>
            <span class="shrink-0 text-sm font-semibold tabular-nums">
              {{ valueLabel(line.value) }}
            </span>
          </li>
        </ol>
      </section>
    </template>

    <p
      v-if="!current.appearsInRankings"
      class="px-4 py-4 text-sm text-ink-muted"
      data-testid="podium-opted-out"
    >
      {{ t('podium.optedOut') }}
      <RouterLink
        to="/profil/modifier"
        class="inline-flex min-h-11 items-center font-semibold text-brand-ink underline"
      >
        {{ t('podium.optedOutLink') }}
      </RouterLink>
    </p>
    <p v-else-if="!me && lines.length > 0" class="px-4 py-4 text-sm text-ink-muted">
      {{ current.status === 'LIVE' ? t('podium.notRanked') : t('podium.notRankedPast') }}
    </p>

    <div
      v-if="me && !myLineInSight"
      class="sticky bottom-[calc(4rem+env(safe-area-inset-bottom))] z-20 mt-2 flex min-h-14 items-center gap-3 border-y border-rule bg-surface px-4 py-2 shadow-sheet"
      role="status"
      data-testid="podium-my-place"
    >
      <span class="text-sm font-semibold text-ink-muted">{{ t('podium.myPlace') }}</span>
      <span class="text-lg font-cord tabular-nums">{{ rankLabel(me.rank) }}</span>
      <span v-if="isTie(lines, me)" class="text-micro text-ink-muted">{{ t('podium.tie') }}</span>
      <span class="ml-auto text-sm font-semibold tabular-nums">{{ valueLabel(me.value) }}</span>
    </div>
  </template>
</template>
