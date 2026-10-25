<script setup lang="ts">
import { computed, useId } from 'vue'
import { useI18n } from 'vue-i18n'

import { errorMessageKey } from '@/api/errors'
import { useMyLogbook, type LogbookRoute } from '@/api/sessions'
import SubPageHeader from '@/components/layout/SubPageHeader.vue'
import RouteSummaryLabel from '@/components/topo/RouteSummaryLabel.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import { formatDate } from '@/lib/format'

/**
 * My logbook ("carnet", spec §8.8): what my sessions add up to, the grade pyramid, and every route
 * I ever climbed, dismounted ones included (spec §2: history is sacred). Figures come computed.
 */
const { t } = useI18n()
const logbook = useMyLogbook()
const totalsId = useId()
const pyramidId = useId()
const routesId = useId()

const data = computed(() => logbook.data.value)
const widest = computed(() =>
  Math.max(1, ...(data.value?.grades ?? []).map((grade) => grade.lead + grade.topRope)),
)

const figures = computed(() => {
  const totals = data.value?.totals
  if (totals === undefined) return []
  return [
    { key: 'sessions', value: String(totals.sessionCount) },
    { key: 'routes', value: String(totals.routeCount) },
    { key: 'ascents', value: String(totals.ascentCount) },
    { key: 'topped', value: String(totals.toppedCount) },
    { key: 'lead', value: String(totals.leadToppedCount) },
    { key: 'clean', value: String(totals.cleanCount) },
    { key: 'maxGrade', value: totals.maxGrade },
    { key: 'maxLeadGrade', value: totals.maxLeadGrade },
  ] as const
})

function sheetLink(item: LogbookRoute) {
  return item.route.status === 'DISMOUNTED'
    ? `/topo/archives/voies/${item.route.id}`
    : `/topo/voies/${item.route.id}`
}

function bestClass(item: LogbookRoute) {
  if (item.best === 'TRIED') return 'text-ink-muted'
  return item.best.startsWith('LEAD') ? 'text-brand-ink' : 'text-success'
}
</script>

<template>
  <SubPageHeader back="/profil" :back-label="t('common.back')">
    {{ t('logbook.title') }}
  </SubPageHeader>

  <div v-if="logbook.isPending.value" class="flex flex-col gap-3 px-4 pt-4" aria-hidden="true">
    <p class="sr-only" role="status">{{ t('logbook.loading') }}</p>
    <SkeletonBlock class="h-32 w-full" />
    <SkeletonBlock class="h-40 w-full" />
  </div>
  <div v-else-if="logbook.isError.value" class="flex flex-col items-start gap-3 px-4 pt-4">
    <FormAlert>{{ t(errorMessageKey(logbook.error.value)) }}</FormAlert>
    <GgButton variant="secondary" @click="logbook.refetch()">{{ t('common.retry') }}</GgButton>
  </div>

  <EmptyState
    v-else-if="data && data.routes.length === 0"
    :title="t('logbook.emptyTitle')"
    :body="t('logbook.emptyBody')"
  />

  <template v-else-if="data">
    <p class="max-w-prose px-4 pt-2 text-ink-muted">{{ t('logbook.intro') }}</p>

    <section class="px-4 pt-5" :aria-labelledby="totalsId">
      <h2 :id="totalsId" class="text-lg font-title">{{ t('logbook.totalsTitle') }}</h2>
      <dl class="mt-2 grid grid-cols-2 border-t border-rule sm:grid-cols-4">
        <div
          v-for="figure in figures"
          :key="figure.key"
          class="border-b border-rule py-3 pr-3 even:border-l even:pl-3 sm:border-l sm:pl-3 sm:[&:nth-child(4n+1)]:border-l-0 sm:[&:nth-child(4n+1)]:pl-0"
        >
          <dt class="text-sm text-ink-muted">{{ t(`logbook.totals.${figure.key}`) }}</dt>
          <dd class="text-2xl font-title tabular-nums">
            {{ figure.value ?? t('logbook.totals.none') }}
          </dd>
        </div>
      </dl>
    </section>

    <section v-if="data.grades.length > 0" class="px-4 pt-6" :aria-labelledby="pyramidId">
      <h2 :id="pyramidId" class="text-lg font-title">{{ t('logbook.pyramidTitle') }}</h2>
      <p class="text-sm text-ink-muted">{{ t('logbook.pyramidHint') }}</p>
      <p class="mt-2 flex gap-4 text-sm font-semibold" aria-hidden="true">
        <span class="inline-flex items-center gap-1.5">
          <span class="size-3 rounded-[2px] bg-brand" />{{ t('logbook.legendLead') }}
        </span>
        <span class="inline-flex items-center gap-1.5">
          <span class="size-3 rounded-[2px] bg-olive" />{{ t('logbook.legendTopRope') }}
        </span>
      </p>
      <ul class="mt-3 flex flex-col gap-1.5">
        <li v-for="grade in data.grades" :key="grade.gradeRank" class="flex items-center gap-2">
          <span class="sr-only">{{
            t('logbook.pyramidRow', {
              grade: grade.grade,
              lead: grade.lead,
              topRope: grade.topRope,
            })
          }}</span>
          <span class="w-10 shrink-0 text-right font-semibold tabular-nums" aria-hidden="true">{{
            grade.grade
          }}</span>
          <span class="flex h-6 min-w-0 flex-1" aria-hidden="true">
            <span
              v-if="grade.lead > 0"
              class="h-full rounded-l-[2px] bg-brand"
              :class="{ 'rounded-r-[2px]': grade.topRope === 0 }"
              :style="{ width: `${(grade.lead / widest) * 100}%` }"
            />
            <span
              v-if="grade.topRope > 0"
              class="h-full rounded-r-[2px] bg-olive"
              :class="{ 'rounded-l-[2px]': grade.lead === 0 }"
              :style="{ width: `${(grade.topRope / widest) * 100}%` }"
            />
          </span>
          <span class="w-8 shrink-0 text-sm text-ink-muted tabular-nums" aria-hidden="true">{{
            grade.lead + grade.topRope
          }}</span>
        </li>
      </ul>
    </section>

    <section class="pt-6 pb-10" :aria-labelledby="routesId">
      <h2 :id="routesId" class="flex items-baseline justify-between gap-3 px-4 text-lg font-title">
        {{ t('logbook.routesTitle') }}
        <span class="font-sans text-sm font-normal text-ink-muted">{{
          t('logbook.routeCount', data.routes.length)
        }}</span>
      </h2>
      <ul class="mt-2 border-t border-rule">
        <li v-for="item in data.routes" :key="item.route.id" class="border-b border-rule">
          <RouterLink
            :to="sheetLink(item)"
            class="flex flex-col items-start gap-1 px-4 py-3 hover:bg-surface-sunken/60"
          >
            <RouteSummaryLabel :route="item.route" size="sm" />
            <span class="dot-list text-sm">
              <span class="font-semibold" :class="bestClass(item)">{{
                t(`logbook.best.${item.best}`)
              }}</span>
              <span class="text-ink-muted">{{ t('logbook.passages', item.ascentCount) }}</span>
              <span class="text-ink-muted">{{
                t('logbook.lastTime', { date: formatDate(item.lastClimbedOn) })
              }}</span>
            </span>
          </RouterLink>
        </li>
      </ul>
    </section>
  </template>
</template>
