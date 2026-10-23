<script setup lang="ts">
import { useId } from 'vue'
import { useI18n } from 'vue-i18n'

import type { RouteDetail } from '@/api/topo'
import { formatDate } from '@/lib/format'
import { ascentOutcome } from '@/lib/sessionView'

/**
 * The ascents of a route (spec §8.4): figures by everyone, then mine, newest first, each one
 * leading to its session.
 */
defineProps<{ stats: RouteDetail['stats']; mine: RouteDetail['myAscents'] }>()

const { t } = useI18n()
const statsId = useId()
const mineId = useId()
</script>

<template>
  <section class="mt-8 border-t border-rule pt-6" :aria-labelledby="statsId">
    <h3 :id="statsId" class="text-lg font-title">{{ t('routeSheet.statsTitle') }}</h3>
    <p v-if="stats.ascentCount === 0" class="mt-2 text-ink-muted">{{ t('routeSheet.noAscent') }}</p>
    <dl v-else class="mt-3 grid grid-cols-2 gap-3 sm:grid-cols-4">
      <div class="flex flex-col">
        <dt class="text-sm text-ink-muted">{{ t('routeSheet.stats.ascents') }}</dt>
        <dd class="text-2xl font-bold tabular-nums">{{ stats.ascentCount }}</dd>
      </div>
      <div class="flex flex-col">
        <dt class="text-sm text-ink-muted">{{ t('routeSheet.stats.topped') }}</dt>
        <dd class="text-2xl font-bold tabular-nums">{{ stats.toppedCount }}</dd>
      </div>
      <div class="flex flex-col">
        <dt class="text-sm text-ink-muted">{{ t('routeSheet.stats.climbers') }}</dt>
        <dd class="text-2xl font-bold tabular-nums">{{ stats.climberCount }}</dd>
      </div>
      <div class="flex flex-col">
        <dt class="text-sm text-ink-muted">{{ t('routeSheet.stats.lead') }}</dt>
        <dd class="text-2xl font-bold tabular-nums">
          {{ t('routeSheet.stats.percent', { n: stats.leadPercent ?? 0 }) }}
        </dd>
      </div>
    </dl>
  </section>

  <section class="mt-6" :aria-labelledby="mineId">
    <h3 :id="mineId" class="text-lg font-title">{{ t('routeSheet.mineTitle') }}</h3>
    <p v-if="mine.length === 0" class="mt-2 text-ink-muted">{{ t('routeSheet.mineEmpty') }}</p>
    <ul v-else class="mt-2 divide-y divide-rule border-y border-rule">
      <li v-for="(ascent, index) in mine" :key="`${ascent.sessionId}-${index}`">
        <RouterLink
          :to="`/seances/${ascent.sessionId}`"
          class="flex min-h-12 flex-wrap items-center gap-x-3 gap-y-1 px-1 py-2 hover:bg-surface-sunken"
        >
          <span class="font-semibold">{{ formatDate(ascent.climbedOn) }}</span>
          <span class="text-ink-muted">
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
        </RouterLink>
      </li>
    </ul>
  </section>
</template>
