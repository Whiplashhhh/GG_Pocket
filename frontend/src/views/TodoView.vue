<script setup lang="ts">
import { ArrowDownWideNarrow, ArrowUpNarrowWide, Check, Undo2 } from 'lucide-vue-next'
import { computed, ref, useId } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

import { errorMessageKey } from '@/api/errors'
import { useMyTodo, useTodoCheck, type TodoRoute } from '@/api/todo'
import RouteSummaryLabel from '@/components/topo/RouteSummaryLabel.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SegmentedControl from '@/components/ui/SegmentedControl.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import UndoToast from '@/components/ui/UndoToast.vue'
import type { ClimbMode } from '@/lib/route'
import {
  familyOptions,
  knownRoutes,
  parseTodoQuery,
  sectorOptions,
  stateIn,
  todoRoutes,
  toTodoQuery,
  type TodoView,
} from '@/lib/todo'

/**
 * Spec §8.5: per mode, the routes on the wall I cannot climb yet. Ticking one sends it away (with
 * a few seconds to undo); "Voir celles que je sais faire" lists the others, to untick by hand.
 * Which routes are known comes from the server (lead implies top rope, clean ascents count).
 */
const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const todo = useMyTodo()
const check = useTodoCheck()
const sectorId = useId()
const gradeId = useId()
const listId = useId()

const view = computed(() => parseTodoQuery(route.query))
const showingKnown = ref(false)

function show(next: TodoView) {
  void router.replace({ path: '/a-faire', query: toTodoQuery(next) })
}

const mode = computed({
  get: () => view.value.mode,
  set: (next: ClimbMode) => {
    show({ ...view.value, mode: next })
  },
})
const sector = computed({
  get: () => view.value.sectorId ?? '',
  set: (next: string) => {
    const changed: TodoView = { ...view.value, sectorId: next }
    if (next === '') delete changed.sectorId
    show(changed)
  },
})
const family = computed({
  get: () => view.value.family ?? '',
  set: (next: string) => {
    const changed: TodoView = { ...view.value, family: next }
    if (next === '') delete changed.family
    show(changed)
  },
})

function flipDirection() {
  show({ ...view.value, direction: view.value.direction === 'asc' ? 'desc' : 'asc' })
}

const all = computed(() => todo.data.value?.routes ?? [])
const remaining = computed(() => todoRoutes(all.value, view.value))
const known = computed(() => knownRoutes(all.value, view.value))
const sectors = computed(() => sectorOptions(all.value))
const families = computed(() => familyOptions(all.value))
const filtered = computed(
  () => view.value.sectorId !== undefined || view.value.family !== undefined,
)

const modeWord = computed(() => t(`todo.modeWords.${view.value.mode}`))
const total = computed(() => todo.data.value?.routeCount ?? 0)
const knownCount = computed(() => {
  const data = todo.data.value
  if (data === undefined) return 0
  return view.value.mode === 'LEAD' ? data.leadKnown : data.topRopeKnown
})
const left = computed(() => total.value - knownCount.value)
const percent = computed(() =>
  total.value === 0 ? 0 : Math.round((knownCount.value / total.value) * 100),
)
const knownInMode = computed(
  () => all.value.filter((item) => stateIn(item, view.value.mode) !== 'TODO').length,
)

const toast = ref<{ message: string; undo?: () => void } | undefined>()

function tick(item: TodoRoute) {
  const target = { id: item.route.id, mode: view.value.mode }
  check.mutate(
    { ...target, checked: true },
    {
      onSuccess: () => {
        toast.value = {
          message: t('todo.checked', { name: item.route.name }),
          undo: () => {
            untick(item, target.mode, true)
          },
        }
      },
      onError: () => {
        toast.value = { message: t('todo.checkFailed') }
      },
    },
  )
}

function untick(item: TodoRoute, inMode: ClimbMode, fromUndo = false) {
  toast.value = undefined
  check.mutate(
    { id: item.route.id, mode: inMode, checked: false },
    {
      onSuccess: () => {
        if (fromUndo) toast.value = { message: t('todo.undone', { name: item.route.name }) }
      },
      onError: (error) => {
        toast.value = { message: t(errorMessageKey(error)) }
      },
    },
  )
}

function undo() {
  const action = toast.value?.undo
  toast.value = undefined
  action?.()
}

/** The sheet opens over the list and keeps its query, like in the topo. */
function sheetLink(item: TodoRoute) {
  return { path: `/a-faire/voies/${item.route.id}`, query: route.query }
}
</script>

<template>
  <div class="flex items-baseline justify-between gap-3 px-4 pt-5">
    <h1 class="text-xl font-title">{{ t('todo.title') }}</h1>
  </div>

  <div class="px-4 pt-3">
    <SegmentedControl
      v-model="mode"
      :label="t('todo.modes')"
      :options="[
        { value: 'TOP_ROPE', label: t('todo.topRope') },
        { value: 'LEAD', label: t('todo.lead') },
      ]"
      stretch
    />
  </div>

  <div v-if="todo.isPending.value" class="px-4 pt-4">
    <p class="sr-only" role="status">{{ t('todo.loading') }}</p>
    <SkeletonBlock class="h-12 w-full" />
    <div v-for="index in 4" :key="index" class="mt-4 flex items-center gap-3" aria-hidden="true">
      <SkeletonBlock class="h-11 flex-1" />
      <SkeletonBlock class="size-11 shrink-0" />
    </div>
  </div>

  <div v-else-if="todo.isError.value" class="flex flex-col items-start gap-3 px-4 py-6">
    <FormAlert>{{ t(errorMessageKey(todo.error.value)) }}</FormAlert>
    <GgButton variant="secondary" @click="todo.refetch()">{{ t('common.retry') }}</GgButton>
  </div>

  <EmptyState
    v-else-if="total === 0"
    :title="t('todo.emptyTitle')"
    :body="t('todo.emptyBody')"
    :hold="{ label: '', hex: '#2e9e44' }"
  />

  <template v-else>
    <section class="px-4 pt-4">
      <p class="text-base font-semibold">
        <span class="text-2xl font-title tabular-nums">{{ knownCount }}</span>
        <span class="tabular-nums"> / {{ total }}</span>
        {{ t('todo.progressUnit', { mode: modeWord }) }}
      </p>
      <p class="text-sm text-ink-muted">{{ t('todo.remaining', left) }}</p>
      <div
        class="mt-2 h-2 overflow-hidden rounded-full bg-surface-sunken"
        role="progressbar"
        :aria-valuenow="knownCount"
        aria-valuemin="0"
        :aria-valuemax="total"
        :aria-label="t('todo.progressLabel', { known: knownCount, total, mode: modeWord })"
      >
        <div
          class="h-full rounded-full bg-success transition-[width] duration-200 ease-out motion-reduce:transition-none"
          :style="{ width: `${percent}%` }"
        />
      </div>
    </section>

    <div
      class="sticky top-[calc(3.5rem+1px)] z-10 mt-4 flex items-center gap-2 border-y border-rule bg-bg/95 px-4 py-2 backdrop-blur-sm"
      role="group"
      :aria-label="t('todo.toolbar')"
    >
      <label :for="sectorId" class="sr-only">{{ t('todo.sector') }}</label>
      <select
        :id="sectorId"
        v-model="sector"
        class="min-h-11 min-w-0 flex-1 rounded-control border-2 border-rule bg-surface px-2 text-base font-semibold"
      >
        <option value="">{{ t('todo.allSectors') }}</option>
        <option v-for="option in sectors" :key="option.id" :value="option.id">
          {{ option.name }}
        </option>
      </select>
      <label :for="gradeId" class="sr-only">{{ t('todo.grade') }}</label>
      <select
        :id="gradeId"
        v-model="family"
        class="min-h-11 w-28 shrink-0 rounded-control border-2 border-rule bg-surface px-2 text-base font-semibold"
      >
        <option value="">{{ t('todo.allGrades') }}</option>
        <option v-for="option in families" :key="option" :value="option">
          {{ t('todo.gradeFamily', { family: option }) }}
        </option>
      </select>
      <button
        type="button"
        class="inline-flex size-11 shrink-0 items-center justify-center rounded-control border-2 border-rule text-ink hover:border-ink"
        :aria-label="t(view.direction === 'asc' ? 'todo.sortEasyFirst' : 'todo.sortHardFirst')"
        @click="flipDirection"
      >
        <ArrowUpNarrowWide v-if="view.direction === 'asc'" :size="22" aria-hidden="true" />
        <ArrowDownWideNarrow v-else :size="22" aria-hidden="true" />
      </button>
    </div>

    <template v-if="!showingKnown">
      <TransitionGroup
        v-if="remaining.length > 0"
        :id="listId"
        tag="ul"
        leave-active-class="transition duration-150 ease-out motion-reduce:transition-none"
        leave-to-class="opacity-0 -translate-x-4"
      >
        <li
          v-for="item in remaining"
          :key="item.route.id"
          class="flex items-center gap-3 border-b border-rule px-4 py-3"
        >
          <RouterLink :to="sheetLink(item)" class="flex min-w-0 flex-1 flex-col items-start gap-1">
            <RouteSummaryLabel :route="item.route" />
            <span class="text-sm text-ink-muted">{{ item.route.sectorName }}</span>
          </RouterLink>
          <button
            type="button"
            class="inline-flex size-12 shrink-0 items-center justify-center rounded-full border-2 border-ink text-ink transition-colors duration-150 hover:bg-success hover:text-bg focus-visible:bg-success focus-visible:text-bg"
            :aria-label="t('todo.check', { name: item.route.name, mode: modeWord })"
            @click="tick(item)"
          >
            <Check :size="24" :stroke-width="3" aria-hidden="true" />
          </button>
        </li>
      </TransitionGroup>
      <div v-else-if="filtered" class="px-4 py-6 text-ink-muted">{{ t('todo.filteredEmpty') }}</div>
      <div v-else class="px-4 py-6">
        <h2 class="text-lg font-title">{{ t('todo.allDoneTitle', { mode: modeWord }) }}</h2>
        <p class="mt-1 text-ink-muted">{{ t('todo.allDoneBody') }}</p>
      </div>
    </template>

    <section v-else :aria-labelledby="`${listId}-known`">
      <h2 :id="`${listId}-known`" class="px-4 pt-4 pb-2 text-lg font-title">
        {{ t('todo.knownTitle', { mode: modeWord }) }}
      </h2>
      <ul v-if="known.length > 0">
        <li
          v-for="item in known"
          :key="item.route.id"
          class="flex items-center gap-3 border-b border-rule px-4 py-3"
        >
          <RouterLink :to="sheetLink(item)" class="flex min-w-0 flex-1 flex-col items-start gap-1">
            <RouteSummaryLabel :route="item.route" />
            <span class="text-sm font-semibold text-success">{{
              t(`todo.states.${stateIn(item, view.mode)}`)
            }}</span>
            <span
              v-if="stateIn(item, view.mode) === 'FROM_ASCENT'"
              class="text-sm text-ink-muted"
              >{{ t('todo.fromAscentHint') }}</span
            >
            <span
              v-else-if="stateIn(item, view.mode) === 'FROM_LEAD'"
              class="text-sm text-ink-muted"
              >{{ t('todo.fromLeadHint') }}</span
            >
          </RouterLink>
          <button
            v-if="stateIn(item, view.mode) === 'CHECKED'"
            type="button"
            class="inline-flex min-h-11 shrink-0 items-center gap-1.5 rounded-control border-2 border-rule px-3 font-semibold text-ink hover:border-ink"
            :aria-label="t('todo.uncheckLabel', { name: item.route.name, mode: modeWord })"
            @click="untick(item, view.mode)"
          >
            <Undo2 :size="18" aria-hidden="true" />
            {{ t('todo.uncheck') }}
          </button>
        </li>
      </ul>
      <p v-else class="px-4 py-2 text-ink-muted">{{ t('todo.knownEmpty') }}</p>
    </section>

    <div class="px-4 pt-4 pb-28">
      <GgButton variant="secondary" @click="showingKnown = !showingKnown">
        {{ showingKnown ? t('todo.hideKnown') : t('todo.showKnown', { n: knownInMode }) }}
      </GgButton>
    </div>
  </template>

  <UndoToast
    :message="toast?.message"
    :can-undo="toast?.undo !== undefined"
    @undo="undo"
    @dismiss="toast = undefined"
  />
  <RouterView />
</template>
