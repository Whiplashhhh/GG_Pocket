<script setup lang="ts">
import { Camera } from 'lucide-vue-next'
import { computed, onBeforeUnmount, ref, useId, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import { PhotoError, prepareRoutePhoto } from '@/lib/photo'

/**
 * The optional photo of a session: shrunk in the browser before anything leaves the phone (spec
 * §8.3), then cleaned again by the server. Seen only by whoever may read the session.
 */
const props = defineProps<{ savedUrl?: string }>()
/** The new photo, already shrunk. */
const photo = defineModel<Blob | undefined>('photo', { required: true })
/** Remove the saved photo when the session is saved. */
const remove = defineModel<boolean>('remove', { required: true })
const preparing = defineModel<boolean>('preparing', { default: false })

const { t } = useI18n()
const inputId = useId()
const error = ref<string | null>(null)

const newUrl = ref<string | undefined>(undefined)
watch(
  photo,
  (blob) => {
    if (newUrl.value !== undefined) URL.revokeObjectURL(newUrl.value)
    newUrl.value = blob === undefined ? undefined : URL.createObjectURL(blob)
  },
  { immediate: true },
)
onBeforeUnmount(() => {
  if (newUrl.value !== undefined) URL.revokeObjectURL(newUrl.value)
})

const shownUrl = computed(() => newUrl.value ?? (remove.value ? undefined : props.savedUrl))

async function chosen(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (file === undefined) return
  error.value = null
  preparing.value = true
  try {
    photo.value = await prepareRoutePhoto(file)
    remove.value = false
  } catch (failure) {
    error.value = failure instanceof PhotoError ? t(`photo.${failure.reason}`) : t('errors.UNKNOWN')
  } finally {
    preparing.value = false
  }
}

function drop() {
  photo.value = undefined
  remove.value = props.savedUrl !== undefined
}
</script>

<template>
  <section class="flex flex-col gap-3" :aria-labelledby="`${inputId}-title`">
    <h3 :id="`${inputId}-title`" class="font-semibold">
      {{ t('session.photo') }}
      <span class="font-normal text-ink-muted">({{ t('common.optional') }})</span>
    </h3>
    <p class="text-sm text-ink-muted">{{ t('session.photoHint') }}</p>
    <img
      v-if="shownUrl"
      :src="shownUrl"
      :alt="t('session.photoAlt')"
      class="max-h-72 w-full rounded-control bg-surface-sunken object-contain"
    />
    <p v-if="remove && photo === undefined" role="status" class="text-ink-muted">
      {{ t('session.photoWillGo') }}
    </p>
    <p v-if="preparing" role="status" class="text-ink-muted">{{ t('session.photoPreparing') }}</p>
    <FormAlert v-if="error">{{ error }}</FormAlert>
    <div class="flex flex-wrap gap-2">
      <!-- A label styled as a button: the native file picker opens, camera included on phones. -->
      <label
        :for="inputId"
        class="inline-flex min-h-12 cursor-pointer items-center gap-2 rounded-control border-2 border-ink px-4 font-semibold text-ink hover:bg-surface-sunken has-focus-visible:outline-3 has-focus-visible:outline-offset-2 has-focus-visible:outline-focus"
        :class="{ 'pointer-events-none opacity-60': preparing }"
      >
        <Camera :size="20" aria-hidden="true" />
        {{ shownUrl ? t('session.photoChange') : t('session.photoChoose') }}
        <input
          :id="inputId"
          type="file"
          accept="image/*"
          class="sr-only"
          :disabled="preparing"
          @change="chosen"
        />
      </label>
      <GgButton v-if="shownUrl" variant="ghost" @click="drop">{{
        t('session.photoRemove')
      }}</GgButton>
      <GgButton v-if="remove && photo === undefined" variant="ghost" @click="remove = false">
        {{ t('session.photoRestore') }}
      </GgButton>
    </div>
  </section>
</template>
