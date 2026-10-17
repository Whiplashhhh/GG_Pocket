<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import type { Role } from '@/lib/route'

defineProps<{ role: Role }>()

const { t } = useI18n()
</script>

<template>
  <span
    class="inline-block rounded-control bg-olive px-1.5 py-px text-micro font-semibold text-on-olive"
    >{{ t(`roles.${role}`) }}</span
  >
</template>
