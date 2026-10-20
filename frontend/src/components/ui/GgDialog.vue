<script setup lang="ts">
import {
  DialogContent,
  DialogDescription,
  DialogOverlay,
  DialogPortal,
  DialogRoot,
  DialogTitle,
} from 'reka-ui'

/**
 * A sheet that slides up from the bottom on phones and sits in the middle on larger screens.
 * Focus is trapped inside while it is open and comes back where it was afterwards.
 */
defineProps<{ title: string; description?: string }>()

const open = defineModel<boolean>('open', { required: true })
</script>

<template>
  <DialogRoot v-model:open="open">
    <DialogPortal>
      <DialogOverlay class="fixed inset-0 z-40 bg-[rgb(23_20_15/0.45)]" />
      <DialogContent
        class="fixed inset-x-0 bottom-0 z-50 max-h-[90dvh] overflow-y-auto rounded-t-sheet bg-surface px-4 pt-5 pb-[max(1.25rem,env(safe-area-inset-bottom))] shadow-sheet sm:inset-x-auto sm:top-1/2 sm:left-1/2 sm:w-[28rem] sm:-translate-x-1/2 sm:-translate-y-1/2 sm:rounded-sheet sm:pb-5"
      >
        <DialogTitle class="text-xl font-title text-balance">{{ title }}</DialogTitle>
        <DialogDescription v-if="description" class="mt-2 text-ink-muted">
          {{ description }}
        </DialogDescription>
        <div class="mt-4"><slot /></div>
      </DialogContent>
    </DialogPortal>
  </DialogRoot>
</template>
