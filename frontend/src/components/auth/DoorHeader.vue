<script setup lang="ts">
import { ArrowLeft } from 'lucide-vue-next'
import type { RouteLocationRaw } from 'vue-router'

import logoDark from '@/assets/brand/logo-dark.webp'
import logoLight from '@/assets/brand/logo-light.webp'
import GgIconButton from '@/components/ui/GgIconButton.vue'

/** Top of the public screens: the club logo, and a way back when there is one. */
withDefaults(
  defineProps<{
    title: string
    intro?: string
    back?: RouteLocationRaw
    backLabel?: string
    /** Big logo for the welcome screen. */
    large?: boolean
  }>(),
  { intro: undefined, back: undefined, backLabel: undefined, large: false },
)
</script>

<template>
  <div class="px-4 pt-3">
    <div class="flex h-11 items-center">
      <GgIconButton v-if="back !== undefined" :to="back" :label="backLabel ?? ''" class="-ml-3">
        <ArrowLeft :size="24" aria-hidden="true" />
      </GgIconButton>
    </div>
    <div class="flex flex-col items-center gap-4 pt-2 pb-6 text-center">
      <img
        :src="logoLight"
        alt=""
        :width="large ? 168 : 88"
        :height="large ? 166 : 87"
        class="dark:hidden"
        :class="large ? 'size-42' : 'size-22'"
      />
      <img
        :src="logoDark"
        alt=""
        :width="large ? 168 : 88"
        :height="large ? 166 : 87"
        class="hidden dark:block"
        :class="large ? 'size-42' : 'size-22'"
      />
      <h1 class="font-title text-balance" :class="large ? 'text-3xl' : 'text-2xl'">{{ title }}</h1>
      <p v-if="intro" class="max-w-prose text-ink-muted">{{ intro }}</p>
    </div>
  </div>
</template>
