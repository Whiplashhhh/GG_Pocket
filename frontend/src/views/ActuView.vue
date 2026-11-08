<script setup lang="ts">
import { ChevronRight, Users } from 'lucide-vue-next'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

import { useAwaitingValidation, useCurrentMember } from '@/api/auth'
import { useClubFeed, useFeed } from '@/api/feed'
import { useMySessions } from '@/api/sessions'
import { useDirectory } from '@/api/social'
import AwaitingValidationNotice from '@/components/member/AwaitingValidationNotice.vue'
import ClubFeedTeaser from '@/components/social/ClubFeedTeaser.vue'
import SessionFeed from '@/components/social/SessionFeed.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SegmentedControl from '@/components/ui/SegmentedControl.vue'

/**
 * The feed (spec §8.2): the club header with its climber count (the way to the directory), then
 * three tabs (D-120). « Juste moi »: my sessions only. « Mes amis »: my sessions and the ones my
 * friends share, latest first. « Le club »: the "Tout le club" sessions of the members who take
 * part, for a member who takes part; blurred templates and the way in otherwise. `?moi` and `?club`
 * in the address open those tabs, like `?amis` in the directory. Friend requests wait on my profile (D-099). An account awaiting validation sees
 * its own sessions only, and why (D-107).
 */
type Tab = 'me' | 'friends' | 'club'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const member = useCurrentMember()
const directory = useDirectory()
const awaiting = useAwaitingValidation()

const tab = computed<Tab>({
  get: () => {
    if (route.query.moi !== undefined) return 'me'
    return route.query.club === undefined ? 'friends' : 'club'
  },
  set: (value) =>
    void router.replace({
      query: value === 'friends' ? {} : value === 'me' ? { moi: null } : { club: null },
    }),
})
const tabs = computed(() => [
  { value: 'me' as const, label: t('actu.tabMe') },
  { value: 'friends' as const, label: t('actu.tabFriends') },
  { value: 'club' as const, label: t('actu.tabClub') },
])

const inClubFeed = computed(() => member.value?.inClubFeed === true)
const myFeed = useMySessions(() => tab.value === 'me')
const friendsFeed = useFeed()
const clubFeed = useClubFeed(() => inClubFeed.value && tab.value === 'club')
</script>

<template>
  <h1 class="sr-only">{{ t('actu.title') }}</h1>
  <section class="border-b border-rule px-4 py-3">
    <p class="text-ink-muted">{{ t('actu.clubTagline') }}</p>
    <AwaitingValidationNotice
      v-if="awaiting"
      class="mt-3"
      :title="t('awaiting.title')"
      :body="t('awaiting.locked')"
    />
    <RouterLink
      v-else
      to="/annuaire"
      class="-mx-1 mt-1 inline-flex min-h-11 items-center gap-2 rounded-control px-1 font-semibold text-brand-ink hover:underline"
    >
      <Users :size="20" aria-hidden="true" />
      <template v-if="directory.data.value">
        {{ t('actu.clubCount', directory.data.value.climberCount) }}
        <span class="sr-only">{{ t('actu.clubCountHintSuffix') }}</span>
      </template>
      <template v-else>{{ t('actu.clubCountHint') }}</template>
      <ChevronRight :size="18" aria-hidden="true" />
    </RouterLink>
    <SegmentedControl v-model="tab" class="mt-3" :label="t('actu.tabs')" :options="tabs" stretch />
  </section>

  <SessionFeed
    v-if="tab === 'me'"
    :feed="myFeed"
    :heading="t('actu.myFeed')"
    :empty-title="t('actu.myEmptyTitle')"
    :empty-body="t('actu.myEmptyBody')"
  >
    <GgButton variant="secondary" to="/seance/nouvelle">{{ t('actu.newSession') }}</GgButton>
  </SessionFeed>
  <SessionFeed
    v-else-if="tab === 'friends'"
    :feed="friendsFeed"
    :heading="t('actu.feed')"
    :empty-title="t('actu.emptyTitle')"
    :empty-body="t('actu.emptyBody')"
  >
    <GgButton v-if="!awaiting" variant="secondary" to="/annuaire">{{
      t('actu.findFriends')
    }}</GgButton>
  </SessionFeed>
  <SessionFeed
    v-else-if="inClubFeed"
    :feed="clubFeed"
    :heading="t('actu.clubFeed')"
    :empty-title="t('actu.clubEmptyTitle')"
    :empty-body="t('actu.clubEmptyBody')"
  />
  <ClubFeedTeaser v-else :awaiting="awaiting" />
</template>
