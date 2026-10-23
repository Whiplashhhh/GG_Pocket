<script setup lang="ts">
import { computed, ref, useId, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import type { HoldColorConfig } from '@/api/topo'
import { useCreateHoldColor, useUpdateHoldColor } from '@/api/topoAdmin'
import CheckboxField from '@/components/ui/CheckboxField.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import GgDialog from '@/components/ui/GgDialog.vue'
import HoldSwatch from '@/components/ui/HoldSwatch.vue'
import TextField from '@/components/ui/TextField.vue'
import {
  COLOR_LABEL_MAX,
  HEX_PATTERN,
  holdColorSettings,
  toHoldColorForm,
  type HoldColorForm,
} from '@/lib/clubConfig'
import { focusFirstInvalid, serverFieldErrors, type FieldErrors, type Message } from '@/lib/forms'

/**
 * Adds a hold colour (`color` null) or changes one. The swatch is previewed on a light and a dark
 * background: a colour that vanishes on one of them is better caught here than on the wall.
 */
const props = defineProps<{ color: HoldColorConfig | null }>()
const emit = defineEmits<{ saved: [message: string] }>()
const open = defineModel<boolean>('open', { required: true })

const { t } = useI18n()
const create = useCreateHoldColor()
const update = useUpdateHoldColor()
const form = ref<HoldColorForm>(toHoldColorForm(undefined))
const errors = ref<FieldErrors<keyof HoldColorForm>>({})
const general = ref<Message | null>(null)
const pickerId = useId()

watch(open, (isOpen) => {
  if (!isOpen) return
  form.value = toHoldColorForm(props.color ?? undefined)
  errors.value = {}
  general.value = null
})

/** The native picker only speaks lower case #rrggbb. */
const pickerValue = computed(() =>
  HEX_PATTERN.test(form.value.hex.trim()) ? form.value.hex.trim().toLowerCase() : '#808080',
)
function picked(event: Event) {
  form.value.hex = (event.target as HTMLInputElement).value.toUpperCase()
}

const preview = computed(() => ({
  label: form.value.label,
  hex: pickerValue.value,
  multicolor: form.value.multicolor,
}))

function refused(error: unknown) {
  const split = serverFieldErrors(
    error,
    ['label', 'hex', 'multicolor', 'active'] as const,
    {},
    { ALREADY_EXISTS: 'label' },
  )
  errors.value = split.fields
  general.value = split.general
  focusFirstInvalid()
}

function save() {
  const result = holdColorSettings(form.value)
  if ('errors' in result) {
    errors.value = result.errors
    focusFirstInvalid()
    return
  }
  const done = (
    color: HoldColorConfig,
    key: 'clubConfig.colorCreated' | 'clubConfig.colorUpdated',
  ) => {
    open.value = false
    emit('saved', t(key, { label: color.label }))
  }
  if (props.color === null) {
    create.mutate(result.settings, {
      onSuccess: (color) => {
        done(color, 'clubConfig.colorCreated')
      },
      onError: refused,
    })
  } else {
    update.mutate(
      { id: props.color.id, settings: result.settings },
      {
        onSuccess: (color) => {
          done(color, 'clubConfig.colorUpdated')
        },
        onError: refused,
      },
    )
  }
}

function text(message: Message | undefined): string | undefined {
  return message && t(message.key, message.params ?? {})
}
</script>

<template>
  <GgDialog
    v-model:open="open"
    :title="
      color === null
        ? t('clubConfig.createColorTitle')
        : t('clubConfig.editColorTitle', { label: color.label })
    "
  >
    <form class="flex flex-col gap-5" novalidate @submit.prevent="save">
      <TextField
        v-model="form.label"
        :label="t('clubConfig.colorLabel')"
        :hint="t('clubConfig.colorLabelHint')"
        :maxlength="COLOR_LABEL_MAX"
        :error="text(errors.label)"
      />
      <div class="flex items-start gap-3">
        <div class="min-w-0 flex-1">
          <TextField
            v-model="form.hex"
            :label="t('clubConfig.colorHex')"
            :hint="t('clubConfig.colorHexHint')"
            :maxlength="7"
            :error="text(errors.hex)"
            verbatim
          />
        </div>
        <div class="flex flex-col gap-1.5">
          <label :for="pickerId" class="font-semibold">{{ t('clubConfig.colorPicker') }}</label>
          <input
            :id="pickerId"
            type="color"
            :value="pickerValue"
            class="h-12 w-16 cursor-pointer rounded-control border-2 border-rule bg-surface p-1"
            @input="picked"
          />
        </div>
      </div>
      <CheckboxField v-model="form.multicolor">{{ t('clubConfig.colorMulticolor') }}</CheckboxField>
      <CheckboxField v-model="form.active" :hint="t('clubConfig.colorActiveHint')">
        {{ t('clubConfig.colorActive') }}
      </CheckboxField>

      <div class="flex flex-col gap-1.5">
        <p class="font-semibold">{{ t('clubConfig.colorPreview') }}</p>
        <div class="grid grid-cols-2 gap-3">
          <!-- Fixed backgrounds: the label paper of the light theme, the night of the dark one. -->
          <div
            class="flex items-center gap-3 rounded-control border border-rule bg-[#fffdf4] px-3 py-3 text-[#17140f]"
          >
            <HoldSwatch :color="preview" :size="40" />
            <span class="text-sm font-semibold">{{ t('clubConfig.previewLight') }}</span>
          </div>
          <div
            class="flex items-center gap-3 rounded-control border border-rule bg-[#12110e] px-3 py-3 text-[#f3efe6]"
          >
            <HoldSwatch :color="preview" :size="40" />
            <span class="text-sm font-semibold">{{ t('clubConfig.previewDark') }}</span>
          </div>
        </div>
      </div>

      <FormAlert v-if="general">{{ text(general) }}</FormAlert>
      <div class="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
        <GgButton variant="secondary" @click="open = false">{{ t('common.cancel') }}</GgButton>
        <GgButton type="submit" :disabled="create.isPending.value || update.isPending.value">
          {{ t('common.save') }}
        </GgButton>
      </div>
    </form>
  </GgDialog>
</template>
