<script setup lang="ts">
import { ref, useId } from 'vue'
import { useI18n } from 'vue-i18n'

import { useAwaitingValidation, useCurrentMember } from '@/api/auth'
import { errorMessageKey } from '@/api/errors'
import { useSetClubFeed } from '@/api/feed'
import AwaitingValidationNotice from '@/components/member/AwaitingValidationNotice.vue'
import ClubFeedJoinButton from '@/components/social/ClubFeedJoinButton.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'

/**
 * "Je participe au fil du club" (D-120), saved at once like the favourite route. Joining is
 * confirmed (to see, one shows oneself); leaving is not: it only takes my "Tout le club" sessions
 * back to my friends.
 */
const { t } = useI18n()
const headingId = useId()
const member = useCurrentMember()
const awaiting = useAwaitingValidation()
const setClubFeed = useSetClubFeed()
const notice = ref<string | null>(null)

function leave() {
  notice.value = null
  setClubFeed.mutate(false, { onSuccess: () => (notice.value = t('clubFeed.left')) })
}
</script>

<template>
  <section class="flex flex-col items-start gap-3 px-4 pb-6" :aria-labelledby="headingId">
    <h2 :id="headingId" class="w-full border-t border-rule pt-5 text-lg font-title">
      {{ t('clubFeed.settingTitle') }}
    </h2>
    <AwaitingValidationNotice v-if="awaiting" class="w-full" :body="t('awaiting.clubFeed')" />
    <template v-else-if="member?.inClubFeed">
      <p class="max-w-prose text-ink-muted">{{ t('clubFeed.settingOn') }}</p>
      <GgButton variant="ghost" :disabled="setClubFeed.isPending.value" @click="leave">
        {{ t('clubFeed.leave') }}
      </GgButton>
    </template>
    <template v-else>
      <p class="max-w-prose text-ink-muted">{{ t('clubFeed.settingOff') }}</p>
      <ClubFeedJoinButton variant="secondary" @joined="notice = t('clubFeed.joined')" />
    </template>
    <p v-if="notice" role="status" class="font-semibold text-success">{{ notice }}</p>
    <FormAlert v-if="setClubFeed.isError.value">
      {{ t(errorMessageKey(setClubFeed.error.value)) }}
    </FormAlert>
  </section>
</template>
