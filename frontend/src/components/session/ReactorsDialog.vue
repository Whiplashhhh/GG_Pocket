<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import { useCurrentMember } from '@/api/auth'
import { errorMessageKey } from '@/api/errors'
import { useSessionReactors } from '@/api/feed'
import GgButton from '@/components/ui/GgButton.vue'
import GgDialog from '@/components/ui/GgDialog.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { REACTION_EMOJI } from '@/lib/reactions'

/** Who reacted to a session, and with what (club feedback, D-128): asked for when it opens. */
const props = defineProps<{ sessionId: string }>()
const open = defineModel<boolean>('open', { required: true })

const { t } = useI18n()
const member = useCurrentMember()
const reactors = useSessionReactors(
  () => props.sessionId,
  () => open.value,
)
const list = computed(() => reactors.data.value ?? [])

function profileOf(id: string): string {
  return member.value?.id === id ? '/profil' : `/membres/${id}`
}
</script>

<template>
  <GgDialog v-model:open="open" :title="t('reactions.whoTitle')">
    <div v-if="reactors.isPending.value" class="flex flex-col gap-3" :aria-busy="true">
      <SkeletonBlock v-for="index in 3" :key="index" class="h-11" />
    </div>
    <p v-else-if="reactors.isError.value" role="alert" class="text-danger">
      {{ t('reactions.whoFailed') }} {{ t(errorMessageKey(reactors.error.value)) }}
    </p>
    <p v-else-if="list.length === 0" class="text-ink-muted">{{ t('reactions.whoEmpty') }}</p>
    <ul v-else class="-mx-4 divide-y divide-rule border-y border-rule">
      <li v-for="reactor in list" :key="reactor.member.id">
        <RouterLink
          :to="profileOf(reactor.member.id)"
          class="flex min-h-14 items-center gap-3 px-4 py-2 hover:bg-surface-sunken"
        >
          <UserAvatar
            :first-name="reactor.member.nickname"
            last-name=""
            :seed="reactor.member.nickname"
            :src="reactor.member.avatarUrl"
            :size="36"
          />
          <span class="min-w-0 flex-1 font-semibold [overflow-wrap:anywhere]">{{
            reactor.member.nickname
          }}</span>
          <span aria-hidden="true" class="text-2xl leading-none">{{
            REACTION_EMOJI[reactor.kind]
          }}</span>
          <span class="sr-only">{{ t(`reactions.names.${reactor.kind}`) }}</span>
        </RouterLink>
      </li>
    </ul>
    <GgButton class="mt-4" block variant="secondary" @click="open = false">{{
      t('reactions.whoClose')
    }}</GgButton>
  </GgDialog>
</template>
