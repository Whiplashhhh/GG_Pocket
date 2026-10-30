<script setup lang="ts">
import { CloudOff, TriangleAlert } from 'lucide-vue-next'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import { useCurrentMember } from '@/api/auth'
import { errorMessageKey, SERVER_ERROR_CODES, type ServerErrorCode } from '@/api/errors'
import GgButton from '@/components/ui/GgButton.vue'
import { draftKey } from '@/composables/useSessionDraft'
import { localStore } from '@/lib/localStore'
import { formatDate } from '@/lib/format'
import { requestToForm } from '@/lib/sessionForm'
import type { SessionDraft } from '@/composables/useSessionDraft'
import type { OutboxItem } from '@/lib/outbox'
import { useOutboxStore } from '@/stores/outbox'

/**
 * Says what the offline queue holds (spec §8.3): sessions waiting for the network, and the ones
 * the server refused, with a way out for each.
 */
const { t } = useI18n()
const router = useRouter()
const outbox = useOutboxStore()
const me = useCurrentMember()

const waitingCount = computed(() => outbox.waiting.length)

function reason(item: OutboxItem): string {
  const code = item.errorCode
  const known = SERVER_ERROR_CODES.includes(code as ServerErrorCode)
  return t(known ? `errors.${code as ServerErrorCode}` : errorMessageKey(undefined))
}

/** Back into the session sheet, as the draft of a new session, to fix and send again. */
async function reopen(item: OutboxItem) {
  const memberId = me.value?.id
  if (memberId === undefined) return
  const taken = await outbox.takeBack(item.clientRef)
  if (taken === undefined) return
  const draft: SessionDraft = { form: requestToForm(taken.body), savedAt: new Date().toISOString() }
  if (taken.photo !== undefined) draft.photo = taken.photo
  await localStore<SessionDraft>('drafts').set(draftKey(memberId), draft)
  await router.push('/seance/nouvelle')
}
</script>

<template>
  <div v-if="outbox.items.length > 0" class="flex flex-col">
    <p
      v-if="waitingCount > 0"
      role="status"
      class="flex items-center gap-2 border-b border-rule bg-surface px-4 py-3 font-semibold"
    >
      <CloudOff :size="20" class="shrink-0" aria-hidden="true" />
      {{ t('outbox.waiting', waitingCount) }}
    </p>
    <div
      v-for="item in outbox.problems"
      :key="item.clientRef"
      role="alert"
      class="flex flex-col gap-2 border-b border-rule bg-surface px-4 py-3"
    >
      <p class="flex items-start gap-2 font-semibold text-danger">
        <TriangleAlert :size="20" class="mt-0.5 shrink-0" aria-hidden="true" />
        <span>
          {{
            item.state === 'photoRefused'
              ? t('outbox.photoRefused', { date: formatDate(item.body.climbedOn) })
              : t('outbox.refused', { date: formatDate(item.body.climbedOn) })
          }}
          {{ reason(item) }}
        </span>
      </p>
      <div class="flex flex-wrap gap-2">
        <template v-if="item.state === 'refused'">
          <GgButton variant="secondary" @click="reopen(item)">{{ t('outbox.reopen') }}</GgButton>
          <GgButton variant="ghost" @click="outbox.dismiss(item.clientRef)">
            {{ t('outbox.discard') }}
          </GgButton>
        </template>
        <template v-else>
          <GgButton
            v-if="item.sessionId"
            variant="secondary"
            :to="`/seances/${item.sessionId}`"
            @click="outbox.dismiss(item.clientRef)"
          >
            {{ t('outbox.seeSession') }}
          </GgButton>
          <GgButton variant="ghost" @click="outbox.dismiss(item.clientRef)">
            {{ t('outbox.ok') }}
          </GgButton>
        </template>
      </div>
    </div>
  </div>
</template>
