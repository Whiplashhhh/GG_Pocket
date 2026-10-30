<script setup lang="ts">
import {
  BookOpen,
  ChevronRight,
  Download,
  FileDown,
  KeyRound,
  Link2,
  ShieldCheck,
  Smartphone,
  Users,
  Wrench,
} from 'lucide-vue-next'
import { storeToRefs } from 'pinia'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { useLogout } from '@/api/auth'
import { useMyProfile } from '@/api/me'
import { useMetaQuery } from '@/api/meta'
import { useMyFavorites, useRouteDetail } from '@/api/topo'
import PageTitle from '@/components/layout/PageTitle.vue'
import FriendRequests from '@/components/social/FriendRequests.vue'
import RouteSummaryLabel from '@/components/topo/RouteSummaryLabel.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import RoleBadge from '@/components/ui/RoleBadge.vue'
import SegmentedControl from '@/components/ui/SegmentedControl.vue'
import SettingsLink from '@/components/ui/SettingsLink.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import AppUpdateCheck from '@/components/profile/AppUpdateCheck.vue'
import { errorMessageKey } from '@/api/errors'
import { formatDate } from '@/lib/format'
import { isAtLeast } from '@/lib/roles'
import { useThemeStore, type ThemePreference } from '@/stores/theme'

/** Spec §8.8: my profile and settings. Badges and key figures come with later phases. */
const { t } = useI18n()
const { data: profile, isPending, isError, error, refetch } = useMyProfile()
const { data: meta } = useMetaQuery()
const { preference } = storeToRefs(useThemeStore())
const logout = useLogout()
const favoriteRoute = useRouteDetail(() => profile.value?.favoriteRouteId)
const favorites = useMyFavorites()
const activeFavorites = computed(() =>
  (favorites.data.value ?? []).filter((route) => route.status !== 'DISMOUNTED'),
)
const pastFavorites = computed(() =>
  (favorites.data.value ?? []).filter((route) => route.status === 'DISMOUNTED'),
)
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
  <FriendRequests />

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
  </section>

  <section
    v-if="profile?.favoriteRouteId && favoriteRoute.data.value"
    class="border-t border-rule px-4 pt-5 pb-6"
    aria-labelledby="voie-preferee"
  >
    <h2 id="voie-preferee" class="text-lg font-title">{{ t('profile.favoriteRoute') }}</h2>
    <RouterLink
      :to="`/topo/voies/${favoriteRoute.data.value.route.id}`"
      aria-labelledby="voie-preferee-nom"
      class="mt-3 flex max-w-full pt-2"
    >
      <RouteSummaryLabel :route="favoriteRoute.data.value.route" label-id="voie-preferee-nom" />
    </RouterLink>
  </section>

  <section v-if="profile" class="border-t border-rule pt-5 pb-6" aria-labelledby="coups-de-coeur">
    <h2 id="coups-de-coeur" class="px-4 text-lg font-title">{{ t('profile.favorites') }}</h2>
    <div v-if="favorites.isPending.value" class="px-4 pt-3" aria-hidden="true">
      <SkeletonBlock class="h-11 w-full max-w-72" />
    </div>
    <p v-else-if="favorites.data.value?.length === 0" class="max-w-prose px-4 pt-2 text-ink-muted">
      {{ t('profile.favoritesEmpty') }}
    </p>
    <template v-else-if="favorites.data.value">
      <ul class="mt-2 flex flex-col">
        <li v-for="route in activeFavorites" :key="route.id">
          <RouterLink
            :to="`/topo/voies/${route.id}`"
            :aria-labelledby="`coeur-${route.id}`"
            class="flex px-4 pt-4 pb-1"
          >
            <RouteSummaryLabel :route="route" :label-id="`coeur-${route.id}`" />
          </RouterLink>
        </li>
      </ul>
      <template v-if="pastFavorites.length > 0">
        <h3 class="px-4 pt-5 text-sm font-semibold text-ink-muted">
          {{ t('profile.pastFavorites') }}
        </h3>
        <ul class="flex flex-col">
          <li v-for="route in pastFavorites" :key="route.id">
            <RouterLink
              :to="`/topo/voies/${route.id}`"
              :aria-labelledby="`coeur-${route.id}`"
              class="flex px-4 pt-3 pb-1"
            >
              <RouteSummaryLabel :route="route" size="sm" :label-id="`coeur-${route.id}`" />
            </RouterLink>
          </li>
        </ul>
      </template>
    </template>
    <RouterLink
      to="/carnet"
      class="mx-4 mt-6 flex min-h-14 items-center gap-3 rounded-control border-2 border-ink px-3 py-2 hover:bg-surface-sunken"
    >
      <BookOpen :size="24" class="shrink-0 text-brand-ink" aria-hidden="true" />
      <span class="flex min-w-0 flex-1 flex-col">
        <span class="font-semibold">{{ t('profile.logbook') }}</span>
        <span class="text-sm text-ink-muted">{{ t('profile.logbookHint') }}</span>
      </span>
      <ChevronRight :size="20" class="shrink-0" aria-hidden="true" />
    </RouterLink>
    <p class="mt-4 px-4 text-sm text-ink-muted">{{ t('profile.comingSoon') }}</p>
  </section>

  <section class="border-t border-rule" aria-labelledby="reglages">
    <h2 id="reglages" class="px-4 pt-6 pb-2 text-lg font-title">{{ t('profile.settings') }}</h2>
    <div class="border-t border-rule">
      <SettingsLink v-if="profile && isAtLeast(profile.role, 'SETTER')" to="/gestion">
        <template #icon><Wrench :size="20" /></template>
        {{ t('profile.management') }}
      </SettingsLink>
      <SettingsLink to="/annuaire?amis">
        <template #icon><Users :size="20" /></template>
        {{ t('profile.friends') }}
      </SettingsLink>
      <SettingsLink to="/profil/assurages">
        <template #icon><Link2 :size="20" /></template>
        {{ t('profile.belays') }}
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
      <SettingsLink to="/profil/compte">
        <template #icon><FileDown :size="20" /></template>
        {{ t('profile.account') }}
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
        variant="secondary"
        :disabled="logout.isPending.value"
        @click="confirmingLogout = true"
      >
        {{ t('profile.logout') }}
      </GgButton>
    </div>
  </section>

  <footer class="border-t border-rule px-4 py-6 text-sm text-ink-muted">
    <p>{{ t('app.versionFooter', { version: appVersion }) }}</p>
    <p v-if="meta && profile && isAtLeast(profile.role, 'BOARD')">
      {{ t('app.serverVersion', { version: meta.version }) }}
    </p>
    <AppUpdateCheck class="mt-3" />
  </footer>

  <ConfirmDialog
    v-model:open="confirmingLogout"
    :title="t('profile.logoutConfirmTitle')"
    :body="t('profile.logoutConfirmBody')"
    :confirm-label="t('profile.logout')"
    @confirm="logout.mutate()"
  />
</template>
