<script setup lang="ts">
import { Check, ChevronLeft, ChevronRight, Plus } from 'lucide-vue-next'
import { computed, ref, useId, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

import { errorMessageKey } from '@/api/errors'
import { useMyPlanning, type PlanningDay } from '@/api/sessions'
import EmptyState from '@/components/ui/EmptyState.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import { clubToday } from '@/lib/age'
import { formatDate } from '@/lib/format'
import {
  calendarWeeks,
  canLogOn,
  canShowNext,
  canShowPrevious,
  formatFeeling,
  formatMonth,
  parseMonthQuery,
  shiftMonth,
  type Month,
  WEEKDAYS,
} from '@/lib/planning'

/**
 * Spec §8.6: my month at the gym. A day with a session carries a tick; touching it shows that
 * day's sessions under the calendar. The summary and the streak come computed from the server.
 */
const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const today = clubToday()
const titleId = useId()
const dayTitleId = useId()

const month = computed(() => parseMonthQuery(route.query, today))
const planning = useMyPlanning(month)
const selected = ref<string | undefined>()

watch(month, () => {
  selected.value = undefined
})

function showMonth(next: Month) {
  void router.replace({
    path: '/planning',
    query: next === today.slice(0, 7) ? {} : { mois: next },
  })
}

const data = computed(() => planning.data.value)
/** While the next month loads, the previous one stays: show nothing of it as if it were this one. */
const current = computed(() => (data.value?.month === month.value ? data.value : undefined))
const byDate = computed(() => {
  const map = new Map<string, PlanningDay>()
  for (const day of current.value?.days ?? []) map.set(day.date, day)
  return map
})
const weeks = computed(() => calendarWeeks(month.value))
const selectedDay = computed(() =>
  selected.value === undefined ? undefined : byDate.value.get(selected.value),
)
const totals = computed(() => current.value?.totals)
const streak = computed(() => current.value?.streak)

function dayLabel(date: string): string {
  const formatted = formatDate(date)
  if (byDate.value.has(date)) return t('planning.dayClimbed', { date: formatted })
  if (date === today) return t('planning.dayToday', { date: formatted })
  return formatted
}

function select(date: string) {
  selected.value = selected.value === date ? undefined : date
}
</script>

<template>
  <div class="flex items-baseline justify-between gap-3 px-4 pt-5">
    <h1 class="text-xl font-title">{{ t('planning.title') }}</h1>
  </div>

  <section class="px-4 pt-3" :aria-labelledby="titleId">
    <div class="flex items-center justify-between gap-2">
      <button
        type="button"
        class="inline-flex size-11 items-center justify-center rounded-control border-2 border-rule text-ink hover:border-ink disabled:opacity-40"
        :aria-label="t('planning.previous')"
        :disabled="!canShowPrevious(month)"
        @click="showMonth(shiftMonth(month, -1))"
      >
        <ChevronLeft :size="24" aria-hidden="true" />
      </button>
      <div class="flex flex-col items-center">
        <h2 :id="titleId" class="text-lg font-title first-letter:uppercase">
          {{ formatMonth(month) }}
        </h2>
        <p class="text-sm font-semibold text-ink-muted" aria-live="polite">
          <template v-if="totals">{{ t('planning.presences', totals.climbingDays) }}</template>
          <template v-else>&nbsp;</template>
        </p>
      </div>
      <button
        type="button"
        class="inline-flex size-11 items-center justify-center rounded-control border-2 border-rule text-ink hover:border-ink disabled:opacity-40"
        :aria-label="t('planning.next')"
        :disabled="!canShowNext(month, today)"
        @click="showMonth(shiftMonth(month, 1))"
      >
        <ChevronRight :size="24" aria-hidden="true" />
      </button>
    </div>

    <table
      class="mt-3 w-full table-fixed border-collapse"
      :aria-label="t('planning.calendar', { month: formatMonth(month) })"
    >
      <thead>
        <tr>
          <th
            v-for="day in WEEKDAYS"
            :key="day.name"
            scope="col"
            class="pb-1 text-center text-xs font-semibold text-ink-muted"
          >
            <abbr :title="day.name" class="no-underline">{{ day.short }}</abbr>
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(week, index) in weeks" :key="index">
          <td v-for="(cell, column) in week" :key="column" class="p-0.5 text-center">
            <button
              v-if="cell"
              type="button"
              class="relative inline-flex h-12 w-full flex-col items-center justify-center rounded-control border-2 text-base font-semibold tabular-nums transition-colors duration-150"
              :class="[
                selected === cell.date
                  ? 'border-ink bg-ink text-bg'
                  : byDate.has(cell.date)
                    ? 'border-brand bg-brand/15 text-ink'
                    : 'border-transparent text-ink hover:border-rule',
                cell.date === today && selected !== cell.date
                  ? 'outline-2 outline-offset-[-6px] outline-ink outline-dotted'
                  : '',
                cell.date > today ? 'text-ink-muted' : '',
              ]"
              :aria-label="dayLabel(cell.date)"
              :aria-pressed="selected === cell.date"
              :aria-controls="dayTitleId"
              :disabled="cell.date > today"
              @click="select(cell.date)"
            >
              <span aria-hidden="true">{{ cell.day }}</span>
              <Check
                v-if="byDate.has(cell.date)"
                :size="14"
                :stroke-width="3.5"
                class="-mt-0.5"
                :class="selected === cell.date ? 'text-bg' : 'text-brand-ink'"
                aria-hidden="true"
              />
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </section>

  <div v-if="planning.isPending.value" class="px-4 pt-4" aria-hidden="true">
    <p class="sr-only" role="status">{{ t('planning.loading') }}</p>
    <SkeletonBlock class="h-24 w-full" />
  </div>
  <div v-else-if="planning.isError.value" class="flex flex-col items-start gap-3 px-4 py-6">
    <FormAlert>{{ t(errorMessageKey(planning.error.value)) }}</FormAlert>
    <GgButton variant="secondary" @click="planning.refetch()">{{ t('common.retry') }}</GgButton>
  </div>

  <template v-else-if="current">
    <section
      v-if="selected"
      :id="dayTitleId"
      class="mt-3 border-y border-rule px-4 py-3"
      aria-live="polite"
    >
      <h2 class="text-base font-title">
        {{ t('planning.dayTitle', { date: formatDate(selected) }) }}
      </h2>
      <ul v-if="selectedDay" class="mt-1">
        <li v-for="session in selectedDay.sessions" :key="session.id">
          <RouterLink
            :to="`/seances/${session.id}`"
            class="flex min-h-11 flex-wrap items-center gap-x-2 py-1 font-semibold hover:underline"
            :aria-label="t('planning.openSession', { date: formatDate(selected) })"
          >
            <span>{{ t('session.summary.ascents', session.summary.ascentCount) }}</span>
            <span aria-hidden="true">·</span>
            <span>{{ t('session.summary.topped', session.summary.toppedCount) }}</span>
            <template v-if="session.summary.maxGrade">
              <span aria-hidden="true">·</span>
              <span>{{ t('session.summary.max', { grade: session.summary.maxGrade }) }}</span>
            </template>
            <template v-if="session.feeling !== undefined">
              <span aria-hidden="true">·</span>
              <span>{{ t('session.summary.feeling', { n: session.feeling }) }}</span>
            </template>
            <ChevronRight :size="18" class="ml-auto shrink-0" aria-hidden="true" />
          </RouterLink>
        </li>
      </ul>
      <template v-else>
        <p class="mt-1 text-ink-muted">{{ t('planning.dayEmpty') }}</p>
        <RouterLink
          v-if="canLogOn(selected, today)"
          :to="{ path: '/seance/nouvelle', query: { jour: selected } }"
          class="mt-2 inline-flex min-h-11 items-center gap-1.5 rounded-control border-2 border-ink px-3 font-semibold hover:bg-ink hover:text-bg"
        >
          <Plus :size="18" aria-hidden="true" />{{ t('planning.logThisDay') }}
        </RouterLink>
      </template>
    </section>

    <p v-if="current.frozen" class="px-4 pt-3 text-sm text-ink-muted">{{ t('planning.frozen') }}</p>

    <EmptyState
      v-if="current.days.length === 0 && !selected"
      :title="t('planning.monthEmpty')"
      :body="t('planning.emptyBody')"
    />

    <section v-if="totals" class="px-4 pt-5" :aria-labelledby="`${titleId}-totals`">
      <h2 :id="`${titleId}-totals`" class="text-lg font-title">{{ t('planning.totalsTitle') }}</h2>
      <dl class="mt-2 grid grid-cols-2 border-t border-rule">
        <div class="border-b border-rule py-3 pr-3">
          <dt class="text-sm text-ink-muted">{{ t('planning.totals.sessions') }}</dt>
          <dd class="text-2xl font-title tabular-nums">{{ totals.sessionCount }}</dd>
        </div>
        <div class="border-b border-l border-rule py-3 pl-3">
          <dt class="text-sm text-ink-muted">{{ t('planning.totals.topped') }}</dt>
          <dd class="text-2xl font-title tabular-nums">{{ totals.toppedCount }}</dd>
        </div>
        <div class="border-b border-rule py-3 pr-3">
          <dt class="text-sm text-ink-muted">{{ t('planning.totals.maxGrade') }}</dt>
          <dd class="text-2xl font-title">
            <template v-if="totals.maxGrade">{{ totals.maxGrade }}</template>
            <template v-else>
              <span aria-hidden="true">{{ t('planning.totals.none') }}</span>
              <span class="sr-only">{{ t('planning.totals.noneLabel') }}</span>
            </template>
          </dd>
        </div>
        <div class="border-b border-l border-rule py-3 pl-3">
          <dt class="text-sm text-ink-muted">{{ t('planning.totals.feeling') }}</dt>
          <dd class="text-2xl font-title tabular-nums">
            <template v-if="totals.averageFeeling !== undefined">{{
              t('planning.totals.feelingValue', { n: formatFeeling(totals.averageFeeling) })
            }}</template>
            <template v-else>
              <span aria-hidden="true">{{ t('planning.totals.none') }}</span>
              <span class="sr-only">{{ t('planning.totals.noneLabel') }}</span>
            </template>
          </dd>
        </div>
      </dl>
    </section>

    <section v-if="streak" class="px-4 pt-5 pb-28" :aria-labelledby="`${titleId}-streak`">
      <h2 :id="`${titleId}-streak`" class="text-lg font-title">{{ t('planning.streakTitle') }}</h2>
      <p class="mt-1 flex items-baseline gap-2">
        <span v-if="streak.weeks > 0" class="text-3xl font-title text-brand-ink tabular-nums">{{
          streak.weeks
        }}</span>
        <span class="font-semibold">{{ t('planning.streakWeeks', streak.weeks) }}</span>
      </p>
      <p class="mt-1 text-sm text-ink-muted">
        {{
          t(
            'planning.streakThisWeek',
            { n: streak.currentWeekDays },
            Math.min(streak.currentWeekDays, 2),
          )
        }}
      </p>
    </section>
  </template>
</template>
