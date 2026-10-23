<script setup lang="ts">
import { Check, Heart, MessageCircle } from 'lucide-vue-next'
import { computed, useId } from 'vue'
import { useI18n } from 'vue-i18n'
import type { RouteLocationRaw } from 'vue-router'

import type { RouteSummary } from '@/api/topo'
import { formatDate } from '@/lib/format'

import RouteSummaryLabel from './RouteSummaryLabel.vue'
import RouteThumbnail from './RouteThumbnail.vue'

/**
 * One line of the topo (docs/DESIGN.md §4, wireframe « Topo »): full width, between rules, no
 * card. The whole line opens the route sheet; its name is the label's, the rest describes it.
 */
const props = defineProps<{
  route: RouteSummary
  to: RouteLocationRaw
  /** The list is already grouped under the sector's heading: do not repeat it on each row. */
  hideSector?: boolean
}>()

const { t } = useI18n()
const labelId = useId()
const metaId = useId()

const styles = computed(() =>
  props.route.styles.map((style) => t(`routeStyles.${style}`)).join(' '),
)
const done = computed(() =>
  (['TOP_ROPE', 'LEAD'] as const).filter((mode) => props.route.checks.includes(mode)),
)
</script>

<template>
  <RouterLink
    :to="to"
    :aria-labelledby="labelId"
    :aria-describedby="metaId"
    class="flex gap-3 px-4 pt-4 pb-3 outline-offset-[-3px] transition-colors duration-150 hover:bg-surface-sunken/60"
  >
    <RouteThumbnail :route="route" />
    <div class="flex min-w-0 flex-1 flex-col items-start gap-1.5">
      <RouteSummaryLabel :route="route" :label-id="labelId" />
      <p :id="metaId" class="dot-list gap-y-0.5 text-sm text-ink-muted">
        <span v-if="!hideSector">{{ route.sectorName }}</span>
        <span v-if="styles">{{ styles }}</span>
        <span v-if="route.favoriteCount > 0" class="inline-flex items-center gap-0.5">
          <Heart :size="14" aria-hidden="true" />
          <span aria-hidden="true">{{ route.favoriteCount }}</span>
          <span class="sr-only">{{ t('topo.row.favorites', route.favoriteCount) }}</span>
        </span>
        <span v-if="route.commentCount > 0" class="inline-flex items-center gap-0.5">
          <MessageCircle :size="14" aria-hidden="true" />
          <span aria-hidden="true">{{ route.commentCount }}</span>
          <span class="sr-only">{{ t('topo.row.comments', route.commentCount) }}</span>
        </span>
        <span
          v-for="mode in done"
          :key="mode"
          class="inline-flex items-center gap-0.5 font-semibold text-success"
        >
          <Check :size="14" :stroke-width="3" aria-hidden="true" />
          <span aria-hidden="true">{{
            t(mode === 'LEAD' ? 'topo.row.doneLead' : 'topo.row.doneTopRope')
          }}</span>
          <span class="sr-only">{{
            t(mode === 'LEAD' ? 'topo.row.doneLeadLong' : 'topo.row.doneTopRopeLong')
          }}</span>
        </span>
        <span
          v-if="route.status === 'LAST_CHANCE' && route.plannedDismountOn"
          class="w-full font-semibold text-brand-ink"
        >
          {{ t('topo.row.plannedDismount', { date: formatDate(route.plannedDismountOn) }) }}
        </span>
        <span v-if="route.dismountedAt" class="w-full">
          {{ t('topo.row.dismountedOn', { date: formatDate(route.dismountedAt) }) }}
        </span>
      </p>
    </div>
  </RouterLink>
</template>
