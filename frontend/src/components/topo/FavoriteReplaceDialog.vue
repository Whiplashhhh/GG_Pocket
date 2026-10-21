<script setup lang="ts">
import { useId } from 'vue'
import { useI18n } from 'vue-i18n'

import type { RouteSummary } from '@/api/topo'
import GgButton from '@/components/ui/GgButton.vue'
import GgDialog from '@/components/ui/GgDialog.vue'

import RouteSummaryLabel from './RouteSummaryLabel.vue'

/** Spec §8.4: « Ton cœur est plein. Lequel tu lâches ? », with my favourites on the wall. */
defineProps<{
  /** Name of the route that wants a place. */
  routeName: string
  favorites: readonly RouteSummary[]
}>()

const emit = defineEmits<{ release: [routeId: string] }>()
const open = defineModel<boolean>('open', { required: true })

const { t } = useI18n()
const baseId = useId()
</script>

<template>
  <GgDialog
    v-model:open="open"
    :title="t('routeSheet.heartFullTitle')"
    :description="t('routeSheet.heartFullBody', { name: routeName })"
  >
    <ul class="flex flex-col gap-2">
      <li v-for="favorite in favorites" :key="favorite.id">
        <button
          type="button"
          class="flex min-h-16 w-full items-center gap-3 rounded-control border-2 border-rule px-3 pt-3 pb-2 text-left hover:border-ink"
          :aria-labelledby="`${baseId}-${favorite.id}-release ${baseId}-${favorite.id}-label`"
          @click="emit('release', favorite.id)"
        >
          <span class="flex min-w-0 flex-1">
            <RouteSummaryLabel :route="favorite" :label-id="`${baseId}-${favorite.id}-label`" />
          </span>
          <span :id="`${baseId}-${favorite.id}-release`" class="shrink-0 font-semibold text-danger">
            {{ t('routeSheet.heartFullRelease') }}
          </span>
        </button>
      </li>
    </ul>
    <GgButton variant="secondary" block class="mt-4" @click="open = false">
      {{ t('routeSheet.heartFullKeep') }}
    </GgButton>
  </GgDialog>
</template>
