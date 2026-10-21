<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import type { RouteSummary } from '@/api/topo'
import { useAnnounceLastChance, useDismountRoute, useRemountRoute } from '@/api/topoAdmin'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import GgButton from '@/components/ui/GgButton.vue'
import GgDialog from '@/components/ui/GgDialog.vue'
import TextField from '@/components/ui/TextField.vue'
import { clubToday } from '@/lib/age'
import { focusFirstInvalid, serverFieldErrors } from '@/lib/forms'
import type { RouteStatusAction } from '@/lib/route'

import RouteSummaryLabel from './RouteSummaryLabel.vue'

/**
 * The status changes of a route in the management space (spec §8.9, §8.10): last chance with
 * an optional date, dismount (with the favourites warning), remount. The screen says which one
 * through `action`; the outcome comes back as a sentence to show, or as an error.
 */
const emit = defineEmits<{ done: [message: string]; failed: [error: unknown] }>()
const action = defineModel<RouteStatusAction | null>('action', { required: true })

const { t } = useI18n()
const announce = useAnnounceLastChance()
const dismount = useDismountRoute()
const remount = useRemountRoute()

/** Kept while a dialog closes, so its title does not go blank during the animation. */
const target = ref<RouteSummary | null>(null)
const plannedOn = ref('')
const plannedError = ref<string | undefined>(undefined)
const today = ref(clubToday())

watch(
  action,
  (next) => {
    if (next === null) return
    target.value = next.route
    today.value = clubToday()
    plannedOn.value = next.route.plannedDismountOn ?? ''
    plannedError.value = undefined
  },
  { immediate: true },
)

function openFor(kind: RouteStatusAction['kind']) {
  return computed({
    get: () => action.value?.kind === kind,
    set: (open: boolean) => {
      if (!open) action.value = null
    },
  })
}
const lastChanceOpen = openFor('lastChance')
const dismountOpen = openFor('dismount')
const remountOpen = openFor('remount')

const name = computed(() => target.value?.name ?? '')
const dismountBody = computed(() => {
  const favorites = target.value?.favoriteCount ?? 0
  const body = t('routeStatus.dismountBody')
  return favorites > 0 ? `${t('routeStatus.dismountWarning', favorites)} ${body}` : body
})

function announceLastChance() {
  const route = target.value
  if (route === null) return
  if (plannedOn.value !== '' && plannedOn.value < clubToday()) {
    plannedError.value = t('errors.DATE_IN_THE_PAST')
    focusFirstInvalid()
    return
  }
  announce.mutate(
    { id: route.id, plannedDismountOn: plannedOn.value === '' ? undefined : plannedOn.value },
    {
      onSuccess: () => {
        action.value = null
        emit('done', t('routeStatus.announced', { name: route.name }))
      },
      onError: (error) => {
        const { fields } = serverFieldErrors(
          error,
          ['plannedDismountOn'],
          {},
          {
            DATE_IN_THE_PAST: 'plannedDismountOn',
          },
        )
        if (fields.plannedDismountOn) {
          plannedError.value = t(fields.plannedDismountOn.key)
          focusFirstInvalid()
          return
        }
        action.value = null
        emit('failed', error)
      },
    },
  )
}

function confirmDismount() {
  const route = target.value
  if (route === null) return
  dismount.mutate(route.id, {
    onSuccess: () => {
      emit('done', t('routeStatus.dismounted', { name: route.name }))
    },
    onError: (error) => {
      emit('failed', error)
    },
  })
}

function confirmRemount() {
  const route = target.value
  if (route === null) return
  remount.mutate(route.id, {
    onSuccess: () => {
      emit('done', t('routeStatus.remounted', { name: route.name }))
    },
    onError: (error) => {
      emit('failed', error)
    },
  })
}
</script>

<template>
  <GgDialog
    v-model:open="lastChanceOpen"
    :title="t('routeStatus.lastChanceTitle', { name })"
    :description="t('routeStatus.lastChanceBody')"
  >
    <form class="flex flex-col gap-5" novalidate @submit.prevent="announceLastChance">
      <RouteSummaryLabel v-if="target" :route="target" />
      <TextField
        v-model="plannedOn"
        type="date"
        :label="t('routeStatus.plannedDismountOn')"
        :hint="t('routeStatus.plannedDismountHint')"
        :min="today"
        :error="plannedError"
        optional
      />
      <div class="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
        <GgButton variant="secondary" @click="lastChanceOpen = false">
          {{ t('common.cancel') }}
        </GgButton>
        <GgButton type="submit" :disabled="announce.isPending.value">
          {{ t('routeStatus.announce') }}
        </GgButton>
      </div>
    </form>
  </GgDialog>

  <ConfirmDialog
    v-model:open="dismountOpen"
    :title="t('routeStatus.dismountTitle', { name })"
    :body="dismountBody"
    :confirm-label="t('routeStatus.dismountConfirm')"
    danger
    @confirm="confirmDismount"
  />

  <ConfirmDialog
    v-model:open="remountOpen"
    :title="t('routeStatus.remountTitle', { name })"
    :body="t('routeStatus.remountBody')"
    :confirm-label="t('routeStatus.remountConfirm')"
    @confirm="confirmRemount"
  />
</template>
