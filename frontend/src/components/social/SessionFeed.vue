<script setup lang="ts">
import { useIntersectionObserver } from '@vueuse/core'
import { computed, useTemplateRef } from 'vue'
import { useI18n } from 'vue-i18n'

import { errorMessageKey } from '@/api/errors'
import type { useFeed } from '@/api/feed'
import SessionCard from '@/components/session/SessionCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'

/**
 * One feed of sessions (the friends' one or the club's, D-120), latest first, page by page as I
 * scroll. The empty state's actions come in the default slot.
 */
const props = defineProps<{
  feed: ReturnType<typeof useFeed>
  /** Accessible heading of the list. */
  heading: string
  emptyTitle: string
  emptyBody: string
}>()

const { t } = useI18n()

const items = computed(() => props.feed.data.value?.pages.flatMap((page) => page.items) ?? [])

const sentinel = useTemplateRef<HTMLElement>('sentinel')
useIntersectionObserver(sentinel, ([entry]) => {
  const feed = props.feed
  if (entry?.isIntersecting && feed.hasNextPage.value && !feed.isFetchingNextPage.value) {
    void feed.fetchNextPage()
  }
})
</script>

<template>
  <div v-if="feed.isPending.value" class="flex flex-col gap-3 px-4 py-4" aria-hidden="true">
    <SkeletonBlock class="h-7 w-48" />
    <SkeletonBlock class="h-5 w-64" />
    <SkeletonBlock class="h-10 w-full" />
  </div>
  <div v-else-if="feed.isError.value" class="flex flex-col items-start gap-3 px-4 py-4">
    <FormAlert>{{ t('actu.loadError') }} {{ t(errorMessageKey(feed.error.value)) }}</FormAlert>
    <GgButton variant="secondary" @click="feed.refetch()">{{ t('common.retry') }}</GgButton>
  </div>
  <EmptyState v-else-if="items.length === 0" :title="emptyTitle" :body="emptyBody">
    <slot />
  </EmptyState>
  <template v-else>
    <h2 class="sr-only">{{ heading }}</h2>
    <div class="flex flex-col gap-3 px-3 pt-3 sm:px-4">
      <SessionCard
        v-for="session in items"
        :key="session.id"
        :session="session"
        linked
        show-author
        framed
        :max-lines="5"
        :heading-level="3"
      />
    </div>
    <div ref="sentinel" class="flex justify-center px-4 py-4">
      <GgButton
        v-if="feed.hasNextPage.value"
        variant="secondary"
        :disabled="feed.isFetchingNextPage.value"
        @click="feed.fetchNextPage()"
      >
        {{ feed.isFetchingNextPage.value ? t('actu.loadingMore') : t('actu.more') }}
      </GgButton>
      <p v-else class="text-sm text-ink-muted">{{ t('actu.end') }}</p>
    </div>
  </template>
</template>
