<script setup lang="ts">
import { Eye, EyeOff, ImageOff, MessageSquareX } from 'lucide-vue-next'
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { errorMessageKey } from '@/api/errors'
import { useChangeSessionPrivacy, useRemoveSessionPhoto, type Session } from '@/api/sessions'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'

/**
 * One of my sessions in a frozen month (D-112): its routes stay as they are, but who sees it, its
 * comment and its photo count in no figure, so I can still take back what I shared.
 */
const props = defineProps<{ session: Session }>()

type Erase = 'comment' | 'photo'

const { t } = useI18n()
const privacy = useChangeSessionPrivacy()
const photo = useRemoveSessionPhoto()
const confirming = ref(false)
/** Kept once the dialog closes: it closes before it says « confirm ». */
const erasing = ref<Erase>('comment')
const error = ref<string | null>(null)

const failed = (cause: unknown) => {
  error.value = t(errorMessageKey(cause))
}

function setVisibility(visibility: Session['visibility']) {
  error.value = null
  privacy.mutate({ id: props.session.id, visibility, removeComment: false }, { onError: failed })
}

function ask(what: Erase) {
  erasing.value = what
  confirming.value = true
}

function erase() {
  error.value = null
  if (erasing.value === 'comment') {
    privacy.mutate(
      { id: props.session.id, visibility: props.session.visibility, removeComment: true },
      { onError: failed },
    )
  } else {
    photo.mutate(props.session.id, { onError: failed })
  }
}
</script>

<template>
  <section class="px-4 py-4" :aria-label="t('session.frozenPrivacy')">
    <p class="text-ink-muted">{{ t('session.frozen') }}</p>
    <div class="mt-3 flex flex-wrap gap-2">
      <GgButton
        v-if="session.visibility === 'FRIENDS'"
        variant="secondary"
        :disabled="privacy.isPending.value"
        @click="setVisibility('PRIVATE')"
      >
        <EyeOff :size="18" aria-hidden="true" />{{ t('session.frozenHide') }}
      </GgButton>
      <GgButton
        v-else
        variant="secondary"
        :disabled="privacy.isPending.value"
        @click="setVisibility('FRIENDS')"
      >
        <Eye :size="18" aria-hidden="true" />{{ t('session.frozenShare') }}
      </GgButton>
      <GgButton
        v-if="session.comment"
        variant="ghost"
        :disabled="privacy.isPending.value"
        @click="ask('comment')"
      >
        <MessageSquareX :size="18" aria-hidden="true" />{{ t('session.frozenEraseComment') }}
      </GgButton>
      <GgButton
        v-if="session.photoUrl"
        variant="ghost"
        :disabled="photo.isPending.value"
        @click="ask('photo')"
      >
        <ImageOff :size="18" aria-hidden="true" />{{ t('session.photoRemove') }}
      </GgButton>
    </div>
    <FormAlert v-if="error" class="mt-3">{{ error }}</FormAlert>
  </section>
  <ConfirmDialog
    v-model:open="confirming"
    :title="t(erasing === 'photo' ? 'session.frozenPhotoTitle' : 'session.frozenCommentTitle')"
    :body="t('session.frozenEraseBody')"
    :confirm-label="t('session.frozenEraseConfirm')"
    danger
    @confirm="erase"
  />
</template>
