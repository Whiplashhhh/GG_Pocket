<script setup lang="ts">
import {
  Archive,
  ArchiveRestore,
  Copy,
  EllipsisVertical,
  Heart,
  Hourglass,
  MessageCircle,
  Pencil,
  Plus,
  Search,
  TimerOff,
} from 'lucide-vue-next'
import { computed, nextTick, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter, type RouteLocationRaw } from 'vue-router'

import { errorMessageKey } from '@/api/errors'
import { useDismountedRoutes, useRoutes, useTopoConfig, type RouteSummary } from '@/api/topo'
import { useCancelLastChance } from '@/api/topoAdmin'
import RouteStatusDialogs from '@/components/topo/RouteStatusDialogs.vue'
import RouteSummaryLabel from '@/components/topo/RouteSummaryLabel.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import GgDialog from '@/components/ui/GgDialog.vue'
import GgIconButton from '@/components/ui/GgIconButton.vue'
import SegmentedControl from '@/components/ui/SegmentedControl.vue'
import SelectField from '@/components/ui/SelectField.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import TextField from '@/components/ui/TextField.vue'
import { orderedSectors } from '@/lib/clubConfig'
import { formatDate, formatShortDate } from '@/lib/format'
import type { RouteStatusAction } from '@/lib/route'
import { filterManagedRoutes, sortRoutes } from '@/lib/topo'

import ManagementTitle from './ManagementTitle.vue'

/**
 * Spec §8.10: the routes on the wall (and, one tap away, the archives) with what a setter does
 * to them. A list on phones, each line opening its actions; a table on large screens.
 */
type Shelf = 'wall' | 'archives'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const shelf = computed<Shelf>({
  get: () => (route.query.vue === 'archives' ? 'archives' : 'wall'),
  set: (next) => {
    void router.replace({
      query: { ...route.query, vue: next === 'archives' ? 'archives' : undefined },
    })
  },
})
const wall = useRoutes()
const archives = useDismountedRoutes(() => shelf.value === 'archives')
const routes = computed(() => (shelf.value === 'archives' ? archives : wall))
const config = useTopoConfig()
const sectors = computed(() => (config.data.value ? orderedSectors(config.data.value) : []))

const search = ref('')
const sectorId = ref('')
const shown = computed(() => {
  const found = filterManagedRoutes(routes.value.data.value ?? [], search.value, sectorId.value)
  // The wall reads from left to right; the archives, most recent dismount first.
  return shelf.value === 'wall' ? sortRoutes(found, 'sector', 'asc') : found
})

/** A confirmation left by the route form after saving (history state, gone after reading). */
function noticeFromForm(): string | null {
  const state = window.history.state as Record<string, unknown> | null
  const notice = state?.notice
  if (typeof notice !== 'string') return null
  window.history.replaceState({ ...state, notice: undefined }, '')
  return notice
}

const notice = ref<string | null>(noticeFromForm())
const failure = ref<unknown>(null)
const cancelLastChance = useCancelLastChance()

function say(message: string) {
  failure.value = null
  notice.value = message
}
function fail(error: unknown) {
  notice.value = null
  failure.value = error
}

/** Phones: the actions of one route, in a sheet. */
const sheetRoute = ref<RouteSummary | null>(null)
const sheetOpen = ref(false)
function openActions(item: RouteSummary) {
  sheetRoute.value = item
  sheetOpen.value = true
}

const statusAction = ref<RouteStatusAction | null>(null)
function ask(kind: RouteStatusAction['kind'], item: RouteSummary) {
  // One dialog at a time: the actions sheet gives the focus back before the next one opens.
  sheetOpen.value = false
  void nextTick(() => {
    statusAction.value = { kind, route: item }
  })
}

function cancel(item: RouteSummary) {
  sheetOpen.value = false
  cancelLastChance.mutate(item.id, {
    onSuccess: () => {
      say(t('routeStatus.cancelled', { name: item.name }))
    },
    onError: fail,
  })
}

const editLink = (item: RouteSummary): RouteLocationRaw => `/gestion/voies/${item.id}/modifier`
const duplicateLink = (item: RouteSummary): RouteLocationRaw => ({
  path: '/gestion/voies/nouvelle',
  query: { depuis: item.id },
})
const sheetLink = (item: RouteSummary): RouteLocationRaw =>
  item.status === 'DISMOUNTED' ? `/topo/archives/voies/${item.id}` : `/topo/voies/${item.id}`

function statusLine(item: RouteSummary, format = formatDate): string {
  if (item.status === 'DISMOUNTED' && item.dismountedAt) {
    return t('routesAdmin.dismountedOn', { date: format(item.dismountedAt) })
  }
  if (item.status === 'LAST_CHANCE') {
    return item.plannedDismountOn
      ? t('routesAdmin.plannedOn', { date: format(item.plannedDismountOn) })
      : t('routesAdmin.noPlannedDate')
  }
  return t('routesAdmin.openedOn', { date: format(item.openedOn) })
}
</script>

<template>
  <ManagementTitle>{{ t('routesAdmin.title') }}</ManagementTitle>
  <p class="max-w-prose px-4 pt-1 pb-4 text-ink-muted lg:px-0">{{ t('routesAdmin.intro') }}</p>

  <div class="flex flex-col gap-3 px-4 pb-4 lg:px-0" aria-live="polite">
    <FormAlert v-if="notice" tone="success">{{ notice }}</FormAlert>
    <FormAlert v-if="failure">{{ t(errorMessageKey(failure)) }}</FormAlert>
  </div>

  <div class="flex flex-wrap items-center gap-3 px-4 pb-4 lg:px-0">
    <GgButton to="/gestion/voies/nouvelle">
      <Plus :size="20" aria-hidden="true" />
      {{ t('routesAdmin.create') }}
    </GgButton>
    <SegmentedControl
      v-model="shelf"
      :label="t('routesAdmin.views')"
      :options="[
        { value: 'wall', label: t('routesAdmin.onTheWall') },
        { value: 'archives', label: t('routesAdmin.archives') },
      ]"
    />
  </div>

  <div class="flex flex-col gap-3 px-4 pb-4 lg:flex-row lg:items-end lg:px-0">
    <div class="lg:w-80">
      <TextField
        v-model="search"
        type="search"
        inputmode="search"
        :label="t('routesAdmin.search')"
        :placeholder="t('routesAdmin.searchPlaceholder')"
        optional
        :show-optional-mark="false"
      >
        <template #trailing>
          <Search :size="20" class="mr-3 text-ink-muted" aria-hidden="true" />
        </template>
      </TextField>
    </div>
    <div class="lg:w-64">
      <SelectField v-model="sectorId" :label="t('routesAdmin.sector')" optional>
        <option value="">{{ t('routesAdmin.allSectors') }}</option>
        <option v-for="sector in sectors" :key="sector.id" :value="sector.id">
          {{ sector.name }}
        </option>
      </SelectField>
    </div>
    <p class="text-sm text-ink-muted lg:ml-auto" role="status">
      {{ t('routesAdmin.count', shown.length) }}
    </p>
  </div>

  <div v-if="routes.isPending.value" class="flex flex-col gap-3 px-4 lg:px-0" aria-hidden="true">
    <SkeletonBlock v-for="index in 5" :key="index" class="h-14 w-full" />
  </div>
  <div v-else-if="routes.isError.value" class="flex flex-col items-start gap-3 px-4 lg:px-0">
    <FormAlert>{{ t(errorMessageKey(routes.error.value)) }}</FormAlert>
    <GgButton variant="secondary" @click="routes.refetch()">{{ t('common.retry') }}</GgButton>
  </div>
  <EmptyState
    v-else-if="(routes.data.value ?? []).length === 0"
    :title="t(shelf === 'wall' ? 'routesAdmin.emptyWallTitle' : 'routesAdmin.emptyArchivesTitle')"
    :body="t(shelf === 'wall' ? 'routesAdmin.emptyWallBody' : 'routesAdmin.emptyArchivesBody')"
  />
  <p v-else-if="shown.length === 0" class="px-4 py-6 text-ink-muted lg:px-0">
    {{ t('routesAdmin.noMatch') }}
  </p>

  <template v-else>
    <!-- Phones: one line per route, opening its actions. -->
    <ul class="border-t border-rule lg:hidden">
      <li v-for="item in shown" :key="item.id" class="border-b border-rule">
        <button
          type="button"
          aria-haspopup="dialog"
          class="flex w-full items-center gap-3 px-4 py-3 text-left outline-offset-[-3px] hover:bg-surface-sunken"
          @click="openActions(item)"
        >
          <span class="flex min-w-0 flex-1 flex-col items-start gap-1.5">
            <RouteSummaryLabel :route="item" />
            <span class="flex flex-wrap gap-x-2 text-sm text-ink-muted">
              <span>{{ item.sectorName }}</span>
              <span aria-hidden="true">·</span>
              <span :class="{ 'font-semibold text-brand-ink': item.status === 'LAST_CHANCE' }">{{
                statusLine(item)
              }}</span>
              <template v-if="item.favoriteCount > 0">
                <span aria-hidden="true">·</span>
                <span>{{ t('topo.row.favorites', item.favoriteCount) }}</span>
              </template>
            </span>
          </span>
          <EllipsisVertical :size="20" class="shrink-0 text-ink-muted" aria-hidden="true" />
          <span class="sr-only">{{ t('routesAdmin.actions') }}</span>
        </button>
      </li>
    </ul>

    <!-- Large screens: a table with the actions on each row. -->
    <div class="hidden overflow-x-auto lg:block">
      <table class="w-full border-collapse text-left">
        <thead>
          <tr class="border-b-2 border-ink">
            <th scope="col" class="py-2 pr-3 font-semibold">
              {{ t('routesAdmin.columns.route') }}
            </th>
            <th scope="col" class="py-2 pr-3 font-semibold">
              {{ t('routesAdmin.columns.sector') }}
            </th>
            <th scope="col" class="py-2 pr-3 text-right font-semibold">
              <span :title="t('routesAdmin.columns.favorites')" class="inline-flex">
                <Heart :size="18" aria-hidden="true" />
                <span class="sr-only">{{ t('routesAdmin.columns.favorites') }}</span>
              </span>
            </th>
            <th scope="col" class="py-2 pr-3 text-right font-semibold">
              <span :title="t('routesAdmin.columns.comments')" class="inline-flex">
                <MessageCircle :size="18" aria-hidden="true" />
                <span class="sr-only">{{ t('routesAdmin.columns.comments') }}</span>
              </span>
            </th>
            <th scope="col" class="py-2 pr-3 font-semibold">
              {{ t('routesAdmin.columns.status') }}
            </th>
            <th scope="col" class="py-2 text-right font-semibold whitespace-nowrap">
              {{ t('routesAdmin.columns.actions') }}
            </th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="item in shown"
            :key="item.id"
            class="border-b border-rule hover:bg-surface-sunken"
          >
            <th scope="row" class="max-w-80 py-2 pr-3 font-normal">
              <RouteSummaryLabel :route="item" />
            </th>
            <td class="min-w-28 py-2 pr-3">{{ item.sectorName }}</td>
            <td class="py-2 pr-3 text-right tabular-nums">{{ item.favoriteCount }}</td>
            <td class="py-2 pr-3 text-right tabular-nums">{{ item.commentCount }}</td>
            <td class="min-w-46 py-2 pr-3">
              <span
                class="font-semibold"
                :class="{
                  'text-brand-ink': item.status === 'LAST_CHANCE',
                  'text-ink-muted': item.status === 'DISMOUNTED',
                }"
                >{{ t(`routesAdmin.status.${item.status}`) }}</span
              >
              <span class="block text-sm text-ink-muted">{{
                statusLine(item, formatShortDate)
              }}</span>
            </td>
            <td class="py-2">
              <div class="flex justify-end gap-1">
                <template v-if="item.status !== 'DISMOUNTED'">
                  <GgIconButton
                    :to="editLink(item)"
                    :label="t('routesAdmin.editNamed', { name: item.name })"
                    :title="t('routesAdmin.edit')"
                  >
                    <Pencil :size="20" aria-hidden="true" />
                  </GgIconButton>
                  <GgIconButton
                    :to="duplicateLink(item)"
                    :label="t('routesAdmin.duplicateNamed', { name: item.name })"
                    :title="t('routesAdmin.duplicate')"
                  >
                    <Copy :size="20" aria-hidden="true" />
                  </GgIconButton>
                  <GgIconButton
                    v-if="item.status === 'ACTIVE'"
                    :label="t('routesAdmin.lastChanceNamed', { name: item.name })"
                    :title="t('routesAdmin.lastChance')"
                    @click="ask('lastChance', item)"
                  >
                    <Hourglass :size="20" aria-hidden="true" />
                  </GgIconButton>
                  <GgIconButton
                    v-else
                    :label="t('routesAdmin.cancelLastChanceNamed', { name: item.name })"
                    :title="t('routesAdmin.cancelLastChance')"
                    @click="cancel(item)"
                  >
                    <TimerOff :size="20" aria-hidden="true" />
                  </GgIconButton>
                  <GgIconButton
                    :label="t('routesAdmin.dismountNamed', { name: item.name })"
                    :title="t('routesAdmin.dismount')"
                    @click="ask('dismount', item)"
                  >
                    <Archive :size="20" aria-hidden="true" />
                  </GgIconButton>
                </template>
                <template v-else>
                  <GgIconButton
                    :to="duplicateLink(item)"
                    :label="t('routesAdmin.duplicateNamed', { name: item.name })"
                    :title="t('routesAdmin.duplicate')"
                  >
                    <Copy :size="20" aria-hidden="true" />
                  </GgIconButton>
                  <GgIconButton
                    :label="t('routesAdmin.remountNamed', { name: item.name })"
                    :title="t('routesAdmin.remount')"
                    @click="ask('remount', item)"
                  >
                    <ArchiveRestore :size="20" aria-hidden="true" />
                  </GgIconButton>
                </template>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </template>

  <GgDialog
    v-model:open="sheetOpen"
    :title="t('routesAdmin.actionsFor', { name: sheetRoute?.name ?? '' })"
  >
    <div v-if="sheetRoute" class="flex flex-col gap-2">
      <div class="pb-2"><RouteSummaryLabel :route="sheetRoute" /></div>
      <template v-if="sheetRoute.status !== 'DISMOUNTED'">
        <GgButton variant="secondary" block :to="editLink(sheetRoute)">
          <Pencil :size="20" aria-hidden="true" />
          {{ t('routesAdmin.edit') }}
        </GgButton>
        <GgButton variant="secondary" block :to="duplicateLink(sheetRoute)">
          <Copy :size="20" aria-hidden="true" />
          {{ t('routesAdmin.duplicate') }}
        </GgButton>
        <GgButton
          v-if="sheetRoute.status === 'ACTIVE'"
          variant="secondary"
          block
          @click="ask('lastChance', sheetRoute)"
        >
          <Hourglass :size="20" aria-hidden="true" />
          {{ t('routesAdmin.lastChance') }}
        </GgButton>
        <GgButton v-else variant="secondary" block @click="cancel(sheetRoute)">
          <TimerOff :size="20" aria-hidden="true" />
          {{ t('routesAdmin.cancelLastChance') }}
        </GgButton>
        <GgButton variant="secondary" block :to="sheetLink(sheetRoute)">
          {{ t('routesAdmin.sheet') }}
        </GgButton>
        <GgButton variant="danger" block @click="ask('dismount', sheetRoute)">
          <Archive :size="20" aria-hidden="true" />
          {{ t('routesAdmin.dismount') }}
        </GgButton>
      </template>
      <template v-else>
        <GgButton variant="secondary" block :to="duplicateLink(sheetRoute)">
          <Copy :size="20" aria-hidden="true" />
          {{ t('routesAdmin.duplicate') }}
        </GgButton>
        <GgButton variant="secondary" block :to="sheetLink(sheetRoute)">
          {{ t('routesAdmin.sheet') }}
        </GgButton>
        <GgButton block @click="ask('remount', sheetRoute)">
          <ArchiveRestore :size="20" aria-hidden="true" />
          {{ t('routesAdmin.remount') }}
        </GgButton>
      </template>
    </div>
  </GgDialog>

  <RouteStatusDialogs v-model:action="statusAction" @done="say" @failed="fail" />
</template>
