<script setup lang="ts">
import { ChevronDown, ChevronUp, Pencil, Plus } from 'lucide-vue-next'
import { computed, nextTick, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import { errorMessageKey } from '@/api/errors'
import { useTopoConfig, type Cord, type HoldColorConfig, type Sector } from '@/api/topo'
import { useReorderSectors } from '@/api/topoAdmin'
import CordDialog from '@/components/club/CordDialog.vue'
import HoldColorDialog from '@/components/club/HoldColorDialog.vue'
import SectorDialog from '@/components/club/SectorDialog.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import GgIconButton from '@/components/ui/GgIconButton.vue'
import HoldSwatch from '@/components/ui/HoldSwatch.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import { moveSector, orderedSectors, suggestedCordNumber, withSectorOrder } from '@/lib/clubConfig'
import { gradeChoices } from '@/lib/routeForm'

import ManagementTitle from './ManagementTitle.vue'

/**
 * Spec §8.11, admin only: the sectors of the wall in their order, their cords, the hold colours.
 * Nothing is deleted (cords and colours are deactivated) so old routes keep their history.
 * Grades are fixed (DECISIONS D-074): shown, not edited.
 */
const { t } = useI18n()
const config = useTopoConfig()
const reorder = useReorderSectors()

const sectors = computed(() => (config.data.value ? orderedSectors(config.data.value) : []))
const colors = computed(() =>
  [...(config.data.value?.holdColors ?? [])].sort((a, b) => a.position - b.position),
)
const grades = computed(() => (config.data.value ? gradeChoices(config.data.value) : []))

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

const sectorOpen = ref(false)
const editedSector = ref<Sector | null>(null)
function editSector(sector: Sector | null) {
  editedSector.value = sector
  sectorOpen.value = true
}

const cordOpen = ref(false)
const editedCord = ref<Cord | null>(null)
const cordSectorId = ref('')
function editCord(cord: Cord | null, sectorId: string) {
  editedCord.value = cord
  cordSectorId.value = sectorId
  cordOpen.value = true
}
const nextCordNumber = computed(() =>
  config.data.value ? suggestedCordNumber(config.data.value) : 1,
)

const colorOpen = ref(false)
const editedColor = ref<HoldColorConfig | null>(null)
function editColor(color: HoldColorConfig | null) {
  editedColor.value = color
  colorOpen.value = true
}

const moveButtonId = (sector: Sector, offset: -1 | 1) =>
  `${offset === -1 ? 'monter' : 'descendre'}-${sector.id}`

/** The button to give the focus back to once the moved sector is drawn at its new place. */
const focusAfterMove = ref<string | null>(null)
watch(
  () => sectors.value.map((sector) => sector.id).join(),
  () => {
    const id = focusAfterMove.value
    focusAfterMove.value = null
    if (id !== null) void nextTick(() => document.getElementById(id)?.focus())
  },
  { flush: 'post' },
)

function move(sector: Sector, offset: -1 | 1) {
  const current = config.data.value
  if (current === undefined) return
  const order = moveSector(sectors.value, sector.id, offset)
  if (order === null) return
  const position = order.indexOf(sector.id) + 1
  // At an end, the button just used is disabled: the other one takes the focus.
  const atEnd = (offset === -1 && position === 1) || (offset === 1 && position === order.length)
  focusAfterMove.value = moveButtonId(sector, atEnd ? (offset === -1 ? 1 : -1) : offset)
  reorder.mutate(
    { sectorIds: order, preview: withSectorOrder(current, order) },
    {
      onSuccess: () => {
        say(t('clubConfig.moved', { name: sector.name, position }))
      },
      onError: fail,
    },
  )
}
</script>

<template>
  <ManagementTitle>{{ t('clubConfig.title') }}</ManagementTitle>
  <p class="max-w-prose px-4 pt-1 pb-4 text-ink-muted lg:px-0">{{ t('clubConfig.intro') }}</p>
  <div class="flex flex-col gap-3 px-4 pb-4 lg:px-0" aria-live="polite">
    <FormAlert v-if="notice" tone="success">{{ notice }}</FormAlert>
    <FormAlert v-if="failure">{{ t(errorMessageKey(failure)) }}</FormAlert>
  </div>

  <div v-if="config.isPending.value" class="flex flex-col gap-3 px-4 lg:px-0" aria-hidden="true">
    <SkeletonBlock v-for="index in 4" :key="index" class="h-16 w-full" />
  </div>
  <div v-else-if="config.isError.value" class="flex flex-col items-start gap-3 px-4 lg:px-0">
    <FormAlert>{{ t(errorMessageKey(config.error.value)) }}</FormAlert>
    <GgButton variant="secondary" @click="config.refetch()">{{ t('common.retry') }}</GgButton>
  </div>

  <div
    v-else
    class="flex flex-col gap-10 px-4 pb-10 lg:grid lg:grid-cols-[minmax(0,1fr)_minmax(0,1fr)] lg:items-start lg:px-0"
  >
    <section aria-labelledby="secteurs" class="flex flex-col gap-2">
      <div class="flex flex-wrap items-center justify-between gap-2">
        <h2 id="secteurs" class="text-lg font-title">{{ t('clubConfig.sectors') }}</h2>
        <GgButton variant="secondary" @click="editSector(null)">
          <Plus :size="20" aria-hidden="true" />
          {{ t('clubConfig.addSector') }}
        </GgButton>
      </div>
      <p class="text-sm text-ink-muted">{{ t('clubConfig.sectorsHint') }}</p>
      <ol class="mt-1 border-t border-rule">
        <li
          v-for="(sector, index) in sectors"
          :key="sector.id"
          class="flex flex-col gap-2 border-b border-rule py-3"
        >
          <div class="flex items-start gap-1">
            <span
              class="w-8 shrink-0 pt-2 text-2xl font-cord text-ink-muted tabular-nums"
              aria-hidden="true"
              >{{ index + 1 }}</span
            >
            <div class="min-w-0 flex-1 pt-2">
              <h3 class="font-semibold">{{ sector.name }}</h3>
              <p v-if="sector.description" class="text-sm text-ink-muted">
                {{ sector.description }}
              </p>
            </div>
            <GgIconButton
              :id="moveButtonId(sector, -1)"
              :label="t('clubConfig.moveUp', { name: sector.name })"
              :disabled="index === 0"
              @click="move(sector, -1)"
            >
              <ChevronUp :size="20" aria-hidden="true" />
            </GgIconButton>
            <GgIconButton
              :id="moveButtonId(sector, 1)"
              :label="t('clubConfig.moveDown', { name: sector.name })"
              :disabled="index === sectors.length - 1"
              @click="move(sector, 1)"
            >
              <ChevronDown :size="20" aria-hidden="true" />
            </GgIconButton>
            <GgIconButton
              :label="t('clubConfig.editSectorNamed', { name: sector.name })"
              @click="editSector(sector)"
            >
              <Pencil :size="20" aria-hidden="true" />
            </GgIconButton>
          </div>
          <ul
            :aria-label="t('clubConfig.cordsOf', { name: sector.name })"
            class="flex flex-wrap gap-2 pl-9"
          >
            <li v-for="cord in sector.cords" :key="cord.id">
              <button
                type="button"
                class="inline-flex min-h-11 min-w-11 items-center justify-center rounded-control border-2 px-2 text-xl font-cord hover:bg-surface-sunken"
                :class="
                  cord.active
                    ? 'border-ink text-ink'
                    : 'border-rule bg-surface-sunken text-ink-muted line-through'
                "
                :aria-label="
                  t(cord.active ? 'clubConfig.editCordNamed' : 'clubConfig.editCordInactiveNamed', {
                    number: cord.number,
                  })
                "
                @click="editCord(cord, sector.id)"
              >
                {{ cord.number }}
              </button>
            </li>
            <li>
              <button
                type="button"
                class="inline-flex min-h-11 items-center gap-1 rounded-control border-2 border-dashed border-rule px-3 font-semibold text-ink hover:border-ink"
                :aria-label="t('clubConfig.addCordTo', { name: sector.name })"
                @click="editCord(null, sector.id)"
              >
                <Plus :size="18" aria-hidden="true" />
                {{ t('clubConfig.addCord') }}
              </button>
            </li>
          </ul>
        </li>
      </ol>
    </section>

    <div class="flex flex-col gap-10">
      <section aria-labelledby="couleurs" class="flex flex-col gap-2">
        <div class="flex flex-wrap items-center justify-between gap-2">
          <h2 id="couleurs" class="text-lg font-title">{{ t('clubConfig.colors') }}</h2>
          <GgButton variant="secondary" @click="editColor(null)">
            <Plus :size="20" aria-hidden="true" />
            {{ t('clubConfig.addColor') }}
          </GgButton>
        </div>
        <p class="text-sm text-ink-muted">{{ t('clubConfig.colorsHint') }}</p>
        <ul class="mt-1 border-t border-rule">
          <li
            v-for="color in colors"
            :key="color.id"
            class="flex items-center gap-3 border-b border-rule py-1.5"
          >
            <HoldSwatch :color="color" :size="32" :class="{ 'opacity-50': !color.active }" />
            <span class="flex min-w-0 flex-1 flex-wrap items-baseline gap-x-2">
              <span class="font-semibold" :class="{ 'text-ink-muted': !color.active }">{{
                color.label
              }}</span>
              <span class="text-sm text-ink-muted tabular-nums">{{ color.hex }}</span>
              <span
                v-if="!color.active"
                class="rounded-control border border-rule px-1.5 text-micro font-semibold text-ink-muted"
                >{{ t('clubConfig.inactive') }}</span
              >
            </span>
            <GgIconButton
              :label="t('clubConfig.editColorNamed', { label: color.label })"
              @click="editColor(color)"
            >
              <Pencil :size="20" aria-hidden="true" />
            </GgIconButton>
          </li>
        </ul>
      </section>

      <section aria-labelledby="cotations" class="flex flex-col gap-2">
        <h2 id="cotations" class="text-lg font-title">{{ t('clubConfig.grades') }}</h2>
        <p class="max-w-prose text-sm text-ink-muted">{{ t('clubConfig.gradesFixed') }}</p>
        <ul class="flex flex-wrap gap-x-3 gap-y-1 text-lg font-grade">
          <li v-for="grade in grades" :key="grade.id">{{ grade.code }}</li>
        </ul>
      </section>
    </div>
  </div>

  <SectorDialog v-model:open="sectorOpen" :sector="editedSector" @saved="say" />
  <CordDialog
    v-model:open="cordOpen"
    :cord="editedCord"
    :sector-id="cordSectorId"
    :suggested-number="nextCordNumber"
    :sectors="sectors"
    @saved="say"
  />
  <HoldColorDialog v-model:open="colorOpen" :color="editedColor" @saved="say" />
</template>
