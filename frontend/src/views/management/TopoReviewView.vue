<script setup lang="ts">
import { computed, ref, useId } from 'vue'
import { useI18n } from 'vue-i18n'

import { errorMessageKey } from '@/api/errors'
import { useRouteAttendance } from '@/api/stats'
import { useTopoConfig } from '@/api/topo'
import RouteSummaryLabel from '@/components/topo/RouteSummaryLabel.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import HoldSwatch from '@/components/ui/HoldSwatch.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import { clubToday } from '@/lib/age'
import {
  cordGrid,
  gradeDistribution,
  leastClimbed,
  mostClimbed,
  mostLoved,
  oldestFirst,
  sectorDistribution,
  wallAge,
} from '@/lib/stats'

import ManagementTitle from './ManagementTitle.vue'

/**
 * Spec §8.10: the wall today. What grades and sectors lack, what each cord holds, which routes
 * draw the crowds and which gather dust, the oldest ones (what to dismount next) and the loved
 * ones.
 */
const RANKED = 5
const OLDEST_SHOWN = 8

const { t } = useI18n()
const today = clubToday()
const lines = useRouteAttendance()
const config = useTopoConfig()
const ids = {
  grades: useId(),
  sectors: useId(),
  cords: useId(),
  most: useId(),
  least: useId(),
  oldest: useId(),
  loved: useId(),
}
const allOldest = ref(false)

const isPending = computed(() => lines.isPending.value || config.isPending.value)
const failure = computed(() => lines.error.value ?? config.error.value)
const routes = computed(() => (lines.data.value ?? []).map((line) => line.route))
const lastChanceCount = computed(
  () => routes.value.filter((route) => route.status === 'LAST_CHANCE').length,
)
const grades = computed(() => gradeDistribution(routes.value, config.data.value?.grades ?? []))
const sectors = computed(() => sectorDistribution(routes.value, config.data.value?.sectors ?? []))
const cords = computed(() => cordGrid(routes.value, config.data.value?.sectors ?? []))
const most = computed(() => mostClimbed(lines.data.value ?? [], RANKED))
const least = computed(() => leastClimbed(lines.data.value ?? [], RANKED))
const oldest = computed(() => oldestFirst(routes.value))
const oldestShown = computed(() =>
  allOldest.value ? oldest.value : oldest.value.slice(0, OLDEST_SHOWN),
)
const loved = computed(() => mostLoved(routes.value, RANKED))

function age(openedOn: string): string {
  const { unit, value } = wallAge(openedOn, today)
  return t(`topoReview.age.${unit}`, { n: value }, value)
}

function retry() {
  if (lines.isError.value) void lines.refetch()
  if (config.isError.value) void config.refetch()
}
</script>

<template>
  <ManagementTitle>{{ t('topoReview.title') }}</ManagementTitle>
  <p class="max-w-prose px-4 pt-1 pb-4 text-ink-muted lg:px-0">{{ t('topoReview.intro') }}</p>

  <div v-if="isPending" class="flex flex-col gap-3 px-4 lg:px-0" aria-hidden="true">
    <p class="sr-only" role="status">{{ t('topoReview.loading') }}</p>
    <SkeletonBlock class="h-16 w-full" />
    <SkeletonBlock class="h-48 w-full" />
  </div>
  <div v-else-if="failure" class="flex flex-col items-start gap-3 px-4 lg:px-0">
    <FormAlert>{{ t(errorMessageKey(failure)) }}</FormAlert>
    <GgButton variant="secondary" @click="retry">{{ t('common.retry') }}</GgButton>
  </div>
  <EmptyState
    v-else-if="routes.length === 0"
    :title="t('topoReview.emptyTitle')"
    :body="t('topoReview.emptyBody')"
  />

  <div v-else class="flex flex-col gap-8 pb-8">
    <p class="mx-4 border-y-2 border-ink py-3 lg:mx-0 lg:max-w-2xl">
      <span class="text-3xl font-title">{{ t('topoReview.total', routes.length) }}</span>
      <span class="block text-sm text-ink-muted">{{
        t('topoReview.lastChance', lastChanceCount)
      }}</span>
    </p>

    <div class="grid grid-cols-1 gap-8 px-4 lg:grid-cols-2 lg:px-0">
      <section :aria-labelledby="ids.grades">
        <h2 :id="ids.grades" class="mb-3 text-lg font-title">{{ t('topoReview.grades') }}</h2>
        <ul class="flex flex-col gap-1">
          <li v-for="bar in grades" :key="bar.item" class="flex items-center gap-3">
            <span class="w-10 shrink-0 font-semibold tabular-nums">{{ bar.item }}</span>
            <span class="h-4 flex-1" aria-hidden="true">
              <span
                v-if="bar.count > 0"
                class="block h-full rounded-r bg-brand"
                :style="{ width: `${String(bar.percent)}%` }"
              />
            </span>
            <span
              class="w-20 shrink-0 text-right text-sm tabular-nums"
              :class="{ 'text-ink-muted': bar.count === 0 }"
              >{{ t('topoReview.routes', bar.count) }}</span
            >
          </li>
        </ul>
      </section>

      <section :aria-labelledby="ids.sectors">
        <h2 :id="ids.sectors" class="mb-3 text-lg font-title">{{ t('topoReview.sectors') }}</h2>
        <ul class="flex flex-col gap-2">
          <li v-for="bar in sectors" :key="bar.item.id" class="flex flex-col gap-1">
            <span class="flex justify-between gap-3">
              <span class="font-semibold">{{ bar.item.name }}</span>
              <span class="shrink-0 text-sm tabular-nums">{{
                t('topoReview.routes', bar.count)
              }}</span>
            </span>
            <span class="h-3" aria-hidden="true">
              <span
                v-if="bar.count > 0"
                class="block h-full rounded-r bg-brand"
                :style="{ width: `${String(bar.percent)}%` }"
              />
            </span>
          </li>
        </ul>
      </section>
    </div>

    <section class="px-4 lg:px-0" :aria-labelledby="ids.cords">
      <h2 :id="ids.cords" class="mb-3 text-lg font-title">{{ t('topoReview.cords') }}</h2>
      <ol class="grid grid-cols-2 gap-2 sm:grid-cols-3 lg:grid-cols-5">
        <li
          v-for="cell in cords"
          :key="cell.number"
          class="flex flex-col gap-1.5 rounded-control border-2 p-2"
          :class="cell.routes.length === 0 ? 'border-dashed border-rule' : 'border-rule'"
        >
          <span class="flex min-w-0 flex-col">
            <span class="font-title whitespace-nowrap">{{
              t('topoReview.cord', { n: cell.number })
            }}</span>
            <span class="truncate text-xs text-ink-muted">{{ cell.sectorName }}</span>
          </span>
          <ul v-if="cell.routes.length > 0" class="flex flex-wrap gap-x-3 gap-y-1">
            <li
              v-for="route in cell.routes"
              :key="route.id"
              class="inline-flex items-center gap-1 text-sm font-semibold"
              :title="route.name"
            >
              <HoldSwatch v-for="hold in route.holds" :key="hold.id" :color="hold" :size="14" />
              {{ route.grade }}
              <span class="sr-only">{{ route.name }}</span>
            </li>
          </ul>
          <span v-else class="text-sm text-ink-muted">{{ t('topoReview.freeCord') }}</span>
        </li>
      </ol>
    </section>

    <div class="grid grid-cols-1 gap-8 px-4 lg:grid-cols-2 lg:px-0">
      <section :aria-labelledby="ids.most">
        <h2 :id="ids.most" class="text-lg font-title">{{ t('topoReview.mostClimbed') }}</h2>
        <p class="pb-2 text-sm text-ink-muted">{{ t('topoReview.climbedHint') }}</p>
        <ol v-if="most.length > 0" class="border-t border-rule">
          <li
            v-for="item in most"
            :key="item.route.id"
            class="flex flex-col items-start gap-1 border-b border-rule py-2 sm:flex-row sm:items-center sm:justify-between sm:gap-3"
          >
            <span class="max-w-full min-w-0 sm:flex-1"
              ><RouteSummaryLabel :route="item.route" size="sm"
            /></span>
            <span class="shrink-0 text-sm font-semibold tabular-nums">{{
              t('attendance.line.ascents', item.attendance.ascentCount)
            }}</span>
          </li>
        </ol>
        <p v-else class="text-ink-muted">{{ t('topoReview.nobodyClimbed') }}</p>
      </section>

      <section :aria-labelledby="ids.least">
        <h2 :id="ids.least" class="text-lg font-title">{{ t('topoReview.leastClimbed') }}</h2>
        <p class="pb-2 text-sm text-ink-muted">{{ t('topoReview.climbedHint') }}</p>
        <ol class="border-t border-rule">
          <li
            v-for="item in least"
            :key="item.route.id"
            class="flex flex-col items-start gap-1 border-b border-rule py-2 sm:flex-row sm:items-center sm:justify-between sm:gap-3"
          >
            <span class="max-w-full min-w-0 sm:flex-1"
              ><RouteSummaryLabel :route="item.route" size="sm"
            /></span>
            <span class="shrink-0 text-sm tabular-nums sm:text-right">
              <span class="block font-semibold">{{
                t('attendance.line.ascents', item.attendance.ascentCount)
              }}</span>
              <span class="block text-ink-muted">{{ age(item.route.openedOn) }}</span>
            </span>
          </li>
        </ol>
      </section>

      <section :aria-labelledby="ids.oldest">
        <h2 :id="ids.oldest" class="text-lg font-title">{{ t('topoReview.oldest') }}</h2>
        <p class="pb-2 text-sm text-ink-muted">{{ t('topoReview.oldestHint') }}</p>
        <ol class="border-t border-rule">
          <li
            v-for="route in oldestShown"
            :key="route.id"
            class="flex flex-col items-start gap-1 border-b border-rule py-2 sm:flex-row sm:items-center sm:justify-between sm:gap-3"
          >
            <span class="max-w-full min-w-0 sm:flex-1"
              ><RouteSummaryLabel :route="route" size="sm"
            /></span>
            <span class="shrink-0 text-sm text-ink-muted">{{ age(route.openedOn) }}</span>
          </li>
        </ol>
        <GgButton
          v-if="oldest.length > OLDEST_SHOWN"
          variant="secondary"
          class="mt-3"
          :aria-expanded="allOldest"
          @click="allOldest = !allOldest"
        >
          {{ allOldest ? t('topoReview.showLess') : t('topoReview.showAll', { n: oldest.length }) }}
        </GgButton>
      </section>

      <section :aria-labelledby="ids.loved">
        <h2 :id="ids.loved" class="pb-2 text-lg font-title">{{ t('topoReview.loved') }}</h2>
        <ol v-if="loved.length > 0" class="border-t border-rule">
          <li
            v-for="route in loved"
            :key="route.id"
            class="flex flex-col items-start gap-1 border-b border-rule py-2 sm:flex-row sm:items-center sm:justify-between sm:gap-3"
          >
            <span class="max-w-full min-w-0 sm:flex-1"
              ><RouteSummaryLabel :route="route" size="sm"
            /></span>
            <span class="shrink-0 text-sm font-semibold tabular-nums">{{
              t('topo.row.favorites', route.favoriteCount)
            }}</span>
          </li>
        </ol>
        <p v-else class="text-ink-muted">{{ t('topoReview.lovedEmpty') }}</p>
      </section>
    </div>
  </div>
</template>
