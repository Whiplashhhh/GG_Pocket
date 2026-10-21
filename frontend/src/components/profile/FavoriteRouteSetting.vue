<script setup lang="ts">
import { computed, ref, useId, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import { errorMessageKey } from '@/api/errors'
import { useMyProfile } from '@/api/me'
import { useRouteDetail, useSetFavoriteRoute } from '@/api/topo'
import RoutePicker from '@/components/topo/RoutePicker.vue'
import RouteSummaryLabel from '@/components/topo/RouteSummaryLabel.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'

/** My favourite route (D-055), saved as soon as it is picked, like the photo. */
const { t } = useI18n()
const profile = useMyProfile()
const setFavoriteRoute = useSetFavoriteRoute()
const headingId = useId()
const labelId = useId()

const currentId = computed(() => profile.data.value?.favoriteRouteId)
const current = useRouteDetail(currentId)
const picking = ref(false)
const choice = ref<string | undefined>(undefined)
const notice = ref('')

watch(choice, (routeId) => {
  if (routeId === undefined || routeId === currentId.value) return
  notice.value = ''
  setFavoriteRoute.mutate(routeId, {
    onSuccess: () => {
      notice.value = t('favoriteRoute.saved')
      picking.value = false
      choice.value = undefined
    },
  })
})

function clear() {
  notice.value = ''
  setFavoriteRoute.mutate(undefined, {
    onSuccess: () => {
      notice.value = t('favoriteRoute.cleared')
    },
  })
}
</script>

<template>
  <section class="flex flex-col items-start gap-3 px-4 pb-6" :aria-labelledby="headingId">
    <h2 :id="headingId" class="w-full border-t border-rule pt-5 text-lg font-title">
      {{ t('profileEdit.favoriteRoute') }}
    </h2>
    <template v-if="currentId">
      <SkeletonBlock v-if="current.isPending.value" class="h-11 w-full max-w-72" />
      <RouterLink
        v-else-if="current.data.value"
        :to="`/topo/voies/${currentId}`"
        :aria-labelledby="labelId"
        class="flex max-w-full pt-2"
      >
        <RouteSummaryLabel :route="current.data.value.route" :label-id="labelId" />
      </RouterLink>
      <p v-if="current.data.value?.route.status === 'DISMOUNTED'" class="text-sm text-ink-muted">
        {{ t('favoriteRoute.dismounted') }}
      </p>
    </template>
    <p v-else class="text-ink-muted">{{ t('favoriteRoute.none') }}</p>

    <div class="flex flex-wrap gap-2">
      <GgButton variant="secondary" :aria-expanded="picking" @click="picking = !picking">
        {{ currentId ? t('favoriteRoute.change') : t('favoriteRoute.choose') }}
      </GgButton>
      <GgButton
        v-if="currentId"
        variant="ghost"
        :disabled="setFavoriteRoute.isPending.value"
        @click="clear"
      >
        {{ t('favoriteRoute.clear') }}
      </GgButton>
    </div>
    <div v-if="picking" class="w-full"><RoutePicker v-model="choice" /></div>
    <p v-if="notice" role="status" class="font-semibold text-success">{{ notice }}</p>
    <FormAlert v-if="setFavoriteRoute.isError.value">
      {{ t(errorMessageKey(setFavoriteRoute.error.value)) }}
    </FormAlert>
  </section>
</template>
