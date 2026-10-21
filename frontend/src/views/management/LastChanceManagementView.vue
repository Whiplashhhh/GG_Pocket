<script setup lang="ts">
import { Archive, TimerOff } from 'lucide-vue-next'
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { errorMessageKey } from '@/api/errors'
import type { RouteSummary } from '@/api/topo'
import { useCancelLastChance, useLastChanceRoutes } from '@/api/topoAdmin'
import RouteStatusDialogs from '@/components/topo/RouteStatusDialogs.vue'
import RouteSummaryLabel from '@/components/topo/RouteSummaryLabel.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import { formatDate } from '@/lib/format'
import type { RouteStatusAction } from '@/lib/route'

import ManagementTitle from './ManagementTitle.vue'

/**
 * Spec §8.10: the routes in last chance, with how many climbers have seen the news (« accepté
 * son destin » in the pop-up). Cancel or dismount from here; announcing starts from the routes.
 */
const { t } = useI18n()
const routes = useLastChanceRoutes()
const cancelLastChance = useCancelLastChance()
const statusAction = ref<RouteStatusAction | null>(null)
const notice = ref<string | null>(null)
const failure = ref<unknown>(null)

function say(message: string) {
  failure.value = null
  notice.value = message
}
function fail(error: unknown) {
  notice.value = null
  failure.value = error
}

function cancel(route: RouteSummary) {
  cancelLastChance.mutate(route.id, {
    onSuccess: () => {
      say(t('routeStatus.cancelled', { name: route.name }))
    },
    onError: fail,
  })
}
</script>

<template>
  <ManagementTitle>{{ t('lastChanceAdmin.title') }}</ManagementTitle>
  <p class="max-w-prose px-4 pt-1 pb-4 text-ink-muted lg:px-0">
    {{ t('lastChanceAdmin.intro') }}
    <RouterLink to="/gestion/voies" class="font-semibold underline underline-offset-4">{{
      t('lastChanceAdmin.toRoutes')
    }}</RouterLink>
  </p>
  <div class="flex flex-col gap-3 px-4 pb-4 lg:px-0" aria-live="polite">
    <FormAlert v-if="notice" tone="success">{{ notice }}</FormAlert>
    <FormAlert v-if="failure">{{ t(errorMessageKey(failure)) }}</FormAlert>
  </div>

  <div v-if="routes.isPending.value" class="flex flex-col gap-3 px-4 lg:px-0" aria-hidden="true">
    <SkeletonBlock v-for="index in 3" :key="index" class="h-24 w-full" />
  </div>
  <div v-else-if="routes.isError.value" class="flex flex-col items-start gap-3 px-4 lg:px-0">
    <FormAlert>{{ t(errorMessageKey(routes.error.value)) }}</FormAlert>
    <GgButton variant="secondary" @click="routes.refetch()">{{ t('common.retry') }}</GgButton>
  </div>
  <EmptyState
    v-else-if="(routes.data.value ?? []).length === 0"
    :title="t('lastChanceAdmin.emptyTitle')"
    :body="t('lastChanceAdmin.emptyBody')"
  />

  <ul v-else class="border-t border-rule">
    <li
      v-for="item in routes.data.value"
      :key="item.route.id"
      class="flex flex-col gap-3 border-b border-rule px-4 py-4 lg:flex-row lg:items-center lg:gap-6 lg:px-0"
    >
      <div class="flex min-w-0 flex-1 flex-col items-start gap-1.5">
        <RouteSummaryLabel :route="item.route" />
        <p class="text-sm text-ink-muted">
          {{ item.route.sectorName }} ·
          <span class="font-semibold text-brand-ink">{{
            item.route.plannedDismountOn
              ? t('routesAdmin.plannedOn', { date: formatDate(item.route.plannedDismountOn) })
              : t('routesAdmin.noPlannedDate')
          }}</span>
        </p>
        <p class="font-semibold">
          {{ t('lastChanceAdmin.acknowledged', item.acknowledgedCount) }}
          <span class="font-normal text-ink-muted">
            · {{ t('topo.row.favorites', item.route.favoriteCount) }}</span
          >
        </p>
      </div>
      <div class="flex flex-wrap gap-2">
        <GgButton
          variant="secondary"
          :aria-label="t('routesAdmin.cancelLastChanceNamed', { name: item.route.name })"
          :disabled="cancelLastChance.isPending.value"
          @click="cancel(item.route)"
        >
          <TimerOff :size="20" aria-hidden="true" />
          {{ t('routesAdmin.cancelLastChance') }}
        </GgButton>
        <GgButton
          variant="danger"
          :aria-label="t('routesAdmin.dismountNamed', { name: item.route.name })"
          @click="statusAction = { kind: 'dismount', route: item.route }"
        >
          <Archive :size="20" aria-hidden="true" />
          {{ t('routesAdmin.dismount') }}
        </GgButton>
      </div>
    </li>
  </ul>

  <RouteStatusDialogs v-model:action="statusAction" @done="say" @failed="fail" />
</template>
