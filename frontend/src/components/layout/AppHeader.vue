<script setup lang="ts">
import { Wrench } from 'lucide-vue-next'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import { useCurrentMember } from '@/api/auth'
import RoleBadge from '@/components/ui/RoleBadge.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { isAtLeast } from '@/lib/roles'

import HeaderBrand from './HeaderBrand.vue'

const { t } = useI18n()
const member = useCurrentMember()
const canManage = computed(() => isAtLeast(member.value?.role, 'SETTER'))
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
        :aria-label="t('header.profileLink')"
      >
        <span class="flex min-w-0 flex-col items-end gap-0.5">
          <span class="max-w-[9rem] truncate leading-tight font-semibold">{{
            member.nickname
          }}</span>
          <RoleBadge :role="member.role" short class="max-w-[9rem] truncate" />
        </span>
        <span
          class="rounded-full ring-brand ring-offset-2 ring-offset-bg group-aria-[current=page]:ring-2"
        >
          <UserAvatar
            :first-name="member.firstName"
            :last-name="member.lastName"
            :seed="member.nickname"
            :src="member.avatarUrl"
            :size="40"
          />
        </span>
      </RouterLink>
    </div>
  </header>
</template>
