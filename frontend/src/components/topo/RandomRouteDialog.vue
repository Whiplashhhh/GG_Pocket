<script setup lang="ts">
import { Shuffle } from 'lucide-vue-next'
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter, type RouteLocationRaw } from 'vue-router'

import type { RouteSummary } from '@/api/topo'
import GgButton from '@/components/ui/GgButton.vue'
import GgDialog from '@/components/ui/GgDialog.vue'
import { pickRandomRoute } from '@/lib/topo'

import RouteRow from './RouteRow.vue'

/**
 * « Je sais pas quoi grimper » (club feedback, D-111): a route drawn among the ones the list shows
 * (its filters apply), drawn again as long as it does not tempt, then opened.
 */
const props = defineProps<{
  routes: readonly RouteSummary[]
  linkFor: (route: RouteSummary) => RouteLocationRaw
}>()

const open = defineModel<boolean>('open', { required: true })

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const picked = ref<RouteSummary>()

function draw() {
  picked.value = pickRandomRoute(props.routes, picked.value?.id)
}

watch(open, (opened) => {
  if (opened) draw()
})
// Following the row's link (the route sheet) closes the dialog behind it.
watch(
  () => route.fullPath,
  () => {
    open.value = false
  },
)

function go() {
  if (picked.value === undefined) return
  open.value = false
  void router.push(props.linkFor(picked.value))
}
</script>

<template>
  <GgDialog
    v-model:open="open"
    :title="t('topo.shuffleTitle')"
    :description="t('topo.shuffleFrom', routes.length)"
    :initial-focus="['[data-shuffle-again]', '[data-shuffle-go]']"
  >
    <div aria-live="polite" class="-mx-4 border-y border-rule" data-testid="random-route">
      <div v-if="picked" :key="picked.id" class="motion-safe:animate-pop">
        <RouteRow :route="picked" :to="linkFor(picked)" />
      </div>
    </div>
    <div class="mt-5 grid grid-cols-2 gap-2">
      <GgButton
        v-if="routes.length > 1"
        variant="secondary"
        class="w-full"
        data-shuffle-again
        @click="draw"
      >
        <Shuffle :size="18" aria-hidden="true" />
        {{ t('topo.shuffleAgain') }}
      </GgButton>
      <GgButton
        :class="routes.length > 1 ? 'w-full' : 'col-span-2 w-full'"
        data-shuffle-go
        :disabled="picked === undefined"
        @click="go"
      >
        {{ t('topo.shuffleGo') }}
      </GgButton>
    </div>
    <div class="mt-2 flex justify-center">
      <GgButton variant="ghost" @click="open = false">{{ t('common.close') }}</GgButton>
    </div>
  </GgDialog>
</template>
