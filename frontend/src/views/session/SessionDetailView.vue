<script setup lang="ts">
import { Pencil, Trash2 } from 'lucide-vue-next'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

import { useCurrentMember } from '@/api/auth'
import { ApiError, errorMessageKey } from '@/api/errors'
import { useDeleteSession, useSession } from '@/api/sessions'
import SubPageHeader from '@/components/layout/SubPageHeader.vue'
import SessionCard from '@/components/session/SessionCard.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'

/**
 * A session: one of mine, with what I can still do to it (spec §8.3, §9.3), or a friend's to read.
 */
const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const id = computed(() => (typeof route.params.id === 'string' ? route.params.id : undefined))
const session = useSession(id)
const member = useCurrentMember()
const isMine = computed(() => session.data.value?.memberId === member.value?.id)
const remove = useDeleteSession()
const confirming = ref(false)
const removeError = ref<string | null>(null)
/** Set by the session sheet when the session went through but not its photo. */
const photoRefused = computed(() => route.query.photo === 'refusee')
const notFound = computed(
  () => session.error.value instanceof ApiError && session.error.value.code === 'NOT_FOUND',
)

function confirmDelete() {
  if (id.value === undefined) return
  removeError.value = null
  remove.mutate(id.value, {
    onSuccess: () => void router.replace('/actu'),
    onError: (error) => {
      removeError.value = t(errorMessageKey(error))
    },
  })
}
</script>

<template>
  <SubPageHeader back="/actu" :back-label="t('session.backToActu')">
    {{ t('session.detailTitle') }}
  </SubPageHeader>

  <div v-if="session.isPending.value" class="flex flex-col gap-3 px-4 py-6" aria-hidden="true">
    <SkeletonBlock class="h-7 w-48" />
    <SkeletonBlock class="h-5 w-64" />
    <SkeletonBlock class="h-10 w-full" />
  </div>
  <EmptyState
    v-else-if="notFound"
    :title="t('session.notFoundTitle')"
    :body="t('session.notFoundBody')"
  >
    <GgButton variant="secondary" to="/actu">{{ t('session.backToActu') }}</GgButton>
  </EmptyState>
  <div v-else-if="session.isError.value" class="flex flex-col items-start gap-3 px-4 py-6">
    <FormAlert>{{ t(errorMessageKey(session.error.value)) }}</FormAlert>
    <GgButton variant="secondary" @click="session.refetch()">{{ t('common.retry') }}</GgButton>
  </div>
  <template v-else-if="session.data.value">
    <FormAlert v-if="photoRefused" class="mx-4 mt-4">{{ t('session.photoRefused') }}</FormAlert>
    <SessionCard :session="session.data.value" />
    <div v-if="session.data.value.editable" class="flex flex-wrap gap-2 px-4 py-4">
      <GgButton variant="secondary" :to="`/seances/${session.data.value.id}/modifier`">
        <Pencil :size="18" aria-hidden="true" />{{ t('session.edit') }}
      </GgButton>
      <GgButton variant="ghost" :disabled="remove.isPending.value" @click="confirming = true">
        <Trash2 :size="18" aria-hidden="true" />{{ t('session.delete') }}
      </GgButton>
    </div>
    <p v-else-if="isMine" class="px-4 py-4 text-ink-muted">{{ t('session.frozen') }}</p>
    <FormAlert v-if="removeError" class="mx-4">{{ removeError }}</FormAlert>
  </template>

  <ConfirmDialog
    v-model:open="confirming"
    :title="t('session.deleteTitle')"
    :body="t('session.deleteBody')"
    :confirm-label="t('session.deleteConfirm')"
    danger
    @confirm="confirmDelete"
  />
</template>
