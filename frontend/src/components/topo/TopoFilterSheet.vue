<script setup lang="ts">
import { computed, useId } from 'vue'
import { useI18n } from 'vue-i18n'

import type { RouteSummary, TopoConfig } from '@/api/topo'
import CheckboxField from '@/components/ui/CheckboxField.vue'
import GgButton from '@/components/ui/GgButton.vue'
import GgDialog from '@/components/ui/GgDialog.vue'
import HoldSwatch from '@/components/ui/HoldSwatch.vue'
import ToggleChip from '@/components/ui/ToggleChip.vue'
import { ROUTE_STYLES } from '@/lib/route'
import { NO_FILTERS, type TopoFilters } from '@/lib/topo'

/** The topo filters (spec §8.4), applied as they are touched: the button says how many remain. */
const props = defineProps<{
  config: TopoConfig
  /** Routes on the wall: a deactivated colour is offered only while a route still uses it. */
  routes: readonly RouteSummary[]
  /** How many routes pass the filters. */
  resultCount: number
}>()

const open = defineModel<boolean>('open', { required: true })
const filters = defineModel<TopoFilters>('filters', { required: true })

const { t } = useI18n()
const minId = useId()
const maxId = useId()

const colors = computed(() => {
  const used = new Set(props.routes.flatMap((route) => route.holds.map((hold) => hold.id)))
  return [...props.config.holdColors]
    .filter((color) => color.active || used.has(color.id))
    .sort((a, b) => a.position - b.position)
})
const sectors = computed(() => [...props.config.sectors].sort((a, b) => a.position - b.position))
const grades = computed(() => [...props.config.grades].sort((a, b) => a.rank - b.rank))

function toggled<T>(list: readonly T[], value: T): T[] {
  return list.includes(value) ? list.filter((item) => item !== value) : [...list, value]
}

function update(patch: Partial<TopoFilters>) {
  filters.value = { ...filters.value, ...patch }
}

function rankOf(value: string): number | undefined {
  return value === '' ? undefined : Number(value)
}
</script>

<template>
  <GgDialog
    v-model:open="open"
    :title="t('topo.filtersTitle')"
    :description="t('topo.filtersDescription')"
  >
    <div class="flex flex-col gap-6">
      <fieldset>
        <legend class="mb-2 font-semibold">{{ t('topo.filterSectors') }}</legend>
        <div class="flex flex-wrap gap-2">
          <ToggleChip
            v-for="sector in sectors"
            :key="sector.id"
            :pressed="filters.sectorIds.includes(sector.id)"
            @toggle="update({ sectorIds: toggled(filters.sectorIds, sector.id) })"
          >
            {{ sector.name }}
          </ToggleChip>
        </div>
      </fieldset>

      <fieldset>
        <legend class="mb-2 font-semibold">{{ t('topo.filterGrades') }}</legend>
        <div class="grid grid-cols-2 gap-3">
          <div class="flex flex-col gap-1.5">
            <label :for="minId" class="text-sm text-ink-muted">{{ t('topo.gradeMin') }}</label>
            <select
              :id="minId"
              :value="filters.minRank ?? ''"
              class="min-h-12 rounded-control border-2 border-rule bg-surface px-3 text-base"
              @change="update({ minRank: rankOf(($event.target as HTMLSelectElement).value) })"
            >
              <option value="">{{ t('topo.gradeAny') }}</option>
              <option
                v-for="grade in grades"
                :key="grade.id"
                :value="grade.rank"
                :disabled="filters.maxRank !== undefined && grade.rank > filters.maxRank"
              >
                {{ grade.code }}
              </option>
            </select>
          </div>
          <div class="flex flex-col gap-1.5">
            <label :for="maxId" class="text-sm text-ink-muted">{{ t('topo.gradeMax') }}</label>
            <select
              :id="maxId"
              :value="filters.maxRank ?? ''"
              class="min-h-12 rounded-control border-2 border-rule bg-surface px-3 text-base"
              @change="update({ maxRank: rankOf(($event.target as HTMLSelectElement).value) })"
            >
              <option value="">{{ t('topo.gradeAny') }}</option>
              <option
                v-for="grade in grades"
                :key="grade.id"
                :value="grade.rank"
                :disabled="filters.minRank !== undefined && grade.rank < filters.minRank"
              >
                {{ grade.code }}
              </option>
            </select>
          </div>
        </div>
      </fieldset>

      <fieldset>
        <legend class="mb-2 font-semibold">{{ t('topo.filterColors') }}</legend>
        <div class="flex flex-wrap gap-2">
          <ToggleChip
            v-for="color in colors"
            :key="color.id"
            :pressed="filters.holdColorIds.includes(color.id)"
            @toggle="update({ holdColorIds: toggled(filters.holdColorIds, color.id) })"
          >
            <HoldSwatch :color="color" :size="22" />
            {{ color.label }}
          </ToggleChip>
        </div>
      </fieldset>

      <fieldset>
        <legend class="mb-2 font-semibold">{{ t('topo.filterStyles') }}</legend>
        <div class="flex flex-wrap gap-2">
          <ToggleChip
            v-for="style in ROUTE_STYLES"
            :key="style"
            :pressed="filters.styles.includes(style)"
            @toggle="update({ styles: toggled(filters.styles, style) })"
          >
            {{ t(`routeStyles.${style}`) }}
          </ToggleChip>
        </div>
      </fieldset>

      <CheckboxField
        :model-value="filters.notDoneByMe"
        :hint="t('topo.notDoneByMeHint')"
        @update:model-value="update({ notDoneByMe: $event })"
      >
        {{ t('topo.notDoneByMe') }}
      </CheckboxField>
    </div>

    <div
      class="sticky bottom-0 -mx-4 mt-6 flex flex-col-reverse gap-2 border-t border-rule bg-surface px-4 py-3 sm:flex-row sm:justify-end"
    >
      <GgButton variant="secondary" @click="filters = { ...NO_FILTERS }">
        {{ t('topo.clearFilters') }}
      </GgButton>
      <GgButton @click="open = false">{{ t('topo.showResults', resultCount) }}</GgButton>
    </div>
  </GgDialog>
</template>
