<script setup lang="ts">
import AppShell from '@/components/layout/AppShell.vue'
import { useThemeStore } from '@/stores/theme'

// Applies the stored theme preference and keeps <html data-theme> in sync.
useThemeStore()
</script>

<template>
  <AppShell />
</template>
