<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import { useMetaQuery } from '@/api/meta'

const { t } = useI18n()
const { data: meta } = useMetaQuery()

const SHORT_COMMIT_LENGTH = 7
const visible = computed(() => meta.value?.environment === 'dev')
const text = computed(() =>
  meta.value
    ? t('devBanner.text', {
        branch: meta.value.branch,
        commit: meta.value.commit.slice(0, SHORT_COMMIT_LENGTH),
      })
    : '',
)
</script>

<template>
  <p
    v-if="visible"
    class="bg-brand px-4 py-1 pt-safe text-center text-micro font-semibold text-on-brand"
    role="note"
  >
    {{ text }}
  </p>
</template>
