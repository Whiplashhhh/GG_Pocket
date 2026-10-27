<script setup lang="ts">
import { useIntersectionObserver } from '@vueuse/core'
import { BookOpen, ChevronRight, Lock } from 'lucide-vue-next'
import { computed, useTemplateRef, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

import { ApiError, errorMessageKey } from '@/api/errors'
import { useMemberProfile, useMemberSessions } from '@/api/social'
import { useRouteDetail } from '@/api/topo'
import SubPageHeader from '@/components/layout/SubPageHeader.vue'
import SessionCard from '@/components/session/SessionCard.vue'
import FriendshipActions from '@/components/social/FriendshipActions.vue'
import RouteSummaryLabel from '@/components/topo/RouteSummaryLabel.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import RoleBadge from '@/components/ui/RoleBadge.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'

/**
 * Another member's profile (spec §8.8): the public part for every member; their sessions and
 * logbook for their friends only (the server says so too).
 */
const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const id = computed(() => String(route.params.id))
const profile = useMemberProfile(id)
const data = computed(() => profile.data.value)
const isFriend = computed(() => data.value?.friendship === 'FRIENDS')
const sessions = useMemberSessions(id, isFriend)
const favoriteRoute = useRouteDetail(() => data.value?.favoriteRouteId)

const notFound = computed(
  () => profile.error.value instanceof ApiError && profile.error.value.code === 'NOT_FOUND',
)

// My own public profile is my profile.
watch(
  () => data.value?.friendship,
  (friendship) => {
    if (friendship === 'SELF') void router.replace('/profil')
  },
  { immediate: true },
)

const activeFavorites = computed(() =>
  (data.value?.favorites ?? []).filter((favorite) => favorite.status !== 'DISMOUNTED'),
)

const facts = computed(() => {
  const value = data.value
  if (value === undefined) return []
  return [
    value.age === undefined ? null : t('profile.age', { age: value.age }),
    value.climbingSinceYear ? t('profile.climbingSince', { year: value.climbingSinceYear }) : null,
    value.member.clubArrivalYear
      ? t('profile.clubSince', { year: value.member.clubArrivalYear })
      : null,
  ].filter((fact): fact is string => fact !== null)
})

const lockedMessage = computed(() => {
  switch (data.value?.friendship) {
    case 'REQUESTED':
      return t('memberProfile.sessionsRequested')
    case 'INCOMING':
      return t('memberProfile.sessionsIncoming')
    default:
      return t('memberProfile.sessionsLocked')
  }
})

const items = computed(() => sessions.data.value?.pages.flatMap((page) => page.items) ?? [])
const sentinel = useTemplateRef<HTMLElement>('sentinel')
useIntersectionObserver(sentinel, ([entry]) => {
  if (entry?.isIntersecting && sessions.hasNextPage.value && !sessions.isFetchingNextPage.value) {
    void sessions.fetchNextPage()
  }
})
</script>

<template>
  <SubPageHeader back="/annuaire" :back-label="t('memberProfile.back')">
    {{ data ? `@${data.member.nickname}` : t('memberProfile.title') }}
  </SubPageHeader>

  <div v-if="profile.isPending.value" class="flex items-center gap-4 px-4 pt-5" aria-hidden="true">
    <SkeletonBlock class="size-24 rounded-full" />
    <div class="flex flex-1 flex-col gap-2">
      <SkeletonBlock class="h-6 w-2/3" />
      <SkeletonBlock class="h-4 w-1/3" />
    </div>
  </div>
  <EmptyState
    v-else-if="notFound"
    :title="t('memberProfile.notFoundTitle')"
    :body="t('memberProfile.notFoundBody')"
  />
  <div v-else-if="profile.isError.value" class="flex flex-col items-start gap-3 px-4 pt-5">
    <FormAlert>
      {{ t('memberProfile.loadError') }} {{ t(errorMessageKey(profile.error.value)) }}
    </FormAlert>
    <GgButton variant="secondary" @click="profile.refetch()">{{ t('common.retry') }}</GgButton>
  </div>

  <template v-else-if="data">
    <section class="px-4 pt-5 pb-6" aria-labelledby="son-identite">
      <div class="flex items-center gap-4">
        <UserAvatar
          :first-name="data.member.firstName"
          :last-name="data.member.lastName"
          :seed="data.member.nickname"
          :src="data.member.avatarUrl"
          :size="96"
        />
        <div class="flex min-w-0 flex-col items-start gap-1">
          <h2 id="son-identite" class="text-xl font-title break-words">
            {{ data.member.firstName }} {{ data.member.lastName }}
          </h2>
          <p class="text-ink-muted">@{{ data.member.nickname }}</p>
          <RoleBadge :role="data.member.role" />
        </div>
      </div>
      <p v-if="data.bio" class="mt-4 max-w-prose">{{ data.bio }}</p>
      <ul
        v-if="facts.length > 0"
        class="mt-3 flex flex-wrap gap-x-4 gap-y-1 text-sm text-ink-muted"
      >
        <li v-for="fact in facts" :key="fact">{{ fact }}</li>
      </ul>
      <FriendshipActions
        class="mt-4 items-start [&>div]:justify-start"
        :member-id="data.member.id"
        :nickname="data.member.nickname"
        :state="data.friendship"
        place="profile"
      />
    </section>

    <section
      v-if="data.favoriteRouteId && favoriteRoute.data.value"
      class="border-t border-rule px-4 pt-5 pb-6"
      aria-labelledby="sa-voie-preferee"
    >
      <h2 id="sa-voie-preferee" class="text-lg font-title">{{ t('profile.favoriteRoute') }}</h2>
      <RouterLink
        :to="`/topo/voies/${favoriteRoute.data.value.route.id}`"
        aria-labelledby="sa-voie-preferee-nom"
        class="mt-3 flex max-w-full pt-2"
      >
        <RouteSummaryLabel
          :route="favoriteRoute.data.value.route"
          label-id="sa-voie-preferee-nom"
        />
      </RouterLink>
    </section>

    <section class="border-t border-rule pt-5 pb-6" aria-labelledby="ses-coups-de-coeur">
      <h2 id="ses-coups-de-coeur" class="px-4 text-lg font-title">
        {{ t('memberProfile.favorites') }}
      </h2>
      <p v-if="activeFavorites.length === 0" class="px-4 pt-2 text-ink-muted">
        {{ t('memberProfile.favoritesEmpty') }}
      </p>
      <ul v-else class="mt-2 flex flex-col">
        <li v-for="favorite in activeFavorites" :key="favorite.id">
          <RouterLink
            :to="`/topo/voies/${favorite.id}`"
            :aria-labelledby="`son-coeur-${favorite.id}`"
            class="flex px-4 pt-4 pb-1"
          >
            <RouteSummaryLabel :route="favorite" :label-id="`son-coeur-${favorite.id}`" />
          </RouterLink>
        </li>
      </ul>
    </section>

    <section class="border-t border-rule pt-5 pb-2" aria-labelledby="ses-seances">
      <h2 id="ses-seances" class="px-4 text-lg font-title">{{ t('memberProfile.sessions') }}</h2>
      <p v-if="!isFriend" class="flex max-w-prose items-start gap-2 px-4 pt-2 pb-4 text-ink-muted">
        <Lock :size="18" class="mt-0.5 shrink-0" aria-hidden="true" />
        {{ lockedMessage }}
      </p>
      <template v-else>
        <RouterLink
          :to="`/membres/${data.member.id}/carnet`"
          class="mx-4 mt-3 flex min-h-14 items-center gap-3 rounded-control border-2 border-ink px-3 py-2 hover:bg-surface-sunken"
        >
          <BookOpen :size="24" class="shrink-0 text-brand-ink" aria-hidden="true" />
          <span class="flex min-w-0 flex-1 flex-col">
            <span class="font-semibold">{{ t('memberProfile.logbook') }}</span>
            <span class="text-sm text-ink-muted">{{ t('memberProfile.logbookHint') }}</span>
          </span>
          <ChevronRight :size="20" class="shrink-0" aria-hidden="true" />
        </RouterLink>
        <div
          v-if="sessions.isPending.value"
          class="flex flex-col gap-3 px-4 py-4"
          aria-hidden="true"
        >
          <SkeletonBlock class="h-24 w-full" />
        </div>
        <div v-else-if="sessions.isError.value" class="flex flex-col items-start gap-3 px-4 py-4">
          <FormAlert>{{ t(errorMessageKey(sessions.error.value)) }}</FormAlert>
          <GgButton variant="secondary" @click="sessions.refetch()">
            {{ t('common.retry') }}
          </GgButton>
        </div>
        <p v-else-if="items.length === 0" class="px-4 pt-4 pb-4 text-ink-muted">
          {{ t('memberProfile.sessionsEmpty') }}
        </p>
        <template v-else>
          <SessionCard
            v-for="session in items"
            :key="session.id"
            :session="session"
            linked
            :max-lines="5"
            :heading-level="3"
          />
          <div ref="sentinel" class="flex justify-center px-4 py-4">
            <GgButton
              v-if="sessions.hasNextPage.value"
              variant="secondary"
              :disabled="sessions.isFetchingNextPage.value"
              @click="sessions.fetchNextPage()"
            >
              {{ sessions.isFetchingNextPage.value ? t('actu.loadingMore') : t('actu.more') }}
            </GgButton>
            <p v-else class="text-sm text-ink-muted">{{ t('actu.end') }}</p>
          </div>
        </template>
      </template>
    </section>
  </template>
</template>
