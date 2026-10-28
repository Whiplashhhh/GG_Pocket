<script setup lang="ts">
import { Trash2 } from 'lucide-vue-next'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { useModerateComment, useModeratedComments, type ModeratedComment } from '@/api/admin'
import { errorMessageKey } from '@/api/errors'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { formatDate, formatDateTime, formatRelative } from '@/lib/format'

import ManagementTitle from './ManagementTitle.vue'

/**
 * Spec §8.11: the latest comments on routes and sessions, newest first, for the board and the
 * admin. Deleting one hides it everywhere and goes to the audit log. A session stays its climber's
 * and their friends': only who, which day and the comment show here.
 */
const { t } = useI18n()
const moderation = useModeratedComments()
const remove = useModerateComment()
const comments = computed(() => moderation.data.value?.pages.flatMap((page) => page.comments) ?? [])

const deleting = ref<ModeratedComment | null>(null)
const confirmOpen = ref(false)
const notice = ref('')
const removeError = ref<unknown>(null)

function where(comment: ModeratedComment): string {
  if (comment.kind === 'ROUTE') {
    return t('moderation.onRoute', { route: comment.route?.name ?? '' })
  }
  return t('moderation.onSession', {
    climber: comment.climber?.nickname ?? '',
    day: comment.climbedOn === undefined ? '' : formatDate(comment.climbedOn),
  })
}

/** Kept after the confirmation closes: the dialog may close before it says "confirm". */
function askDelete(comment: ModeratedComment) {
  deleting.value = comment
  confirmOpen.value = true
}

function confirmDelete() {
  const comment = deleting.value
  if (comment === null) return
  notice.value = ''
  removeError.value = null
  remove.mutate(comment, {
    onSuccess: () => {
      notice.value = t('moderation.deleted', { nickname: comment.author.nickname })
    },
    onError: (error) => {
      removeError.value = error
    },
  })
}
</script>

<template>
  <ManagementTitle>{{ t('moderation.title') }}</ManagementTitle>
  <p class="px-4 pt-1 pb-4 text-ink-muted lg:px-0">{{ t('moderation.intro') }}</p>

  <p v-if="notice" role="status" class="px-4 pb-3 font-semibold text-success lg:px-0">
    {{ notice }}
  </p>
  <FormAlert v-if="removeError" class="mx-4 mb-3 lg:mx-0">{{
    t(errorMessageKey(removeError))
  }}</FormAlert>

  <div
    v-if="moderation.isPending.value"
    class="flex flex-col gap-3 px-4 lg:px-0"
    aria-hidden="true"
  >
    <SkeletonBlock v-for="index in 4" :key="index" class="h-16 w-full" />
  </div>
  <div v-else-if="moderation.isError.value" class="flex flex-col items-start gap-3 px-4 lg:px-0">
    <FormAlert>{{ t(errorMessageKey(moderation.error.value)) }}</FormAlert>
    <GgButton variant="secondary" @click="moderation.refetch()">{{ t('common.retry') }}</GgButton>
  </div>
  <EmptyState
    v-else-if="comments.length === 0"
    :title="t('moderation.emptyTitle')"
    :body="t('moderation.emptyBody')"
  />

  <template v-else>
    <ol class="border-t border-rule" :aria-label="t('moderation.listLabel')">
      <li
        v-for="comment in comments"
        :key="comment.id"
        class="flex gap-3 border-b border-rule py-3 pr-2 pl-4 lg:px-0"
      >
        <UserAvatar
          :first-name="comment.author.nickname"
          last-name=""
          :seed="comment.author.nickname"
          :src="comment.author.avatarUrl"
          :size="40"
        />
        <div class="min-w-0 flex-1">
          <p class="text-sm">
            <span class="font-semibold break-words">{{ comment.author.nickname }}</span>
            <span class="text-ink-muted" aria-hidden="true"> · </span>
            <time
              :datetime="comment.createdAt"
              :title="formatDateTime(comment.createdAt)"
              class="text-ink-muted"
              >{{ formatRelative(comment.createdAt) }}</time
            >
          </p>
          <p class="text-sm text-ink-muted">
            <RouterLink
              v-if="comment.kind === 'ROUTE' && comment.route"
              :to="`/topo/voies/${comment.route.id}`"
              class="underline-offset-2 hover:text-ink hover:underline"
              >{{ where(comment) }}</RouterLink
            >
            <template v-else>{{ where(comment) }}</template>
          </p>
          <p class="mt-1 break-words whitespace-pre-line">{{ comment.body }}</p>
        </div>
        <GgButton
          variant="ghost"
          class="shrink-0 self-start"
          :disabled="remove.isPending.value"
          :aria-label="t('moderation.deleteNamed', { nickname: comment.author.nickname })"
          @click="askDelete(comment)"
        >
          <Trash2 :size="18" aria-hidden="true" /><span class="hidden sm:inline">{{
            t('moderation.delete')
          }}</span>
        </GgButton>
      </li>
    </ol>
    <div v-if="moderation.hasNextPage.value" class="px-4 py-5 lg:px-0">
      <GgButton
        variant="secondary"
        :disabled="moderation.isFetchingNextPage.value"
        @click="moderation.fetchNextPage()"
      >
        {{ t('moderation.loadMore') }}
      </GgButton>
    </div>
  </template>

  <ConfirmDialog
    v-model:open="confirmOpen"
    :title="t('moderation.confirmTitle')"
    :body="t('moderation.confirmBody')"
    :confirm-label="t('moderation.delete')"
    danger
    @confirm="confirmDelete"
  />
</template>
