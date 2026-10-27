<script setup lang="ts">
import { ChevronDown, UserPlus } from 'lucide-vue-next'
import { ref, useId } from 'vue'
import { useI18n } from 'vue-i18n'

import { useMyFriends } from '@/api/social'
import { formatRelative } from '@/lib/format'

import MemberRow from './MemberRow.vue'

/**
 * The requests waiting for my answer, on my profile (D-099): a banner with their number, folded
 * until I open it. Nothing when none.
 */
const { t } = useI18n()
const friends = useMyFriends()
const open = ref(false)
const listId = useId()
</script>

<template>
  <section
    v-if="friends.data.value && friends.data.value.incoming.length > 0"
    class="border-b border-rule bg-surface-sunken"
    :aria-label="t('friends.requestsTitle', friends.data.value.incoming.length)"
  >
    <button
      type="button"
      class="flex min-h-14 w-full items-center gap-3 px-4 py-2 text-left font-semibold"
      :aria-expanded="open"
      :aria-controls="listId"
      @click="open = !open"
    >
      <UserPlus :size="22" class="shrink-0 text-brand-ink" aria-hidden="true" />
      <span class="flex-1">
        {{ t('friends.requestsTitle', friends.data.value.incoming.length) }}
      </span>
      <ChevronDown
        :size="20"
        class="shrink-0 transition-transform motion-reduce:transition-none"
        :class="{ 'rotate-180': open }"
        aria-hidden="true"
      />
    </button>
    <div v-show="open" :id="listId" class="pb-1">
      <p class="px-4 text-sm text-ink-muted">{{ t('friends.requestsIntro') }}</p>
      <ul>
        <li v-for="request in friends.data.value.incoming" :key="request.member.id">
          <MemberRow
            :member="request.member"
            friendship="INCOMING"
            :detail="t('friends.asked', { date: formatRelative(request.since) })"
          />
        </li>
      </ul>
    </div>
  </section>
</template>
