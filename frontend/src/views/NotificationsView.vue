<script setup lang="ts">
import { computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import { errorMessageKey } from '@/api/errors'
import { useMarkNotificationsSeen, useNotifications, type Notification } from '@/api/notifications'
import SubPageHeader from '@/components/layout/SubPageHeader.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { formatDateTime, formatRelative } from '@/lib/format'
import { notificationLine } from '@/lib/notifications'

/**
 * The bell (club feedback, D-128): what happened around me this last month, newest first. Opening
 * it puts the header's dot out; what was new keeps its mark until the next visit.
 */
const { t } = useI18n()
const notifications = useNotifications()
const markSeen = useMarkNotificationsSeen()

const items = computed(() => notifications.data.value?.items ?? [])

watch(
  () => notifications.data.value?.unreadCount,
  (unread) => {
    if (unread !== undefined && unread > 0 && !markSeen.isPending.value) markSeen.mutate()
  },
  { immediate: true },
)

function line(item: Notification) {
  const { key, params, to } = notificationLine(item)
  const day =
    params.dayKind === undefined
      ? ''
      : t(`session.day.${params.dayKind}`, { date: params.date ?? '' })
  return { text: t(key, { ...params, day }), to }
}
</script>

<template>
  <SubPageHeader back="/actu" :back-label="t('common.back')">
    {{ t('notifications.title') }}
  </SubPageHeader>
  <p class="px-4 pt-2 pb-4 text-ink-muted">{{ t('notifications.intro') }}</p>
  <div v-if="notifications.isPending.value" class="flex flex-col gap-3 px-4" aria-hidden="true">
    <SkeletonBlock v-for="index in 3" :key="index" class="h-16 w-full" />
  </div>
  <div v-else-if="notifications.isError.value" class="flex flex-col items-start gap-3 px-4">
    <FormAlert>{{ t(errorMessageKey(notifications.error.value)) }}</FormAlert>
    <GgButton variant="secondary" @click="notifications.refetch()">{{
      t('common.retry')
    }}</GgButton>
  </div>
  <EmptyState
    v-else-if="items.length === 0"
    :title="t('notifications.emptyTitle')"
    :body="t('notifications.emptyBody')"
  />
  <ul v-else class="border-t border-rule" :aria-label="t('notifications.title')">
    <li v-for="item in items" :key="item.id" class="border-b border-rule">
      <RouterLink
        :to="line(item).to"
        class="flex min-h-16 items-start gap-3 px-4 py-3 hover:bg-surface-sunken"
        :class="item.unread ? 'bg-surface' : ''"
      >
        <UserAvatar
          :first-name="item.actor.nickname"
          last-name=""
          :seed="item.actor.nickname"
          :src="item.actor.avatarUrl"
          :size="40"
        />
        <span class="min-w-0 flex-1">
          <span class="block [overflow-wrap:anywhere] break-words">{{ line(item).text }}</span>
          <span class="mt-0.5 flex items-center gap-2 text-sm text-ink-muted">
            <time :datetime="item.at" :title="formatDateTime(item.at)">{{
              formatRelative(item.at)
            }}</time>
            <span
              v-if="item.unread"
              class="rounded-full bg-brand px-2 text-xs leading-5 font-bold text-on-brand"
              >{{ t('notifications.new') }}</span
            >
          </span>
        </span>
      </RouterLink>
    </li>
  </ul>
</template>
