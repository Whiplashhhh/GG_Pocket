<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import { useAuditLog, type AuditEntry } from '@/api/admin'
import { errorMessageKey } from '@/api/errors'
import EmptyState from '@/components/ui/EmptyState.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import { formatDateTime } from '@/lib/format'
import type { Role } from '@/lib/route'

import ManagementTitle from './ManagementTitle.vue'

/** Spec §5: who did what, when, before and after. Read-only for the board and the admin. */
const { t } = useI18n()
const audit = useAuditLog()
const entries = computed(() => audit.data.value?.pages.flatMap((page) => page.entries) ?? [])

const ROLE_VALUES = new Set(['CLIMBER', 'SETTER', 'BOARD', 'ADMIN'])

/** Role changes store role codes: shown with their names. */
function value(raw: string | undefined): string {
  if (raw === undefined) return '—'
  return ROLE_VALUES.has(raw) ? t(`roleNames.${raw as Role}`) : raw
}

function sentence(entry: AuditEntry): string {
  return t(`audit.actions.${entry.action}`, {
    actor: entry.actorNickname ?? t('audit.formerMember'),
    target: entry.targetLabel,
    before: value(entry.before),
    after: value(entry.after),
  })
}
</script>

<template>
  <ManagementTitle>{{ t('audit.title') }}</ManagementTitle>
  <p class="px-4 pt-1 pb-4 text-ink-muted lg:px-0">{{ t('audit.intro') }}</p>

  <div v-if="audit.isPending.value" class="flex flex-col gap-3 px-4 lg:px-0" aria-hidden="true">
    <SkeletonBlock v-for="index in 4" :key="index" class="h-12 w-full" />
  </div>
  <div v-else-if="audit.isError.value" class="flex flex-col items-start gap-3 px-4 lg:px-0">
    <FormAlert>{{ t(errorMessageKey(audit.error.value)) }}</FormAlert>
    <GgButton variant="secondary" @click="audit.refetch()">{{ t('common.retry') }}</GgButton>
  </div>
  <EmptyState v-else-if="entries.length === 0" :title="t('audit.empty')" body="" />

  <template v-else>
    <ol class="border-t border-rule">
      <li
        v-for="entry in entries"
        :key="entry.id"
        class="flex flex-col gap-0.5 border-b border-rule px-4 py-3 lg:flex-row lg:gap-6 lg:px-0"
      >
        <p class="flex shrink-0 gap-2 text-sm text-ink-muted lg:w-60">
          <time :datetime="entry.occurredAt" class="tabular-nums">{{
            formatDateTime(entry.occurredAt)
          }}</time>
          <span aria-hidden="true">·</span>
          <span>{{ t(`audit.targets.${entry.targetType}`) }}</span>
        </p>
        <p>{{ sentence(entry) }}</p>
      </li>
    </ol>
    <div v-if="audit.hasNextPage.value" class="px-4 py-5 lg:px-0">
      <GgButton
        variant="secondary"
        :disabled="audit.isFetchingNextPage.value"
        @click="audit.fetchNextPage()"
      >
        {{ t('audit.loadMore') }}
      </GgButton>
    </div>
  </template>
</template>
