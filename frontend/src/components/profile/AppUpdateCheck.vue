<script setup lang="ts">
import { RefreshCw } from 'lucide-vue-next'
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

import GgButton from '@/components/ui/GgButton.vue'
import { checkForUpdate, type UpdateCheck } from '@/lib/appUpdate'

/**
 * « Chercher une mise à jour » (spec §8.12): looks now instead of waiting for the next start, and
 * says plainly what came of it, files refused by the phone included (docs/DECISIONS.md D-091).
 */
const { t } = useI18n()
const checking = ref(false)
const result = ref<UpdateCheck | null>(null)
const copied = ref(false)

async function check() {
  checking.value = true
  copied.value = false
  result.value = null
  result.value = await checkForUpdate()
  checking.value = false
}

async function copy(lines: string[]) {
  await navigator.clipboard.writeText(
    [`version ${__APP_VERSION__}`, navigator.userAgent, ...lines].join('\n'),
  )
  copied.value = true
}
</script>

<template>
  <div class="flex flex-col items-start gap-2">
    <GgButton variant="ghost" class="-ml-4" :disabled="checking" @click="check">
      <RefreshCw
        :size="18"
        aria-hidden="true"
        :class="{ 'animate-spin motion-reduce:animate-none': checking }"
      />
      {{ checking ? t('appUpdate.checking') : t('appUpdate.check') }}
    </GgButton>
    <p v-if="result" role="status" class="text-ink">
      {{ t(`appUpdate.result.${result.kind}`) }}
    </p>
    <template v-if="result?.kind === 'failed' || result?.kind === 'error'">
      <pre
        class="w-full overflow-x-auto rounded-control bg-surface-sunken p-3 text-xs break-all whitespace-pre-wrap"
        >{{
          (result.kind === 'failed' ? result.refused : [result.message]).join('\n') ||
          t('appUpdate.noRefusedFile')
        }}</pre>
      <GgButton
        variant="ghost"
        @click="copy(result.kind === 'failed' ? result.refused : [result.message])"
      >
        {{ copied ? t('common.copied') : t('common.copy') }}
      </GgButton>
    </template>
  </div>
</template>
