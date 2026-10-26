<script setup lang="ts">
import { Download, Trash2 } from 'lucide-vue-next'
import { nextTick, ref, useTemplateRef } from 'vue'
import { useI18n } from 'vue-i18n'

import { useCurrentMember, useDeleteMyAccount } from '@/api/auth'
import { errorMessageKey } from '@/api/errors'
import { useExportMyData } from '@/api/me'
import SubPageHeader from '@/components/layout/SubPageHeader.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import PasswordField from '@/components/ui/PasswordField.vue'
import { myDataFileName, saveFile } from '@/lib/download'
import { focusFirstInvalid, serverFieldErrors, type FieldErrors, type Message } from '@/lib/forms'

/** Spec §14: export all my data, delete my account for good (DECISIONS D-096). */
const { t } = useI18n()
const member = useCurrentMember()
const exportData = useExportMyData()
const deleteAccount = useDeleteMyAccount()

const exported = ref(false)
const deleting = ref(false)
const password = ref('')
const errors = ref<FieldErrors<'password'>>({})
const general = ref<Message | null>(null)
const passwordField = useTemplateRef<InstanceType<typeof PasswordField>>('passwordField')

function download() {
  exported.value = false
  exportData.mutate(undefined, {
    onSuccess: (file) => {
      saveFile(file, myDataFileName(new Date()))
      exported.value = true
    },
  })
}

function startDeleting() {
  deleting.value = true
  void nextTick(() => passwordField.value?.focus())
}

function cancelDeleting() {
  deleting.value = false
  password.value = ''
  errors.value = {}
  general.value = null
}

function confirmDeletion() {
  general.value = null
  errors.value = password.value === '' ? { password: { key: 'validation.required' } } : {}
  if (errors.value.password) {
    focusFirstInvalid()
    return
  }
  deleteAccount.mutate(password.value, {
    onError: (error) => {
      const placed = serverFieldErrors(
        error,
        ['password'] as const,
        {},
        {
          CURRENT_PASSWORD_WRONG: 'password',
        },
      )
      errors.value = placed.fields
      general.value = placed.general
      focusFirstInvalid()
    },
  })
}
</script>

<template>
  <SubPageHeader back="/profil" :back-label="t('common.back')">
    {{ t('account.title') }}
  </SubPageHeader>

  <section class="flex flex-col gap-3 px-4 py-6" aria-labelledby="export-title">
    <h2 id="export-title" class="text-lg font-title">{{ t('account.exportTitle') }}</h2>
    <p class="max-w-prose">{{ t('account.exportBody') }}</p>
    <FormAlert v-if="exported" tone="success">{{ t('account.exportDone') }}</FormAlert>
    <FormAlert v-if="exportData.isError.value">
      {{ t(errorMessageKey(exportData.error.value)) }}
    </FormAlert>
    <GgButton
      variant="secondary"
      class="self-start"
      :disabled="exportData.isPending.value"
      @click="download"
    >
      <Download :size="20" aria-hidden="true" />
      {{ exportData.isPending.value ? t('account.exporting') : t('account.export') }}
    </GgButton>
  </section>

  <section
    class="flex flex-col gap-3 border-t border-rule px-4 py-6"
    aria-labelledby="delete-title"
  >
    <h2 id="delete-title" class="text-lg font-title">{{ t('account.deleteTitle') }}</h2>
    <p class="max-w-prose">{{ t('account.deleteGone') }}</p>
    <p class="max-w-prose">{{ t('account.deleteKept') }}</p>
    <p class="max-w-prose font-semibold">{{ t('account.deleteFinal') }}</p>
    <GgButton v-if="!deleting" variant="danger" class="self-start" @click="startDeleting">
      <Trash2 :size="20" aria-hidden="true" />
      {{ t('account.delete') }}
    </GgButton>
    <form
      v-else
      class="flex flex-col gap-4 rounded-control border-2 border-danger p-4"
      novalidate
      @submit.prevent="confirmDeletion"
    >
      <FormAlert v-if="general">{{ t(general.key) }}</FormAlert>
      <!-- Lets password managers fill the right login. -->
      <input
        v-if="member"
        type="text"
        name="username"
        autocomplete="username"
        :aria-label="t('login.nickname')"
        :value="member.nickname"
        hidden
        readonly
      />
      <PasswordField
        ref="passwordField"
        v-model="password"
        :label="t('account.password')"
        :hint="t('account.passwordHint')"
        autocomplete="current-password"
        :error="errors.password && t(errors.password.key)"
      />
      <div class="flex flex-wrap gap-3">
        <GgButton type="submit" variant="danger" :disabled="deleteAccount.isPending.value">
          {{ t('account.deleteConfirm') }}
        </GgButton>
        <GgButton variant="ghost" :disabled="deleteAccount.isPending.value" @click="cancelDeleting">
          {{ t('common.cancel') }}
        </GgButton>
      </div>
    </form>
  </section>
</template>
