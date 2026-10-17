<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, type RouteLocationRaw } from 'vue-router'

const props = withDefaults(
  defineProps<{
    variant?: 'primary' | 'secondary' | 'ghost' | 'danger'
    size?: 'md' | 'lg'
    /** Renders a link (RouterLink) instead of a button. */
    to?: RouteLocationRaw
    type?: 'button' | 'submit'
    disabled?: boolean
    block?: boolean
  }>(),
  { variant: 'primary', size: 'md', to: undefined, type: 'button', disabled: false, block: false },
)

const VARIANTS = {
  primary: 'bg-brand text-on-brand hover:brightness-95',
  secondary: 'border-2 border-ink text-ink hover:bg-surface-sunken',
  ghost: 'text-ink underline-offset-4 hover:underline',
  danger: 'bg-danger text-on-danger hover:brightness-95',
} as const

const SIZES = { md: 'min-h-11 px-4 text-base', lg: 'min-h-12 px-5 text-lg' } as const

const classes = computed(() => [
  'inline-flex items-center justify-center gap-2 rounded-control font-semibold transition-[filter,background-color] duration-150 active:translate-y-px disabled:cursor-not-allowed disabled:opacity-50',
  VARIANTS[props.variant],
  SIZES[props.size],
  { 'w-full': props.block },
])
</script>

<template>
  <RouterLink v-if="to !== undefined" :to="to" :class="classes"><slot /></RouterLink>
  <button v-else :type="type" :disabled="disabled" :class="classes"><slot /></button>
</template>
