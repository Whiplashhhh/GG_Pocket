<script setup lang="ts">
import { Eye, EyeOff } from 'lucide-vue-next'
import { ref, useTemplateRef } from 'vue'
import { useI18n } from 'vue-i18n'

import TextField from './TextField.vue'

/** Password input with a show/hide toggle; autocomplete lets password managers do their job. */
withDefaults(
  defineProps<{
    label: string
    autocomplete: 'current-password' | 'new-password'
    hint?: string
    error?: string
  }>(),
  { hint: undefined, error: undefined },
)

const model = defineModel<string>({ required: true })
const { t } = useI18n()
const visible = ref(false)
const field = useTemplateRef<InstanceType<typeof TextField>>('field')

defineExpose({ focus: () => field.value?.focus() })
</script>

<template>
  <TextField
    ref="field"
    v-model="model"
    :label="label"
    :type="visible ? 'text' : 'password'"
    :autocomplete="autocomplete"
    :hint="hint"
    :error="error"
    :maxlength="200"
    verbatim
  >
    <template #trailing>
      <button
        type="button"
        class="inline-flex size-11 items-center justify-center rounded-control text-ink-muted hover:text-ink"
        :aria-label="visible ? t('common.hidePassword') : t('common.showPassword')"
        :aria-pressed="visible"
        @click="visible = !visible"
      >
        <EyeOff v-if="visible" :size="22" aria-hidden="true" />
        <Eye v-else :size="22" aria-hidden="true" />
      </button>
    </template>
  </TextField>
</template>
