<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { errorMessageKey } from '@/api/errors'
import { useSetClubFeed } from '@/api/feed'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'

/**
 * "Je participe au fil du club" (D-120), always after a confirmation that says it plainly: to see
 * the club feed, my "Tout le club" sessions show in it too.
 */
withDefaults(defineProps<{ variant?: 'primary' | 'secondary' }>(), { variant: 'primary' })
const emit = defineEmits<{ joined: [] }>()

const { t } = useI18n()
const setClubFeed = useSetClubFeed()
const confirming = ref(false)

function join() {
  setClubFeed.mutate(true, {
    onSuccess: () => {
      emit('joined')
    },
  })
}
</script>

<template>
  <div class="flex flex-col items-start gap-3">
    <GgButton :variant="variant" :disabled="setClubFeed.isPending.value" @click="confirming = true">
      {{ t('clubFeed.join') }}
    </GgButton>
    <FormAlert v-if="setClubFeed.isError.value">
      {{ t(errorMessageKey(setClubFeed.error.value)) }}
    </FormAlert>
  </div>
  <ConfirmDialog
    v-model:open="confirming"
    :title="t('clubFeed.confirmTitle')"
    :body="t('clubFeed.confirmBody')"
    :confirm-label="t('clubFeed.confirmLabel')"
    @confirm="join"
  />
</template>
