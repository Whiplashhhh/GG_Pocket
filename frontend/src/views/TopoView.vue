<script setup lang="ts">
import { Archive, ArrowDownWideNarrow, ArrowUpNarrowWide, SlidersHorizontal } from 'lucide-vue-next'
import { computed, ref, useId } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

import { errorMessageKey } from '@/api/errors'
import { useRoutes, useTopoConfig, type RouteSummary } from '@/api/topo'
import RouteRow from '@/components/topo/RouteRow.vue'
import TopoFilterSheet from '@/components/topo/TopoFilterSheet.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SettingsLink from '@/components/ui/SettingsLink.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import {
  activeFilterCount,
  applyTopoView,
  defaultDirection,
  groupBySector,
  NO_FILTERS,
  parseTopoQuery,
  TOPO_SORTS,
  toTopoQuery,
  type TopoFilters,
  type TopoSort,
  type TopoView,
} from '@/lib/topo'

/**
 * Spec §8.4: the routes on the wall, sorted and filtered in the browser (D-078). Sort and
 * filters live in the URL; the route sheet opens over the list (child route) and keeps them.
 */
const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const config = useTopoConfig()
const routes = useRoutes()
const sortId = useId()
const filtering = ref(false)

const view = computed(() => parseTopoQuery(route.query))
const all = computed(() => routes.data.value ?? [])
const shown = computed(() => applyTopoView(all.value, view.value))
const groups = computed(() => (view.value.sort === 'sector' ? groupBySector(shown.value) : null))
const filterCount = computed(() => activeFilterCount(view.value.filters))

function show(next: TopoView) {
  void router.replace({ path: '/topo', query: toTopoQuery(next) })
}

const sort = computed({
  get: () => view.value.sort,
  set: (next: TopoSort) => {
    show({ ...view.value, sort: next, direction: defaultDirection(next) })
  },
})

const filters = computed({
  get: () => view.value.filters,
  set: (next: TopoFilters) => {
    show({ ...view.value, filters: next })
  },
})

function flipDirection() {
  show({ ...view.value, direction: view.value.direction === 'asc' ? 'desc' : 'asc' })
}

/** The sheet's link keeps the list's query, so the list behind it stays the same. */
function sheetLink(item: RouteSummary) {
  return { path: `/topo/voies/${item.id}`, query: route.query }
}
</script>

<template>
  <div class="flex items-baseline justify-between gap-3 px-4 pt-5">
    <h1 class="text-xl font-title">{{ t('topo.title') }}</h1>
    <p v-if="routes.data.value" class="text-sm text-ink-muted" aria-live="polite">
      <template v-if="filterCount > 0">
        {{ t('topo.countFiltered', { shown: shown.length, total: t('topo.count', all.length) }) }}
      </template>
      <template v-else>{{ t('topo.count', all.length) }}</template>
    </p>
  </div>

  <div
    class="sticky top-[calc(3.5rem+1px)] z-10 mt-3 flex items-center gap-2 border-y border-rule bg-bg/95 px-4 py-2 backdrop-blur-sm"
    role="group"
    :aria-label="t('topo.toolbar')"
  >
    <label :for="sortId" class="sr-only">{{ t('topo.sortBy') }}</label>
    <select
      :id="sortId"
      v-model="sort"
      class="min-h-11 min-w-0 flex-1 rounded-control border-2 border-rule bg-surface px-3 text-base font-semibold sm:flex-none"
    >
      <option v-for="key in TOPO_SORTS" :key="key" :value="key">
        {{ t(`topo.sorts.${key}`) }}
      </option>
    </select>
    <button
      type="button"
      class="inline-flex size-11 shrink-0 items-center justify-center rounded-control border-2 border-rule text-ink hover:border-ink"
      :aria-label="t(view.direction === 'asc' ? 'topo.directionAsc' : 'topo.directionDesc')"
      @click="flipDirection"
    >
      <ArrowUpNarrowWide v-if="view.direction === 'asc'" :size="22" aria-hidden="true" />
      <ArrowDownWideNarrow v-else :size="22" aria-hidden="true" />
    </button>
    <button
      type="button"
      class="inline-flex min-h-11 shrink-0 items-center gap-2 rounded-control border-2 px-3 font-semibold"
      :class="
        filterCount > 0 ? 'border-ink bg-ink text-bg' : 'border-rule text-ink hover:border-ink'
      "
      :aria-label="filterCount > 0 ? t('topo.filtersActive', filterCount) : t('topo.filters')"
      :disabled="config.data.value === undefined"
      @click="filtering = true"
    >
      <SlidersHorizontal :size="20" aria-hidden="true" />
      <span aria-hidden="true">{{ t('topo.filters') }}</span>
      <span
        v-if="filterCount > 0"
        class="inline-flex size-6 items-center justify-center rounded-full bg-brand text-sm text-on-brand"
        aria-hidden="true"
        >{{ filterCount }}</span
      >
    </button>
  </div>

  <div v-if="routes.isPending.value">
    <p class="sr-only" role="status">{{ t('topo.loading') }}</p>
    <div
      v-for="index in 5"
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
    v-else-if="all.length === 0"
    :title="t('topo.emptyTitle')"
    :body="t('topo.emptyBody')"
    :hold="{ label: '', hex: '#1f5fbf' }"
  />
  <EmptyState
    v-else-if="shown.length === 0"
    :title="t('topo.noMatchTitle')"
    :body="t('topo.noMatchBody')"
    :hold="{ label: '', hex: '#f5c518' }"
  >
    <GgButton variant="secondary" @click="filters = { ...NO_FILTERS }">
      {{ t('topo.clearFilters') }}
    </GgButton>
  </EmptyState>
  <template v-else-if="groups">
    <section
      v-for="group in groups"
      :key="group.sectorId"
      :aria-labelledby="`secteur-${group.sectorId}`"
    >
      <h2 :id="`secteur-${group.sectorId}`" class="px-4 pt-6 pb-1 text-lg font-title">
        {{ group.sectorName }}
      </h2>
      <ul class="divide-y divide-rule border-b border-rule">
        <li v-for="item in group.routes" :key="item.id">
          <RouteRow :route="item" :to="sheetLink(item)" />
        </li>
      </ul>
    </section>
  </template>
  <ul v-else class="divide-y divide-rule border-b border-rule">
    <li v-for="item in shown" :key="item.id">
      <RouteRow :route="item" :to="sheetLink(item)" />
    </li>
  </ul>

  <div class="mt-6 border-t border-rule">
    <SettingsLink to="/topo/archives">
      <template #icon><Archive :size="20" /></template>
      {{ t('topo.archivesLink') }}
    </SettingsLink>
  </div>

  <TopoFilterSheet
    v-if="config.data.value"
    v-model:open="filtering"
    v-model:filters="filters"
    :config="config.data.value"
    :routes="all"
    :result-count="shown.length"
  />

  <RouterView />
</template>
