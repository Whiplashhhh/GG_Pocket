<script setup lang="ts">
import { computed } from 'vue'

import { avatarColor, initialsOf } from '@/lib/avatar'

const props = withDefaults(
  defineProps<{
    firstName: string
    lastName: string
    /** Stable seed for the background colour (the nickname). */
    seed: string
    src?: string
    size?: number
  }>(),
  { src: undefined, size: 40 },
)

const initials = computed(() => initialsOf(props.firstName, props.lastName))
const background = computed(() => avatarColor(props.seed))
</script>

<template>
  <img
    v-if="src"
    :src="src"
    alt=""
    :width="size"
    :height="size"
    class="shrink-0 rounded-full object-cover"
  />
  <span
    v-else
    class="inline-flex shrink-0 items-center justify-center rounded-full font-bold text-[#17140f]"
    :style="{
      width: `${size}px`,
      height: `${size}px`,
      backgroundColor: background,
      fontSize: `${Math.round(size * 0.4)}px`,
    }"
    aria-hidden="true"
    >{{ initials }}</span
  >
</template>
