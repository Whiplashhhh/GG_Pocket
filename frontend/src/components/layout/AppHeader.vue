<script setup lang="ts">
import { Wrench } from 'lucide-vue-next'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import { useCurrentMember } from '@/api/auth'
import { useIncomingRequestCount } from '@/api/social'
import RoleBadge from '@/components/ui/RoleBadge.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { isAtLeast } from '@/lib/roles'

import HeaderBrand from './HeaderBrand.vue'

const { t } = useI18n()
const member = useCurrentMember()
const canManage = computed(() => isAtLeast(member.value?.role, 'SETTER'))
/** Friend requests waiting for my answer: a dot on my avatar (spec §8.2). */
const requests = useIncomingRequestCount(() => member.value !== undefined)
const profileLabel = computed(() =>
  requests.value > 0 ? t('header.profileLinkRequests', requests.value) : t('header.profileLink'),
)
</script>

<template>
  <header class="sticky top-0 z-20 border-b border-rule bg-bg/95 backdrop-blur-sm">
    <div class="mx-auto flex h-14 max-w-6xl items-center gap-2 px-4">
      <RouterLink
        to="/actu"
        class="-mx-1 flex min-h-11 shrink-0 items-center rounded-control px-1"
        :aria-label="t('header.homeLink')"
      >
        <HeaderBrand />
      </RouterLink>

      <div class="flex-1" />

      <RouterLink
        v-if="canManage"
        to="/gestion"
        class="inline-flex size-11 shrink-0 items-center justify-center rounded-control text-ink hover:bg-surface-sunken aria-[current=page]:text-brand-ink"
        :aria-label="t('header.management')"
      >
        <Wrench :size="22" aria-hidden="true" />
      </RouterLink>

      <RouterLink
        v-if="member"
        to="/profil"
        class="group flex min-h-11 min-w-0 items-center gap-2 rounded-control"
        :aria-label="profileLabel"
      >
        <span class="flex min-w-0 flex-col items-end gap-0.5">
          <span class="max-w-[9rem] truncate leading-tight font-semibold">{{
            member.nickname
          }}</span>
          <RoleBadge :role="member.role" short class="max-w-[9rem] truncate" />
        </span>
        <span
          class="relative rounded-full ring-brand ring-offset-2 ring-offset-bg group-aria-[current=page]:ring-2"
        >
          <UserAvatar
            :first-name="member.firstName"
            :last-name="member.lastName"
            :seed="member.nickname"
            :src="member.avatarUrl"
            :size="40"
          />
          <span
            v-if="requests > 0"
            class="absolute -top-1 -right-1 inline-flex min-w-5 items-center justify-center rounded-full bg-brand px-1 text-xs leading-5 font-bold text-on-brand ring-2 ring-bg"
            aria-hidden="true"
            >{{ requests > 9 ? '9+' : requests }}</span
          >
        </span>
      </RouterLink>
    </div>
  </header>
</template>
