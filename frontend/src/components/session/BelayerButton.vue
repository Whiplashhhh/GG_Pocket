<script setup lang="ts">
import { ChevronRight } from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'

import UserAvatar from '@/components/ui/UserAvatar.vue'

/** Who held the rope, as a field of the session sheet: a tap opens the BelayerPicker. */
defineProps<{
  /** Already translated: says what the field is for, who is named, and that a tap changes it. */
  label: string
  /** Undefined when nobody is named (or no longer in the club). */
  belayer: { nickname: string; avatarUrl?: string } | undefined
}>()
const emit = defineEmits<{ choose: [] }>()
const { t } = useI18n()
</script>

<template>
  <button
    type="button"
    class="flex min-h-12 w-full items-center gap-3 rounded-control border-2 border-rule bg-surface px-3 py-1.5 text-left hover:border-ink"
    @click="emit('choose')"
  >
    <!-- The name in hidden text, not aria-label: the avatar's initial is not in it (axe). -->
    <span class="sr-only">{{ label }}</span>
    <UserAvatar
      v-if="belayer"
      aria-hidden="true"
      :first-name="belayer.nickname"
      last-name=""
      :seed="belayer.nickname"
      :src="belayer.avatarUrl"
      :size="32"
    />
    <span
      class="flex-1 truncate"
      :class="belayer ? 'font-semibold' : 'text-ink-muted'"
      aria-hidden="true"
      >{{ belayer?.nickname ?? t('session.belayerNobody') }}</span
    >
    <ChevronRight :size="20" aria-hidden="true" />
  </button>
</template>
