<script setup lang="ts">
import { useIntersectionObserver } from '@vueuse/core'
import { ChevronRight, Users } from 'lucide-vue-next'
import { computed, useTemplateRef } from 'vue'
import { useI18n } from 'vue-i18n'

import { useAwaitingValidation } from '@/api/auth'
import { errorMessageKey } from '@/api/errors'
import { useFeed } from '@/api/feed'
import { useDirectory } from '@/api/social'
import AwaitingValidationNotice from '@/components/member/AwaitingValidationNotice.vue'
import SessionCard from '@/components/session/SessionCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'

/**
 * The feed (spec §8.2): the club header with its climber count (the way to the directory), then my
 * sessions and the ones my friends share, latest first, page by page as I scroll. Friend requests
 * wait on my profile (D-099). An account awaiting validation sees its own sessions only, and why
 * (D-107).
 */
const { t } = useI18n()
const sessions = useFeed()
const directory = useDirectory()
const awaiting = useAwaitingValidation()

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
    <AwaitingValidationNotice
      v-if="awaiting"
      class="mt-3"
      :title="t('awaiting.title')"
      :body="t('awaiting.locked')"
    />
    <RouterLink
      v-else
      to="/annuaire"
      class="-mx-1 mt-1 inline-flex min-h-11 items-center gap-2 rounded-control px-1 font-semibold text-brand-ink hover:underline"
    >
      <Users :size="20" aria-hidden="true" />
      <template v-if="directory.data.value">
        {{ t('actu.clubCount', directory.data.value.climberCount) }}
        <span class="sr-only">{{ t('actu.clubCountHintSuffix') }}</span>
      </template>
      <template v-else>{{ t('actu.clubCountHint') }}</template>
      <ChevronRight :size="18" aria-hidden="true" />
    </RouterLink>
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
  >
    <GgButton v-if="!awaiting" variant="secondary" to="/annuaire">{{
      t('actu.findFriends')
    }}</GgButton>
  </EmptyState>
  <template v-else>
    <h2 class="sr-only">{{ t('actu.feed') }}</h2>
    <SessionCard
      v-for="session in items"
      :key="session.id"
      :session="session"
      linked
      show-author
      :max-lines="5"
      :heading-level="3"
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
