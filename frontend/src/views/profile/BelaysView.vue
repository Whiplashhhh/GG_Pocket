<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { useMyBelays, useWithdrawBelay, type Belay } from '@/api/belays'
import { errorMessageKey } from '@/api/errors'
import SubPageHeader from '@/components/layout/SubPageHeader.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { sessionDay } from '@/lib/sessionView'

/**
 * "Tes assurages" (D-101): the sessions where someone named me as their belayer. I see who and
 * when, open the session only if it is shared with me anyway, and may take my name back.
 */
const { t } = useI18n()
const belays = useMyBelays()
const withdraw = useWithdrawBelay()
const confirming = ref<Belay | null>(null)
const open = computed({
  get: () => confirming.value !== null,
  set: (value: boolean) => {
    if (!value) confirming.value = null
  },
})

const items = computed(() => belays.data.value?.pages.flatMap((page) => page.items) ?? [])

function day(belay: Belay): string {
  const value = sessionDay(belay.climbedOn)
  return value.kind === 'date'
    ? t('session.day.date', { date: value.date })
    : t(`session.day.${value.kind}`)
}

function title(belay: Belay): string {
  return t('belays.sessionOf', { nickname: belay.climber.nickname, day: day(belay) })
}

function confirmWithdraw() {
  const belay = confirming.value
  if (belay !== null) withdraw.mutate(belay.sessionId)
}
</script>

<template>
  <SubPageHeader back="/profil" :back-label="t('common.back')">
    {{ t('belays.title') }}
  </SubPageHeader>
  <p class="px-4 pt-2 pb-4 text-ink-muted">{{ t('belays.intro') }}</p>

  <div v-if="belays.isPending.value" class="flex flex-col gap-3 px-4" aria-hidden="true">
    <SkeletonBlock class="h-16 w-full" />
    <SkeletonBlock class="h-16 w-full" />
  </div>
  <div v-else-if="belays.isError.value" class="flex flex-col items-start gap-3 px-4">
    <FormAlert>{{ t(errorMessageKey(belays.error.value)) }}</FormAlert>
    <GgButton variant="secondary" @click="belays.refetch()">{{ t('common.retry') }}</GgButton>
  </div>
  <EmptyState
    v-else-if="items.length === 0"
    :title="t('belays.emptyTitle')"
    :body="t('belays.emptyBody')"
  />

  <template v-else>
    <FormAlert v-if="withdraw.error.value" class="mx-4 mb-3">{{
      t(errorMessageKey(withdraw.error.value))
    }}</FormAlert>
    <ul class="border-t border-rule" :aria-label="t('belays.title')">
      <li
        v-for="belay in items"
        :key="belay.sessionId"
        class="flex flex-wrap items-center gap-x-3 gap-y-2 border-b border-rule px-4 py-3"
      >
        <UserAvatar
          :first-name="belay.climber.nickname"
          last-name=""
          :seed="belay.climber.nickname"
          :src="belay.climber.avatarUrl"
          :size="40"
        />
        <div class="flex min-w-0 flex-1 basis-40 flex-col">
          <RouterLink
            v-if="belay.readable"
            :to="`/seances/${belay.sessionId}`"
            class="font-semibold hover:underline"
            >{{ title(belay) }}</RouterLink
          >
          <p v-else class="font-semibold">{{ title(belay) }}</p>
          <p class="text-sm text-ink-muted">{{ t('belays.routes', belay.routeCount) }}</p>
        </div>
        <GgButton
          variant="ghost"
          class="ml-auto"
          :disabled="withdraw.isPending.value"
          :aria-label="t('belays.withdrawNamed', { title: title(belay) })"
          @click="confirming = belay"
        >
          {{ t('belays.withdraw') }}
        </GgButton>
      </li>
    </ul>
    <div v-if="belays.hasNextPage.value" class="px-4 py-6">
      <GgButton
        variant="secondary"
        :disabled="belays.isFetchingNextPage.value"
        @click="belays.fetchNextPage()"
      >
        {{ t('belays.more') }}
      </GgButton>
    </div>
  </template>

  <ConfirmDialog
    v-model:open="open"
    :title="t('belays.withdrawTitle')"
    :body="t('belays.withdrawBody', { nickname: confirming?.climber.nickname ?? '' })"
    :confirm-label="t('belays.withdraw')"
    @confirm="confirmWithdraw"
  />
</template>
