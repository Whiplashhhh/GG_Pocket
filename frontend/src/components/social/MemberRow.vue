<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import type { FriendshipState, MemberCard } from '@/api/social'
import UserAvatar from '@/components/ui/UserAvatar.vue'

import FriendshipActions from './FriendshipActions.vue'

/** One member in a list: who they are (link to their profile) and where I stand with them. */
defineProps<{ member: MemberCard; friendship: FriendshipState; detail?: string }>()

const { t } = useI18n()
</script>

<template>
  <div class="flex flex-wrap items-center gap-x-3 gap-y-2 px-4 py-3">
    <RouterLink
      :to="`/membres/${member.id}`"
      class="flex min-h-11 min-w-0 flex-1 basis-48 items-center gap-3 rounded-control"
    >
      <UserAvatar
        :first-name="member.firstName"
        :last-name="member.lastName"
        :seed="member.nickname"
        :src="member.avatarUrl"
        :size="48"
      />
      <span class="flex min-w-0 flex-col">
        <span class="truncate font-semibold">{{ member.firstName }} {{ member.lastName }}</span>
        <span class="truncate text-sm text-ink-muted">
          @{{ member.nickname }}
          <template v-if="detail"> · {{ detail }}</template>
          <template v-else-if="member.clubArrivalYear">
            · {{ t('directory.clubSince', { year: member.clubArrivalYear }) }}
          </template>
        </span>
      </span>
    </RouterLink>
    <FriendshipActions
      class="ml-auto"
      :member-id="member.id"
      :nickname="member.nickname"
      :state="friendship"
      place="row"
    />
  </div>
</template>
