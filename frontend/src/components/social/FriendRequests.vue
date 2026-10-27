<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import { useMyFriends } from '@/api/social'
import { formatRelative } from '@/lib/format'

import MemberRow from './MemberRow.vue'

/** The requests waiting for my answer, on top of the feed (spec §8.2). Nothing when none. */
const { t } = useI18n()
const friends = useMyFriends()
</script>

<template>
  <section
    v-if="friends.data.value && friends.data.value.incoming.length > 0"
    class="border-b border-rule bg-surface-sunken pt-4 pb-1"
    aria-labelledby="demandes-amis"
  >
    <h2 id="demandes-amis" class="px-4 text-lg font-title">
      {{ t('friends.requestsTitle', friends.data.value.incoming.length) }}
    </h2>
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
  </section>
</template>
