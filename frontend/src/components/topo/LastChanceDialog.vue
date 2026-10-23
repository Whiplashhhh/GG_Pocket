<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { errorMessageKey } from '@/api/errors'
import { useAcknowledgeLastChance, useUnacknowledgedLastChance } from '@/api/topo'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import GgDialog from '@/components/ui/GgDialog.vue'
import { formatDate } from '@/lib/format'

import RouteSummaryLabel from './RouteSummaryLabel.vue'

/**
 * Spec §8.9: when the app opens, the routes in last chance I have not seen yet. « J'accepte
 * leurs destins » records it; the pop-up comes back only for routes announced afterwards.
 * Put aside for now, it comes back the next time the app opens.
 */
const props = defineProps<{ enabled: boolean }>()

const { t } = useI18n()
const unacknowledged = useUnacknowledgedLastChance(() => props.enabled)
const acknowledge = useAcknowledgeLastChance()
const postponed = ref(false)

const routes = computed(() => unacknowledged.data.value ?? [])
const open = computed({
  get: () => props.enabled && !postponed.value && routes.value.length > 0,
  set: (isOpen: boolean) => {
    if (!isOpen) postponed.value = true
  },
})

function accept() {
  acknowledge.mutate(routes.value.map((route) => route.id))
}
</script>

<template>
  <GgDialog v-model:open="open" :title="t('lastChance.title')" :description="t('lastChance.body')">
    <ul class="divide-y divide-rule border-y border-rule">
      <li
        v-for="route in routes"
        :key="route.id"
        class="flex flex-col items-start gap-1.5 pt-4 pb-3"
      >
        <RouteSummaryLabel :route="route" />
        <p class="text-sm text-ink-muted">
          {{
            route.plannedDismountOn
              ? t('lastChance.planned', { date: formatDate(route.plannedDismountOn) })
              : t('lastChance.noDate')
          }}
          · {{ t('lastChance.favorites', route.favoriteCount) }}
        </p>
      </li>
    </ul>
    <div v-if="acknowledge.isError.value" class="mt-4">
      <FormAlert>{{ t(errorMessageKey(acknowledge.error.value)) }}</FormAlert>
    </div>
    <div class="mt-5 flex flex-col gap-2">
      <GgButton size="lg" block :disabled="acknowledge.isPending.value" @click="accept">
        {{ t('lastChance.accept') }}
      </GgButton>
      <GgButton variant="ghost" block @click="open = false">{{ t('lastChance.later') }}</GgButton>
    </div>
  </GgDialog>
</template>
