<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import EmptyState from '@/components/ui/EmptyState.vue'
import GgButton from '@/components/ui/GgButton.vue'
import { collectLoadDiagnostics, resetAppCaches } from '@/lib/load-diagnostics'
import { forgetReload, type ScreenLoadFailure } from '@/lib/stale-chunk'

/**
 * A screen's code still fails right after a reload: say so instead of a blank page, offer a way
 * out, and show what to send to whoever looks after the app (docs/DECISIONS.md D-073).
 */
const props = defineProps<{ failure: ScreenLoadFailure }>()
const { t } = useI18n()
const details = ref<string[]>([])
const busy = ref(false)
const copied = ref(false)

onMounted(async () => {
  details.value = await collectLoadDiagnostics(props.failure).catch((error: unknown) => [
    `diagnostic impossible : ${String(error)}`,
  ])
})

function retry() {
  forgetReload()
  window.location.assign(props.failure.path)
}

/** A full load: after a failed first navigation, the router has no page to go back from. */
function goHome() {
  forgetReload()
  window.location.assign('/actu')
}

async function repair() {
  busy.value = true
  await resetAppCaches().catch(() => undefined)
  retry()
}

async function copy() {
  await navigator.clipboard.writeText(details.value.join('\n'))
  copied.value = true
}
</script>

<template>
  <main id="contenu" tabindex="-1" class="mx-auto max-w-2xl outline-none">
    <EmptyState
      :title="t('loadFailure.title')"
      :body="t('loadFailure.body')"
      :hold="{ label: '', hex: '#9a9a9a' }"
      :heading-level="1"
    >
      <div class="flex flex-wrap gap-3">
        <GgButton :disabled="busy" @click="retry">{{ t('common.retry') }}</GgButton>
        <GgButton variant="secondary" :disabled="busy" @click="repair">
          {{ t('loadFailure.repair') }}
        </GgButton>
        <GgButton variant="ghost" :disabled="busy" @click="goHome">
          {{ t('loadFailure.home') }}
        </GgButton>
      </div>
      <details class="mt-6">
        <summary class="min-h-11 cursor-pointer py-2 font-semibold">
          {{ t('loadFailure.details') }}
        </summary>
        <pre
          class="mt-2 overflow-x-auto rounded-control bg-surface-sunken p-3 text-xs break-all whitespace-pre-wrap"
          >{{ details.join('\n') }}</pre>
        <GgButton variant="ghost" class="mt-2" :disabled="details.length === 0" @click="copy">
          {{ copied ? t('common.copied') : t('common.copy') }}
        </GgButton>
      </details>
    </EmptyState>
  </main>
</template>
