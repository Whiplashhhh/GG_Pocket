<script setup lang="ts">
import { Check, Minus, Plus } from 'lucide-vue-next'
import { computed, ref, useId } from 'vue'
import { useI18n } from 'vue-i18n'

import type { RouteSummary } from '@/api/topo'
import RouteSummaryLabel from '@/components/topo/RouteSummaryLabel.vue'
import TextField from '@/components/ui/TextField.vue'
import { searchRoutes } from '@/lib/topo'

/**
 * Adds routes to the session (spec §8.3): first the ones I climbed lately, then a search by cord
 * number ("12"), name, colour or grade.
 */
const props = withDefaults(
  defineProps<{
    /** Routes on the wall. */
    routes: readonly RouteSummary[]
    /** Ids of the routes I climbed, most recent first. */
    recentIds: readonly string[]
    /** How many lines each route already has in the session. */
    addedCounts?: ReadonlyMap<string, number>
    disabled?: boolean
    limit?: number
  }>(),
  { addedCounts: () => new Map(), disabled: false, limit: 8 },
)
const emit = defineEmits<{ add: [routeId: string]; remove: [routeId: string] }>()

const { t } = useI18n()
const baseId = useId()
const search = ref('')

const byId = computed(() => new Map(props.routes.map((route) => [route.id, route])))
const recent = computed(() =>
  props.recentIds
    .map((id) => byId.value.get(id))
    .filter((route): route is RouteSummary => route !== undefined)
    .slice(0, props.limit),
)
const results = computed(() => searchRoutes(props.routes, search.value))
const searching = computed(() => search.value.trim() !== '')
const shown = computed(() => (searching.value ? results.value.slice(0, props.limit) : recent.value))

function addedCount(id: string): number {
  return props.addedCounts.get(id) ?? 0
}

function add(id: string) {
  emit('add', id)
}

function remove(id: string) {
  emit('remove', id)
}
</script>

<template>
  <section class="flex flex-col gap-3 px-4 py-4" :aria-label="t('session.pickerTitle')">
    <TextField
      v-model="search"
      type="search"
      inputmode="search"
      :label="t('session.pickerSearch')"
      :placeholder="t('session.pickerPlaceholder')"
      optional
      :show-optional-mark="false"
    />
    <h3 v-if="!searching && recent.length > 0" class="font-semibold text-ink-muted">
      {{ t('session.recent') }}
    </h3>
    <p v-if="!searching && recent.length === 0" class="text-ink-muted">
      {{ t('session.pickerHint') }}
    </p>
    <p v-if="searching" class="sr-only" role="status">
      {{ t('routePicker.results', results.length) }}
    </p>
    <p v-if="searching && results.length === 0" class="text-ink-muted">
      {{ t('routePicker.noMatch') }}
    </p>
    <ul v-if="shown.length > 0" class="divide-y divide-rule border-y border-rule">
      <li
        v-for="route in shown"
        :key="route.id"
        class="flex items-center gap-2"
        :class="{ 'bg-brand/10': addedCount(route.id) > 0 }"
      >
        <span class="flex min-w-0 flex-1 flex-col items-start gap-0.5 py-2">
          <RouteSummaryLabel :route="route" :label-id="`${baseId}-${route.id}`" />
          <span
            v-if="addedCount(route.id) > 0"
            class="inline-flex items-center gap-1 text-sm font-semibold text-brand-ink"
          >
            <Check :size="16" :stroke-width="3" aria-hidden="true" />
            {{ t('session.inSession', addedCount(route.id)) }}
          </span>
        </span>
        <button
          v-if="addedCount(route.id) > 0"
          type="button"
          class="inline-flex size-12 shrink-0 items-center justify-center rounded-control border-2 border-rule bg-bg text-ink hover:border-ink"
          :aria-label="
            t(
              'session.removeRoute',
              { name: route.name, n: addedCount(route.id) - 1 },
              addedCount(route.id) - 1,
            )
          "
          @click="remove(route.id)"
        >
          <Minus :size="24" :stroke-width="3" aria-hidden="true" />
        </button>
        <button
          type="button"
          class="inline-flex size-12 shrink-0 items-center justify-center rounded-control disabled:cursor-not-allowed disabled:opacity-45"
          :class="
            addedCount(route.id) > 0
              ? 'border-2 border-brand bg-bg text-brand-ink'
              : 'bg-brand text-on-brand'
          "
          :disabled="disabled"
          :aria-label="
            addedCount(route.id) > 0
              ? t('session.addRouteAgain', { name: route.name, n: addedCount(route.id) })
              : t('session.addRoute', { name: route.name })
          "
          @click="add(route.id)"
        >
          <Plus :size="24" :stroke-width="3" aria-hidden="true" />
        </button>
      </li>
    </ul>
    <p v-if="searching && results.length > shown.length" class="text-sm text-ink-muted">
      {{ t('routePicker.more', { n: results.length - shown.length }) }}
    </p>
  </section>
</template>
