<script setup lang="ts">
import { Download, KeyRound, ShieldCheck, Smartphone, Wrench } from 'lucide-vue-next'
import { storeToRefs } from 'pinia'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { useLogout } from '@/api/auth'
import { useMyProfile } from '@/api/me'
import { useMetaQuery } from '@/api/meta'
import PageTitle from '@/components/layout/PageTitle.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import RoleBadge from '@/components/ui/RoleBadge.vue'
import SegmentedControl from '@/components/ui/SegmentedControl.vue'
import SettingsLink from '@/components/ui/SettingsLink.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { errorMessageKey } from '@/api/errors'
import { formatDate } from '@/lib/format'
import { isAtLeast } from '@/lib/roles'
import { useThemeStore, type ThemePreference } from '@/stores/theme'

/** Spec §8.8: my profile and settings. Favourites, badges and key figures come with later phases. */
const { t } = useI18n()
const { data: profile, isPending, isError, error, refetch } = useMyProfile()
const { data: meta } = useMetaQuery()
const { preference } = storeToRefs(useThemeStore())
const logout = useLogout()
const confirmingLogout = ref(false)

const appVersion = __APP_VERSION__
const themeOptions: { value: ThemePreference; label: string }[] = [
  { value: 'auto', label: t('theme.auto') },
  { value: 'light', label: t('theme.light') },
  { value: 'dark', label: t('theme.dark') },
]

const facts = computed(() => {
  const value = profile.value
  if (value === undefined) {
    return []
  }
  return [
    value.showAge && value.age !== undefined ? t('profile.age', { age: value.age }) : null,
    value.climbingSinceYear ? t('profile.climbingSince', { year: value.climbingSinceYear }) : null,
    value.clubArrivalYear ? t('profile.clubSince', { year: value.clubArrivalYear }) : null,
    t('profile.memberSince', { date: formatDate(value.createdAt) }),
  ].filter((fact): fact is string => fact !== null)
})
</script>

<template>
  <PageTitle>{{ t('profile.title') }}</PageTitle>

  <div v-if="isPending" class="flex items-center gap-4 px-4 py-6" aria-hidden="true">
    <SkeletonBlock class="size-24 rounded-full" />
    <div class="flex flex-1 flex-col gap-2">
      <SkeletonBlock class="h-6 w-2/3" />
      <SkeletonBlock class="h-4 w-1/3" />
    </div>
  </div>
  <div v-else-if="isError" class="flex flex-col items-start gap-3 px-4 py-6">
    <FormAlert>{{ t(errorMessageKey(error)) }}</FormAlert>
    <GgButton variant="secondary" @click="refetch()">{{ t('common.retry') }}</GgButton>
  </div>
  <section v-else-if="profile" class="px-4 pt-5 pb-6" aria-labelledby="mon-identite">
    <div class="flex items-center gap-4">
      <UserAvatar
        :first-name="profile.firstName"
        :last-name="profile.lastName"
        :seed="profile.nickname"
        :src="profile.avatarUrl"
        :size="96"
      />
      <div class="flex min-w-0 flex-col items-start gap-1">
        <h2 id="mon-identite" class="text-xl font-title break-words">
          {{ profile.firstName }} {{ profile.lastName }}
        </h2>
        <p class="text-ink-muted">@{{ profile.nickname }}</p>
        <RoleBadge :role="profile.role" />
      </div>
    </div>
    <p v-if="profile.bio" class="mt-4 max-w-prose">{{ profile.bio }}</p>
    <ul class="mt-3 flex flex-wrap gap-x-4 gap-y-1 text-sm text-ink-muted">
      <li v-for="fact in facts" :key="fact">{{ fact }}</li>
    </ul>
    <GgButton to="/profil/modifier" variant="secondary" class="mt-5">
      {{ t('profile.edit') }}
    </GgButton>
    <p class="mt-5 text-sm text-ink-muted">{{ t('profile.comingSoon') }}</p>
  </section>

  <section class="border-t border-rule" aria-labelledby="reglages">
    <h2 id="reglages" class="px-4 pt-6 pb-2 text-lg font-title">{{ t('profile.settings') }}</h2>
    <div class="border-t border-rule">
      <SettingsLink v-if="profile && isAtLeast(profile.role, 'SETTER')" to="/gestion">
        <template #icon><Wrench :size="20" /></template>
        {{ t('profile.management') }}
      </SettingsLink>
      <SettingsLink to="/profil/mot-de-passe">
        <template #icon><KeyRound :size="20" /></template>
        {{ t('profile.password') }}
      </SettingsLink>
      <SettingsLink to="/profil/appareils">
        <template #icon><Smartphone :size="20" /></template>
        {{ t('profile.devices') }}
      </SettingsLink>
      <SettingsLink to="/profil/installer">
        <template #icon><Download :size="20" /></template>
        {{ t('profile.install') }}
      </SettingsLink>
      <SettingsLink to="/tes-donnees">
        <template #icon><ShieldCheck :size="20" /></template>
        {{ t('profile.privacy') }}
      </SettingsLink>
    </div>
    <div class="px-4 py-6">
      <p class="mb-2 font-semibold" aria-hidden="true">{{ t('theme.label') }}</p>
      <SegmentedControl v-model="preference" :label="t('theme.label')" :options="themeOptions" />
      <p v-if="preference === 'auto'" class="mt-2 text-sm text-ink-muted">
        {{ t('theme.autoHint') }}
      </p>
    </div>
    <div class="border-t border-rule px-4 py-6">
      <GgButton
        variant="danger"
        :disabled="logout.isPending.value"
        @click="confirmingLogout = true"
      >
        {{ t('profile.logout') }}
      </GgButton>
    </div>
  </section>

  <footer class="border-t border-rule px-4 py-6 text-sm text-ink-muted">
    <p>{{ t('app.versionFooter', { version: appVersion }) }}</p>
    <p v-if="meta">{{ t('app.serverVersion', { version: meta.version }) }}</p>
  </footer>

  <ConfirmDialog
    v-model:open="confirmingLogout"
    :title="t('profile.logoutConfirmTitle')"
    :body="t('profile.logoutConfirmBody')"
    :confirm-label="t('profile.logout')"
    danger
    @confirm="logout.mutate()"
  />
</template>
