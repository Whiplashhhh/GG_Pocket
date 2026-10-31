<script setup lang="ts">
import { ArrowDown, ArrowUp, ChevronRight, Search } from 'lucide-vue-next'
import { computed, ref, useId } from 'vue'
import { useI18n } from 'vue-i18n'

import { useMembers, type MemberSummary } from '@/api/admin'
import { errorMessageKey } from '@/api/errors'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import TextField from '@/components/ui/TextField.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { formatDate, formatRelative } from '@/lib/format'
import {
  awaitingValidation,
  filterMembers,
  sortMembers,
  type MemberSortKey,
  type SortDirection,
} from '@/lib/members'

import ManagementTitle from './ManagementTitle.vue'

/**
 * Spec §8.11: searchable, sortable members table; a list on phones, a table on large screens. New
 * accounts awaiting validation come first, apart (D-107).
 */
const COLUMNS: MemberSortKey[] = [
  'name',
  'nickname',
  'role',
  'status',
  'registeredAt',
  'lastSeenAt',
  'sessionCount',
]

/** Dates and counts: the most recent, or the most, first. */
const DESCENDING_FIRST = new Set<MemberSortKey>(['registeredAt', 'lastSeenAt', 'sessionCount'])

const { t } = useI18n()
const members = useMembers()
const query = ref('')
const sortKey = ref<MemberSortKey>('name')
const direction = ref<SortDirection>('asc')
const sortSelectId = useId()

const pending = computed(() => awaitingValidation(members.data.value ?? []))
const pendingTitleId = useId()

const shown = computed(() =>
  sortMembers(filterMembers(members.data.value ?? [], query.value), sortKey.value, direction.value),
)

function sortBy(key: MemberSortKey) {
  if (sortKey.value === key) {
    direction.value = direction.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortKey.value = key
    // Dates read best newest first.
    direction.value = DESCENDING_FIRST.has(key) ? 'desc' : 'asc'
  }
}

function ariaSort(key: MemberSortKey): 'ascending' | 'descending' | 'none' {
  if (sortKey.value !== key) return 'none'
  return direction.value === 'asc' ? 'ascending' : 'descending'
}

function fullName(member: MemberSummary): string {
  return `${member.firstName} ${member.lastName}`
}
</script>

<template>
  <ManagementTitle>{{ t('members.title') }}</ManagementTitle>

  <section
    v-if="pending.length > 0"
    class="mx-4 mt-4 border-l-4 border-brand bg-surface lg:mx-0"
    :aria-labelledby="pendingTitleId"
  >
    <div class="px-4 pt-3">
      <h2 :id="pendingTitleId" class="text-lg font-title">
        {{ t('members.pendingTitle') }}
        <span class="font-sans text-base font-semibold text-ink-muted tabular-nums"
          >({{ pending.length }})</span
        >
      </h2>
      <p class="text-sm text-ink-muted">{{ t('members.pendingIntro') }}</p>
    </div>
    <ul class="mt-2">
      <li v-for="member in pending" :key="member.id">
        <RouterLink
          :to="`/gestion/membres/${member.id}`"
          class="flex items-center gap-3 border-t border-rule px-4 py-3 hover:bg-surface-sunken"
        >
          <UserAvatar
            :first-name="member.firstName"
            :last-name="member.lastName"
            :seed="member.nickname"
            :src="member.avatarUrl"
          />
          <span class="flex min-w-0 flex-1 flex-col items-start gap-0.5">
            <span class="truncate font-semibold">{{ fullName(member) }}</span>
            <span class="truncate text-sm text-ink-muted">@{{ member.nickname }}</span>
            <span class="flex flex-wrap gap-x-2 text-sm text-ink-muted">
              <span v-if="member.declaredLicensed === true">{{
                t('members.declaredLicensed')
              }}</span>
              <span v-else-if="member.declaredLicensed === false">{{
                t('members.declaredVisitor')
              }}</span>
              <span>{{ formatRelative(member.registeredAt) }}</span>
            </span>
          </span>
          <ChevronRight :size="20" class="text-ink-muted" aria-hidden="true" />
        </RouterLink>
      </li>
    </ul>
  </section>

  <div class="flex flex-col gap-3 px-4 py-4 lg:flex-row lg:items-end lg:px-0">
    <div class="lg:w-80">
      <TextField
        v-model="query"
        type="search"
        :label="t('members.search')"
        :placeholder="t('members.searchPlaceholder')"
        inputmode="search"
        optional
        :show-optional-mark="false"
        verbatim
      >
        <template #trailing>
          <Search :size="20" class="mr-3 text-ink-muted" aria-hidden="true" />
        </template>
      </TextField>
    </div>
    <label :for="sortSelectId" class="flex flex-col gap-1.5 lg:hidden">
      <span class="font-semibold">{{ t('members.sortBy') }}</span>
      <select
        :id="sortSelectId"
        v-model="sortKey"
        class="min-h-12 rounded-control border-2 border-rule bg-surface px-3 text-base"
      >
        <option v-for="key in COLUMNS" :key="key" :value="key">
          {{ t(`members.sort.${key}`) }}
        </option>
      </select>
    </label>
    <p class="text-sm text-ink-muted lg:ml-auto" role="status">
      {{ t('members.count', shown.length) }}
    </p>
  </div>

  <div v-if="members.isPending.value" class="flex flex-col gap-3 px-4 lg:px-0" aria-hidden="true">
    <SkeletonBlock v-for="index in 5" :key="index" class="h-14 w-full" />
  </div>
  <div v-else-if="members.isError.value" class="flex flex-col items-start gap-3 px-4 lg:px-0">
    <FormAlert>{{ t(errorMessageKey(members.error.value)) }}</FormAlert>
    <GgButton variant="secondary" @click="members.refetch()">{{ t('common.retry') }}</GgButton>
  </div>
  <p v-else-if="shown.length === 0" class="px-4 py-6 text-ink-muted lg:px-0">
    {{ t('members.noMatch', { query }) }}
  </p>

  <template v-else>
    <!-- Phones: one line per member. -->
    <ul class="border-t border-rule lg:hidden">
      <li v-for="member in shown" :key="member.id">
        <RouterLink
          :to="`/gestion/membres/${member.id}`"
          class="flex items-center gap-3 border-b border-rule px-4 py-3 hover:bg-surface-sunken"
        >
          <UserAvatar
            :first-name="member.firstName"
            :last-name="member.lastName"
            :seed="member.nickname"
            :src="member.avatarUrl"
          />
          <span class="flex min-w-0 flex-1 flex-col items-start gap-0.5">
            <span class="truncate font-semibold">{{ fullName(member) }}</span>
            <span class="truncate text-sm text-ink-muted">@{{ member.nickname }}</span>
            <span class="flex gap-2">
              <span class="text-sm text-ink-muted">{{ t(`roleNames.${member.role}`) }}</span>
              <span class="text-sm text-ink-muted">{{
                t('members.sessions', member.sessionCount)
              }}</span>
              <span
                v-if="member.status === 'DISABLED'"
                class="rounded-control border border-danger px-1.5 text-micro font-semibold text-danger"
                >{{ t('members.disabledChip') }}</span
              >
              <span
                v-else-if="!member.validated"
                class="rounded-control border border-ink px-1.5 text-micro font-semibold text-ink"
                >{{ t('members.pendingChip') }}</span
              >
            </span>
          </span>
          <ChevronRight :size="20" class="text-ink-muted" aria-hidden="true" />
        </RouterLink>
      </li>
    </ul>

    <!-- Large screens: a sortable table. -->
    <table class="hidden w-full border-collapse text-left lg:table">
      <thead>
        <tr class="border-b-2 border-ink">
          <th
            v-for="key in COLUMNS"
            :key="key"
            scope="col"
            :aria-sort="ariaSort(key)"
            class="py-2 pr-3 font-semibold"
          >
            <button
              type="button"
              class="inline-flex min-h-11 items-center gap-1 hover:text-brand-ink"
              @click="sortBy(key)"
            >
              {{ t(`members.columns.${key}`) }}
              <ArrowUp v-if="ariaSort(key) === 'ascending'" :size="16" aria-hidden="true" />
              <ArrowDown v-else-if="ariaSort(key) === 'descending'" :size="16" aria-hidden="true" />
            </button>
          </th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="member in shown"
          :key="member.id"
          class="border-b border-rule hover:bg-surface-sunken"
        >
          <td class="py-2 pr-3">
            <RouterLink
              :to="`/gestion/membres/${member.id}`"
              class="flex min-h-11 items-center gap-3 font-semibold hover:underline"
            >
              <UserAvatar
                :first-name="member.firstName"
                :last-name="member.lastName"
                :seed="member.nickname"
                :src="member.avatarUrl"
                :size="32"
              />
              {{ member.firstName }} {{ member.lastName }}
            </RouterLink>
          </td>
          <td class="py-2 pr-3 text-ink-muted">@{{ member.nickname }}</td>
          <td class="py-2 pr-3">
            {{ t(`roleNames.${member.role}`) }}
            <span
              v-if="!member.validated"
              class="ml-1 rounded-control border border-ink px-1.5 text-micro font-semibold text-ink"
              >{{ t('members.pendingChip') }}</span
            >
          </td>
          <td
            class="py-2 pr-3"
            :class="{ 'font-semibold text-danger': member.status === 'DISABLED' }"
          >
            {{ t(`members.status.${member.status}`) }}
          </td>
          <td class="py-2 pr-3 tabular-nums">{{ formatDate(member.registeredAt) }}</td>
          <td class="py-2 pr-3 text-ink-muted">
            {{ member.lastSeenAt ? formatRelative(member.lastSeenAt) : t('members.never') }}
          </td>
          <td class="py-2 pr-3 tabular-nums">{{ member.sessionCount }}</td>
        </tr>
      </tbody>
    </table>
  </template>
</template>
