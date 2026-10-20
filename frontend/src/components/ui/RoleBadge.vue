<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import type { Role } from '@/lib/route'

withDefaults(
  defineProps<{
    role: Role
    /** Short label, for the header where the full one does not fit at phone width. */
    short?: boolean
  }>(),
  { short: false },
)

const { t } = useI18n()
</script>

<template>
  <span
    class="inline-block rounded-control bg-olive px-1.5 py-px text-micro font-semibold text-on-olive"
    >{{ short ? t(`rolesShort.${role}`) : t(`roles.${role}`) }}</span
  >
</template>
