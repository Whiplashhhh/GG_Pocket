<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

import { errorMessageKey } from '@/api/errors'
import { useDirectory } from '@/api/social'
import SubPageHeader from '@/components/layout/SubPageHeader.vue'
import MemberRow from '@/components/social/MemberRow.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SegmentedControl from '@/components/ui/SegmentedControl.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import TextField from '@/components/ui/TextField.vue'
import { filterMembers } from '@/lib/members'

/**
 * The club directory (spec §8.2): search by name or nickname, ask, answer, see who is a friend.
 * `?amis` in the address opens it on my friends.
 */
type Shown = 'all' | 'friends'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const directory = useDirectory()
const query = ref('')

const shown = computed<Shown>({
  get: () => (route.query.amis === undefined ? 'all' : 'friends'),
  set: (value) => void router.replace({ query: value === 'friends' ? { amis: null } : {} }),
})

const entries = computed(() => directory.data.value?.members ?? [])
const friendCount = computed(
  () => entries.value.filter((entry) => entry.friendship === 'FRIENDS').length,
)
const options = computed(() => [
  { value: 'all' as const, label: t('directory.filterAll') },
  { value: 'friends' as const, label: t('directory.filterFriends', { n: friendCount.value }) },
])

const results = computed(() => {
  const pool =
    shown.value === 'friends'
      ? entries.value.filter((entry) => entry.friendship === 'FRIENDS')
      : entries.value
  const found = new Set(
    filterMembers(
      pool.map((entry) => entry.member),
      query.value,
    ),
  )
  return pool.filter((entry) => found.has(entry.member))
})
</script>

<template>
  <SubPageHeader back="/actu" :back-label="t('common.back')">
    {{ t('directory.title') }}
  </SubPageHeader>
  <p class="max-w-prose px-4 pt-1 text-ink-muted">{{ t('directory.intro') }}</p>

  <div class="flex flex-col gap-3 px-4 pt-4 pb-2">
    <TextField
      v-model="query"
      type="search"
      inputmode="search"
      :label="t('directory.search')"
      :placeholder="t('directory.searchHint')"
    />
    <SegmentedControl v-model="shown" :label="t('directory.filter')" :options="options" stretch />
  </div>

  <div v-if="directory.isPending.value" class="flex flex-col gap-4 px-4 py-4" aria-hidden="true">
    <SkeletonBlock v-for="n in 5" :key="n" class="h-12 w-full" />
  </div>
  <div v-else-if="directory.isError.value" class="flex flex-col items-start gap-3 px-4 py-4">
    <FormAlert>
      {{ t('directory.loadError') }} {{ t(errorMessageKey(directory.error.value)) }}
    </FormAlert>
    <GgButton variant="secondary" @click="directory.refetch()">{{ t('common.retry') }}</GgButton>
  </div>
  <template v-else>
    <p class="px-4 pt-2 text-sm text-ink-muted" role="status">
      {{ t('directory.count', results.length) }}
    </p>
    <EmptyState
      v-if="entries.length === 0"
      :title="t('directory.aloneTitle')"
      :body="t('directory.aloneBody')"
    />
    <EmptyState
      v-else-if="results.length === 0 && query.trim() === '' && shown === 'friends'"
      :title="t('directory.noFriendsTitle')"
      :body="t('directory.noFriendsBody')"
    />
    <EmptyState
      v-else-if="results.length === 0"
      :title="t('directory.noMatchTitle')"
      :body="t('directory.noMatchBody')"
    />
    <ul v-else class="divide-y divide-rule pb-6">
      <li v-for="entry in results" :key="entry.member.id">
        <MemberRow :member="entry.member" :friendship="entry.friendship" />
      </li>
    </ul>
  </template>
</template>
