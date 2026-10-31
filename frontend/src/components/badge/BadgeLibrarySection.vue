<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import { useBadgeLibrary } from '@/api/badges'
import { errorMessageKey } from '@/api/errors'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'

import BadgeLibrary from './BadgeLibrary.vue'

/** The badge library of a profile: mine without a member id, theirs with one. */
const props = defineProps<{ memberId?: string; title: string; headingId: string }>()

const { t } = useI18n()
const library = useBadgeLibrary(() => props.memberId)
const entries = computed(() => library.data.value?.badges ?? [])
const earned = computed(() => entries.value.filter((entry) => entry.count > 0).length)
</script>

<template>
  <section class="border-t border-rule pt-5 pb-4" :aria-labelledby="headingId">
    <h2 :id="headingId" class="px-4 text-lg font-title">{{ title }}</h2>
    <div v-if="library.isPending.value" class="flex gap-3 px-4 pt-3" aria-hidden="true">
      <SkeletonBlock v-for="index in 4" :key="index" class="size-12 rounded-full" />
    </div>
    <div v-else-if="library.isError.value" class="flex flex-col items-start gap-3 px-4 pt-3">
      <FormAlert>
        {{ t('badges.loadError') }} {{ t(errorMessageKey(library.error.value)) }}
      </FormAlert>
      <GgButton variant="secondary" @click="library.refetch()">{{ t('common.retry') }}</GgButton>
    </div>
    <template v-else>
      <p class="px-4 text-sm text-ink-muted">
        {{ t('badges.earnedCount', { earned, total: entries.length }) }}
      </p>
      <BadgeLibrary :entries="entries" :id-prefix="headingId" />
    </template>
  </section>
</template>
