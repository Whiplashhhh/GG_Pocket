<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import { errorMessageKey } from '@/api/errors'
import { useDismountedRoutes, type RouteSummary } from '@/api/topo'
import SubPageHeader from '@/components/layout/SubPageHeader.vue'
import RouteRow from '@/components/topo/RouteRow.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'

/** Spec §8.4: dismounted routes, most recently dismounted first, read-only. */
const { t } = useI18n()
const routes = useDismountedRoutes()

function sheetLink(route: RouteSummary) {
  return `/topo/archives/voies/${route.id}`
}
</script>

<template>
  <SubPageHeader back="/topo" :back-label="t('archives.back')">
    {{ t('archives.title') }}
  </SubPageHeader>
  <p class="px-4 pt-2 pb-4 text-ink-muted">{{ t('archives.intro') }}</p>

  <div v-if="routes.isPending.value">
    <p class="sr-only" role="status">{{ t('common.loading') }}</p>
    <div
      v-for="index in 3"
      :key="index"
      class="flex gap-3 border-b border-rule px-4 pt-4 pb-3"
      aria-hidden="true"
    >
      <SkeletonBlock class="size-16 shrink-0" />
      <div class="flex flex-1 flex-col gap-2">
        <SkeletonBlock class="h-11 w-full max-w-72" />
        <SkeletonBlock class="h-4 w-1/2" />
      </div>
    </div>
  </div>
  <div v-else-if="routes.isError.value" class="flex flex-col items-start gap-3 px-4 py-6">
    <FormAlert>{{ t(errorMessageKey(routes.error.value)) }}</FormAlert>
    <GgButton variant="secondary" @click="routes.refetch()">{{ t('common.retry') }}</GgButton>
  </div>
  <EmptyState
    v-else-if="routes.data.value?.length === 0"
    :title="t('archives.emptyTitle')"
    :body="t('archives.emptyBody')"
    :hold="{ label: '', hex: '#9a9a9a' }"
  />
  <ul v-else class="divide-y divide-rule border-y border-rule">
    <li v-for="item in routes.data.value" :key="item.id">
      <RouteRow :route="item" :to="sheetLink(item)" />
    </li>
  </ul>

  <RouterView />
</template>
