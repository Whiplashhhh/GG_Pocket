<script setup lang="ts">
import { Trash2 } from 'lucide-vue-next'
import { computed, ref, useId } from 'vue'
import { useI18n } from 'vue-i18n'

import { errorMessageKey } from '@/api/errors'
import { useAddSessionComment, useDeleteSessionComment, useSessionComments } from '@/api/feed'
import { useAddComment, useDeleteComment, useRouteComments, type RouteComment } from '@/api/topo'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import GgIconButton from '@/components/ui/GgIconButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { formatDateTime, formatRelative } from '@/lib/format'

/**
 * Comments of a route sheet or of a session: oldest first, mine (or anyone's for the board, as the
 * server says with `deletable`) can be deleted. A dismounted route takes no new comment, but its
 * comments can still be deleted (D-081). The target never changes for a given thread.
 */
const props = defineProps<{
  target: { kind: 'route' | 'session'; id: string }
  readOnly: boolean
  count: number
}>()

/** A comment on a route or on a session: the same shape. */
type ThreadComment = RouteComment

const MAX_LENGTH = 500

const { t } = useI18n()
const targetId = () => props.target.id
const { comments, add, remove } =
  props.target.kind === 'route'
    ? {
        comments: useRouteComments(targetId),
        add: useAddComment(targetId),
        remove: useDeleteComment(targetId),
      }
    : {
        comments: useSessionComments(targetId),
        add: useAddSessionComment(targetId),
        remove: useDeleteSessionComment(targetId),
      }

const headingId = useId()
const fieldId = useId()
const body = ref('')
const invalid = ref(false)
const sendError = ref<unknown>(null)
const deleting = ref<ThreadComment | null>(null)
const notice = ref('')

const confirmOpen = ref(false)

/** What differs between a route sheet and a session. */
const texts = computed(() =>
  props.target.kind === 'session'
    ? {
        empty: t('comments.session.empty'),
        placeholder: t('comments.session.placeholder'),
        deleteConfirmBody: t('comments.session.deleteConfirmBody'),
      }
    : {
        empty: t('comments.empty'),
        placeholder: t('comments.placeholder'),
        deleteConfirmBody: t('comments.deleteConfirmBody'),
      },
)

const shownCount = computed(() => comments.data.value?.length ?? props.count)

/** Kept after the confirmation closes: the dialog may close before it says "confirm". */
function askDelete(comment: ThreadComment) {
  deleting.value = comment
  confirmOpen.value = true
}

function submit() {
  const text = body.value.trim()
  sendError.value = null
  notice.value = ''
  invalid.value = text === ''
  if (invalid.value) {
    document.getElementById(fieldId)?.focus()
    return
  }
  add.mutate(text, {
    onSuccess: () => {
      body.value = ''
    },
    onError: (error) => {
      sendError.value = error
    },
  })
}

function confirmDelete() {
  const comment = deleting.value
  if (comment === null) return
  notice.value = ''
  remove.mutate(comment.id, {
    onSuccess: () => {
      notice.value = t('comments.deleted')
    },
    onError: (error) => {
      sendError.value = error
    },
  })
}
</script>

<template>
  <section :aria-labelledby="headingId">
    <h3 :id="headingId" class="px-4 text-lg font-title">
      {{ t('comments.title', { count: shownCount }) }}
    </h3>

    <div v-if="comments.isPending.value" class="flex flex-col gap-3 px-4 py-3" aria-hidden="true">
      <SkeletonBlock class="h-12 w-full" />
      <SkeletonBlock class="h-12 w-2/3" />
    </div>
    <div v-else-if="comments.isError.value" class="flex flex-col items-start gap-3 px-4 py-3">
      <FormAlert
        >{{ t('comments.loadError') }} {{ t(errorMessageKey(comments.error.value)) }}</FormAlert
      >
      <GgButton variant="secondary" @click="comments.refetch()">{{ t('common.retry') }}</GgButton>
    </div>
    <p v-else-if="comments.data.value?.length === 0" class="px-4 py-3 text-ink-muted">
      {{ texts.empty }}
    </p>
    <ul v-else class="mt-2 divide-y divide-rule border-y border-rule">
      <li
        v-for="comment in comments.data.value"
        :key="comment.id"
        class="flex gap-3 py-3 pr-2 pl-4"
      >
        <UserAvatar
          :first-name="comment.author.nickname"
          last-name=""
          :seed="comment.author.nickname"
          :src="comment.author.avatarUrl"
          :size="36"
        />
        <div class="min-w-0 flex-1">
          <p class="text-sm">
            <span class="font-semibold">{{ comment.author.nickname }}</span>
            <span class="text-ink-muted" aria-hidden="true"> · </span>
            <time
              :datetime="comment.createdAt"
              :title="formatDateTime(comment.createdAt)"
              class="text-ink-muted"
              >{{ formatRelative(comment.createdAt) }}</time
            >
          </p>
          <p class="mt-0.5 break-words whitespace-pre-line">{{ comment.body }}</p>
        </div>
        <GgIconButton
          v-if="comment.deletable"
          :label="t('comments.deleteNamed', { nickname: comment.author.nickname })"
          class="shrink-0 text-ink-muted"
          @click="askDelete(comment)"
        >
          <Trash2 :size="20" aria-hidden="true" />
        </GgIconButton>
      </li>
    </ul>

    <p v-if="notice" role="status" class="px-4 pt-3 text-sm font-semibold text-success">
      {{ notice }}
    </p>

    <form
      v-if="!readOnly"
      class="flex flex-col gap-1.5 px-4 pt-4"
      novalidate
      @submit.prevent="submit"
    >
      <label :for="fieldId" class="font-semibold">{{ t('comments.label') }}</label>
      <textarea
        :id="fieldId"
        v-model="body"
        rows="3"
        :maxlength="MAX_LENGTH"
        :placeholder="texts.placeholder"
        :aria-invalid="invalid ? true : undefined"
        :aria-describedby="`${fieldId}-counter${invalid ? ` ${fieldId}-error` : ''}`"
        class="w-full rounded-control border-2 border-rule bg-surface px-3 py-2 text-base text-ink placeholder:text-ink-muted focus:border-ink focus:outline-none aria-invalid:border-danger"
        @input="invalid = false"
      />
      <p v-if="invalid" :id="`${fieldId}-error`" class="text-sm font-semibold text-danger">
        {{ t('comments.required') }}
      </p>
      <div class="flex items-center justify-between gap-3">
        <p :id="`${fieldId}-counter`" class="text-sm text-ink-muted">
          {{ t('comments.counter', { count: body.length, max: MAX_LENGTH }) }}
        </p>
        <GgButton type="submit" :disabled="add.isPending.value">{{ t('comments.send') }}</GgButton>
      </div>
      <FormAlert v-if="sendError">{{ t(errorMessageKey(sendError)) }}</FormAlert>
    </form>

    <ConfirmDialog
      v-model:open="confirmOpen"
      :title="t('comments.deleteConfirmTitle')"
      :body="texts.deleteConfirmBody"
      :confirm-label="t('comments.delete')"
      danger
      @confirm="confirmDelete"
    />
  </section>
</template>
