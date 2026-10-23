<script setup lang="ts">
import {
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogOverlay,
  AlertDialogPortal,
  AlertDialogRoot,
  AlertDialogTitle,
} from 'reka-ui'
import { useI18n } from 'vue-i18n'

/** "Are you sure?" before something that cannot be undone in one tap. */
withDefaults(
  defineProps<{
    title: string
    body: string
    confirmLabel: string
    danger?: boolean
  }>(),
  { danger: false },
)

const emit = defineEmits<{ confirm: [] }>()
const open = defineModel<boolean>('open', { required: true })
const { t } = useI18n()
</script>

<template>
  <AlertDialogRoot v-model:open="open">
    <AlertDialogPortal>
      <AlertDialogOverlay class="fixed inset-0 z-40 bg-[rgb(23_20_15/0.45)]" />
      <AlertDialogContent
        class="fixed inset-x-0 bottom-0 z-50 rounded-t-sheet bg-surface px-4 pt-5 pb-[max(1.25rem,env(safe-area-inset-bottom))] shadow-sheet sm:inset-x-auto sm:top-1/2 sm:bottom-auto sm:left-1/2 sm:w-[26rem] sm:-translate-x-1/2 sm:-translate-y-1/2 sm:rounded-sheet sm:pb-5"
      >
        <AlertDialogTitle class="text-xl font-title text-balance">{{ title }}</AlertDialogTitle>
        <AlertDialogDescription class="mt-2 text-ink-muted">{{ body }}</AlertDialogDescription>
        <div class="mt-5 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
          <AlertDialogCancel
            class="inline-flex min-h-11 items-center justify-center rounded-control border-2 border-ink px-4 font-semibold text-ink hover:bg-surface-sunken"
          >
            {{ t('common.cancel') }}
          </AlertDialogCancel>
          <AlertDialogAction
            class="inline-flex min-h-11 items-center justify-center rounded-control px-4 font-semibold hover:brightness-95"
            :class="danger ? 'bg-danger text-on-danger' : 'bg-brand text-on-brand'"
            @click="emit('confirm')"
          >
            {{ confirmLabel }}
          </AlertDialogAction>
        </div>
      </AlertDialogContent>
    </AlertDialogPortal>
  </AlertDialogRoot>
</template>
