<script setup lang="ts">
import { useIntersectionObserver } from '@vueuse/core'
import { computed, useTemplateRef } from 'vue'
import { useI18n } from 'vue-i18n'

import { errorMessageKey } from '@/api/errors'
import { useMySessions } from '@/api/sessions'
import SessionCard from '@/components/session/SessionCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'

/**
 * The feed (spec §8.2). Until friends arrive (phase 5), it shows my own sessions, latest first,
 * page by page as I scroll.
 */
const { t } = useI18n()
const sessions = useMySessions()

const items = computed(() => sessions.data.value?.pages.flatMap((page) => page.items) ?? [])

const sentinel = useTemplateRef<HTMLElement>('sentinel')
useIntersectionObserver(sentinel, ([entry]) => {
  if (entry?.isIntersecting && sessions.hasNextPage.value && !sessions.isFetchingNextPage.value) {
    void sessions.fetchNextPage()
  }
})
</script>

<template>
  <h1 class="sr-only">{{ t('actu.title') }}</h1>
  <section class="border-b border-rule px-4 py-3">
    <p class="text-ink-muted">{{ t('actu.clubTagline') }}</p>
  </section>

  <div v-if="sessions.isPending.value" class="flex flex-col gap-3 px-4 py-4" aria-hidden="true">
    <SkeletonBlock class="h-7 w-48" />
    <SkeletonBlock class="h-5 w-64" />
    <SkeletonBlock class="h-10 w-full" />
  </div>
  <div v-else-if="sessions.isError.value" class="flex flex-col items-start gap-3 px-4 py-4">
    <FormAlert>{{ t('actu.loadError') }} {{ t(errorMessageKey(sessions.error.value)) }}</FormAlert>
    <GgButton variant="secondary" @click="sessions.refetch()">{{ t('common.retry') }}</GgButton>
  </div>
  <EmptyState
    v-else-if="items.length === 0"
    :title="t('actu.emptyTitle')"
    :body="t('actu.emptyBody')"
  />
  <template v-else>
    <h2 class="px-4 pt-4 text-lg font-title">{{ t('actu.mine') }}</h2>
    <SessionCard
      v-for="session in items"
      :key="session.id"
      :session="session"
      linked
      :max-lines="5"
    />
    <div ref="sentinel" class="flex justify-center px-4 py-4">
      <GgButton
        v-if="sessions.hasNextPage.value"
        variant="secondary"
        :disabled="sessions.isFetchingNextPage.value"
        @click="sessions.fetchNextPage()"
      >
        {{ sessions.isFetchingNextPage.value ? t('actu.loadingMore') : t('actu.more') }}
      </GgButton>
      <p v-else class="text-sm text-ink-muted">{{ t('actu.end') }}</p>
    </div>
  </template>
</template>
