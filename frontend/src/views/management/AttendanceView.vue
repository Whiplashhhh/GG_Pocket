<script setup lang="ts">
import { ChevronLeft, ChevronRight, Download } from 'lucide-vue-next'
import { computed, ref, useId } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

import { errorMessageKey } from '@/api/errors'
import { useRankingMonths } from '@/api/rankings'
import { useMonthAttendance } from '@/api/stats'
import RouteSummaryLabel from '@/components/topo/RouteSummaryLabel.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SelectField from '@/components/ui/SelectField.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import { clubToday } from '@/lib/age'
import { saveFile } from '@/lib/download'
import {
  canShowNext,
  canShowPrevious,
  formatMonth,
  parseMonthQuery,
  shiftMonth,
  type Month,
} from '@/lib/planning'
import {
  ATTENDANCE_SORTS,
  attendanceCsv,
  attendanceFileName,
  isAttendanceSort,
  sortAttendance,
  type AttendanceSort,
  type CsvHeaders,
} from '@/lib/stats'

import ManagementTitle from './ManagementTitle.vue'

/**
 * Spec §8.10: route by route, the ascents of a month (every attempt), how many climbers, how many
 * topped; sorted as the setter likes, and exported for the club's spreadsheets.
 */
const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const today = clubToday()
const monthTitleId = useId()

const month = computed(() => parseMonthQuery(route.query, today))
const sort = computed<AttendanceSort>(() =>
  isAttendanceSort(route.query.tri) ? route.query.tri : 'ascents',
)
const attendance = useMonthAttendance(month)
// The podium's months: from the first climbed one to this one.
const months = useRankingMonths()
const notice = ref<string | null>(null)

function show(next: { month?: Month; sort?: AttendanceSort }) {
  const shownMonth = next.month ?? month.value
  const shownSort = next.sort ?? sort.value
  notice.value = null
  void router.replace({
    path: '/gestion/frequentation',
    query: {
      ...(shownMonth === today.slice(0, 7) ? {} : { mois: shownMonth }),
      ...(shownSort === 'ascents' ? {} : { tri: shownSort }),
    },
  })
}

const sortModel = computed<string>({
  get: () => sort.value,
  set: (value) => {
    if (isAttendanceSort(value)) show({ sort: value })
  },
})

const firstMonth = computed(() => months.data.value?.at(-1)?.month)
const previousAllowed = computed(
  () =>
    canShowPrevious(month.value) &&
    (firstMonth.value === undefined || month.value > firstMonth.value),
)

/** While another month loads, the previous one stays: show nothing of it as if it were this one. */
const current = computed(() =>
  attendance.data.value?.month === month.value ? attendance.data.value : undefined,
)
const lines = computed(() => sortAttendance(current.value?.routes ?? [], sort.value))

function exportCsv() {
  if (!current.value) return
  const headers: CsvHeaders = [
    t('attendance.csv.cord'),
    t('attendance.csv.sector'),
    t('attendance.csv.route'),
    t('attendance.csv.grade'),
    t('attendance.csv.holds'),
    t('attendance.csv.opened'),
    t('attendance.csv.ascents'),
    t('attendance.csv.topped'),
    t('attendance.csv.clean'),
    t('attendance.csv.climbers'),
    t('attendance.csv.success'),
  ]
  const name = attendanceFileName(current.value)
  saveFile(
    new Blob([attendanceCsv(lines.value, headers)], { type: 'text/csv;charset=utf-8' }),
    name,
  )
  notice.value = t('attendance.exported', { name })
}

const percent = (value: number | undefined) =>
  value === undefined ? '–' : `${String(value)}\u00a0%`
</script>

<template>
  <ManagementTitle>{{ t('attendance.title') }}</ManagementTitle>
  <p class="max-w-prose px-4 pt-1 pb-4 text-ink-muted lg:px-0">{{ t('attendance.intro') }}</p>

  <section class="px-4 pb-4 lg:px-0" :aria-labelledby="monthTitleId">
    <div class="flex items-center justify-between gap-2 lg:max-w-md">
      <button
        type="button"
        class="inline-flex size-11 shrink-0 items-center justify-center rounded-control border-2 border-rule text-ink hover:border-ink disabled:opacity-40"
        :aria-label="t('attendance.previous')"
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
        :aria-label="t('attendance.next')"
        :disabled="!canShowNext(month, today)"
        @click="show({ month: shiftMonth(month, 1) })"
      >
        <ChevronRight :size="24" aria-hidden="true" />
      </button>
    </div>
  </section>

  <div
    v-if="attendance.isPending.value"
    class="flex flex-col gap-3 px-4 lg:px-0"
    aria-hidden="true"
  >
    <p class="sr-only" role="status">{{ t('attendance.loading') }}</p>
    <SkeletonBlock class="h-20 w-full" />
    <SkeletonBlock v-for="index in 4" :key="index" class="h-14 w-full" />
  </div>
  <div v-else-if="attendance.isError.value" class="flex flex-col items-start gap-3 px-4 lg:px-0">
    <FormAlert>{{ t(errorMessageKey(attendance.error.value)) }}</FormAlert>
    <GgButton variant="secondary" @click="attendance.refetch()">{{ t('common.retry') }}</GgButton>
  </div>

  <template v-else-if="current">
    <dl
      class="mx-4 grid grid-cols-3 border-y-2 border-ink lg:mx-0 lg:max-w-2xl"
      :aria-label="t('attendance.totalsLabel')"
    >
      <div class="flex flex-col-reverse px-2 py-3">
        <dt class="text-sm text-ink-muted">
          {{ t('attendance.totals.ascents', current.total.ascentCount) }}
        </dt>
        <dd class="text-2xl font-title whitespace-nowrap tabular-nums sm:text-3xl">
          {{ current.total.ascentCount }}
        </dd>
      </div>
      <div class="flex flex-col-reverse border-l border-rule px-2 py-3">
        <dt class="text-sm text-ink-muted">
          {{ t('attendance.totals.climbers', current.total.climberCount) }}
        </dt>
        <dd class="text-2xl font-title whitespace-nowrap tabular-nums sm:text-3xl">
          {{ current.total.climberCount }}
        </dd>
      </div>
      <div class="flex flex-col-reverse border-l border-rule px-2 py-3">
        <dt class="text-sm text-ink-muted">
          {{
            current.total.successPercent === undefined
              ? t('attendance.totals.noSuccess')
              : t('attendance.totals.success')
          }}
        </dt>
        <dd class="text-2xl font-title whitespace-nowrap tabular-nums sm:text-3xl">
          {{ percent(current.total.successPercent) }}
        </dd>
      </div>
    </dl>
    <p class="px-4 pt-2 pb-4 text-sm text-ink-muted lg:px-0">{{ t('attendance.successHelp') }}</p>

    <EmptyState
      v-if="lines.length === 0"
      :title="t('attendance.emptyTitle')"
      :body="t('attendance.emptyBody')"
    />

    <template v-else>
      <div class="flex flex-col gap-3 px-4 pb-4 lg:flex-row lg:items-end lg:px-0">
        <div class="lg:w-72">
          <SelectField v-model="sortModel" :label="t('attendance.sort')">
            <option v-for="option in ATTENDANCE_SORTS" :key="option" :value="option">
              {{ t(`attendance.sorts.${option}`) }}
            </option>
          </SelectField>
        </div>
        <GgButton variant="secondary" class="self-start lg:ml-auto lg:self-end" @click="exportCsv">
          <Download :size="20" aria-hidden="true" />
          {{ t('attendance.export') }}
        </GgButton>
      </div>
      <div class="px-4 pb-4 lg:px-0" aria-live="polite">
        <FormAlert v-if="notice" tone="success">{{ notice }}</FormAlert>
      </div>

      <!-- Phones: one line per route. -->
      <ul class="border-t border-rule lg:hidden">
        <li
          v-for="item in lines"
          :key="item.route.id"
          class="flex flex-col items-start gap-1.5 border-b border-rule px-4 py-3"
        >
          <RouteSummaryLabel :route="item.route" />
          <p class="dot-list text-sm text-ink-muted">
            <span class="font-semibold text-ink">{{
              t('attendance.line.ascents', item.attendance.ascentCount)
            }}</span>
            <span>{{ t('attendance.line.climbers', item.attendance.climberCount) }}</span>
            <span v-if="item.attendance.successPercent !== undefined">{{
              t('attendance.line.success', { n: item.attendance.successPercent })
            }}</span>
            <span v-if="item.route.status === 'DISMOUNTED'">{{ t('attendance.dismounted') }}</span>
          </p>
        </li>
      </ul>

      <!-- Large screens: a table. -->
      <div class="hidden overflow-x-auto lg:block">
        <table class="w-full border-collapse text-left">
          <thead>
            <tr class="border-b-2 border-ink">
              <th scope="col" class="py-2 pr-3 font-semibold">
                {{ t('attendance.columns.route') }}
              </th>
              <th scope="col" class="py-2 pr-3 font-semibold">
                {{ t('attendance.columns.sector') }}
              </th>
              <th
                v-for="column in ['ascents', 'topped', 'clean', 'climbers', 'success'] as const"
                :key="column"
                scope="col"
                class="py-2 pr-3 text-right font-semibold"
                :aria-sort="column === sort ? 'descending' : undefined"
              >
                {{ t(`attendance.columns.${column}`) }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="item in lines"
              :key="item.route.id"
              class="border-b border-rule hover:bg-surface-sunken"
            >
              <th scope="row" class="max-w-80 py-2 pr-3 font-normal">
                <RouteSummaryLabel :route="item.route" />
              </th>
              <td class="min-w-28 py-2 pr-3">
                {{ item.route.sectorName }}
                <span
                  v-if="item.route.status === 'DISMOUNTED'"
                  class="block text-sm text-ink-muted"
                >
                  {{ t('attendance.dismounted') }}
                </span>
              </td>
              <td class="py-2 pr-3 text-right font-semibold tabular-nums">
                {{ item.attendance.ascentCount }}
              </td>
              <td class="py-2 pr-3 text-right tabular-nums">{{ item.attendance.toppedCount }}</td>
              <td class="py-2 pr-3 text-right tabular-nums">{{ item.attendance.cleanCount }}</td>
              <td class="py-2 pr-3 text-right tabular-nums">{{ item.attendance.climberCount }}</td>
              <td class="py-2 pr-3 text-right tabular-nums">
                {{ percent(item.attendance.successPercent) }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </template>
</template>
