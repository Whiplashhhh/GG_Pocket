<script setup lang="ts">
import { Camera } from 'lucide-vue-next'
import { computed, ref, useId } from 'vue'
import { useI18n } from 'vue-i18n'

import { useCurrentMember } from '@/api/auth'
import { errorMessageKey } from '@/api/errors'
import { useRemoveAvatar, useUploadAvatar } from '@/api/me'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { PhotoError, prepareAvatar } from '@/lib/photo'

/** Choose, change or remove the profile photo. Resized in the browser, cleaned by the server. */
const { t } = useI18n()
const member = useCurrentMember()
const upload = useUploadAvatar()
const remove = useRemoveAvatar()
const inputId = useId()
const error = ref<string | null>(null)
const busy = computed(() => upload.isPending.value || remove.isPending.value)

async function chosen(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (file === undefined) {
    return
  }
  error.value = null
  try {
    const photo = await prepareAvatar(file)
    await upload.mutateAsync(photo)
  } catch (failure) {
    error.value =
      failure instanceof PhotoError ? t(`photo.${failure.reason}`) : t(errorMessageKey(failure))
  }
}

function removePhoto() {
  error.value = null
  remove.mutate(undefined, { onError: (failure) => (error.value = t(errorMessageKey(failure))) })
}
</script>

<template>
  <div v-if="member" class="flex flex-col items-center gap-4">
    <UserAvatar
      :first-name="member.firstName"
      :last-name="member.lastName"
      :seed="member.nickname"
      :src="member.avatarUrl"
      :size="128"
    />
    <p v-if="upload.isPending.value" role="status" class="text-ink-muted">
      {{ t('photo.uploading') }}
    </p>
    <FormAlert v-if="error">{{ error }}</FormAlert>
    <div class="flex flex-wrap justify-center gap-2">
      <!-- A label styled as a button: the native file picker opens, camera included on phones. -->
      <label
        :for="inputId"
        class="inline-flex min-h-11 cursor-pointer items-center gap-2 rounded-control bg-brand px-4 font-semibold text-on-brand hover:brightness-95 has-focus-visible:outline-3 has-focus-visible:outline-offset-2 has-focus-visible:outline-focus"
        :class="{ 'pointer-events-none opacity-60': busy }"
      >
        <Camera :size="20" aria-hidden="true" />
        {{ member.avatarUrl ? t('photo.change') : t('photo.choose') }}
        <input
          :id="inputId"
          type="file"
          accept="image/*"
          class="sr-only"
          :disabled="busy"
          @change="chosen"
        />
      </label>
      <GgButton v-if="member.avatarUrl" variant="ghost" :disabled="busy" @click="removePhoto">
        {{ t('photo.remove') }}
      </GgButton>
    </div>
  </div>
</template>
