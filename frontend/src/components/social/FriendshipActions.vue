<script setup lang="ts">
import { Check, Clock } from 'lucide-vue-next'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { errorMessageKey } from '@/api/errors'
import { useBefriend, useUnfriend, type FriendshipState } from '@/api/social'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import { friendshipControls, type FriendshipAction, type FriendshipPlace } from '@/lib/friendship'

/** « Demander en ami » / « En attente » / « Amis » (spec §8.2), and the answers to a request. */
const props = defineProps<{
  memberId: string
  nickname: string
  state: FriendshipState
  place: FriendshipPlace
}>()

const { t } = useI18n()
const befriend = useBefriend()
const unfriend = useUnfriend()
const confirmOpen = ref(false)
const toConfirm = ref<FriendshipAction | null>(null)

const controls = computed(() => friendshipControls(props.state, props.place))
const busy = computed(() => befriend.isPending.value || unfriend.isPending.value)
const failure = computed(() => befriend.error.value ?? unfriend.error.value)

function ask(action: FriendshipAction) {
  if (action.confirm) {
    toConfirm.value = action
    confirmOpen.value = true
  } else {
    run(action)
  }
}

function run(action: FriendshipAction) {
  befriend.reset()
  unfriend.reset()
  if (action.kind === 'befriend') befriend.mutate(props.memberId)
  else unfriend.mutate(props.memberId)
}

function confirmed() {
  if (toConfirm.value !== null) run(toConfirm.value)
}
</script>

<template>
  <div class="flex flex-col items-end gap-2">
    <div class="flex flex-wrap items-center justify-end gap-2">
      <span
        v-if="controls.status"
        class="inline-flex min-h-11 items-center gap-1.5 px-1 text-sm font-semibold"
        :class="controls.status === 'friends' ? 'text-success' : 'text-ink-muted'"
      >
        <Check v-if="controls.status === 'friends'" :size="18" aria-hidden="true" />
        <Clock v-else :size="18" aria-hidden="true" />
        {{ t(`friends.${controls.status}`) }}
      </span>
      <GgButton
        v-for="action in controls.actions"
        :key="action.label"
        :variant="action.variant"
        :disabled="busy"
        :aria-label="t(`friends.${action.label}Label`, { name: nickname })"
        @click="ask(action)"
      >
        {{ t(`friends.${action.label}`) }}
      </GgButton>
    </div>
    <FormAlert v-if="failure" class="max-w-xs">
      {{ t('friends.error', { reason: t(errorMessageKey(failure)) }) }}
    </FormAlert>
    <ConfirmDialog
      v-model:open="confirmOpen"
      :title="t('friends.removeTitle')"
      :body="t('friends.removeBody', { name: nickname })"
      :confirm-label="t('friends.remove')"
      danger
      @confirm="confirmed"
    />
  </div>
</template>
