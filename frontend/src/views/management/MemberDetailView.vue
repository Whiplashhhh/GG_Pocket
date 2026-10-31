<script setup lang="ts">
import { useClipboard, useShare } from '@vueuse/core'
import { computed, ref, useId, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

import {
  useChangeRole,
  useChangeStatus,
  useCreateResetLink,
  useDeleteMember,
  useMember,
  useValidateMember,
  type MemberDetail,
} from '@/api/admin'
import { useCurrentMember } from '@/api/auth'
import { errorMessageKey } from '@/api/errors'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import RoleBadge from '@/components/ui/RoleBadge.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { formatDate, formatDateTime, formatRelative } from '@/lib/format'
import type { Role } from '@/lib/route'
import { ROLES } from '@/lib/roles'

import ManagementTitle from './ManagementTitle.vue'

/**
 * Spec §8.11: the member sheet. Only the actions the server allows are offered (it checks them
 * again anyway); the birth date is never shown (DECISIONS D-045). A new account awaiting
 * validation shows what it declared at sign-up, to validate it or refuse it (D-107).
 */
const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const me = useCurrentMember()
const id = computed(() => String(route.params.id))
const member = useMember(() => id.value)
const changeRole = useChangeRole()
const changeStatus = useChangeStatus()
const createLink = useCreateResetLink()
const deleteMember = useDeleteMember()
const validateMember = useValidateMember()
const { copy, copied, isSupported: canCopy } = useClipboard({ copiedDuring: 2500 })
const { share, isSupported: canShare } = useShare()

const selectedRole = ref<Role>('CLIMBER')
const confirmingDisable = ref(false)
const confirmingDelete = ref(false)
const confirmingRefusal = ref(false)
const notice = ref<string | null>(null)
const failure = ref<unknown>(null)
const resetLink = ref<{ url: string; expiresAt: string } | null>(null)
const roleSelectId = useId()
const resetInputId = useId()

watch(
  () => member.data.value,
  (detail) => {
    if (detail) {
      selectedRole.value = detail.member.role
    }
  },
  { immediate: true },
)

const detail = computed(() => member.data.value)
const name = computed(() =>
  detail.value ? `${detail.value.member.firstName} ${detail.value.member.lastName}` : '',
)
const isSelf = computed(() => detail.value?.member.id === me.value?.id)
const roleChoices = computed(() => {
  const value = detail.value
  if (value === undefined) return []
  const allowed = new Set<Role>([value.member.role, ...value.permissions.assignableRoles])
  return ROLES.filter((role) => allowed.has(role))
})
const canChangeRole = computed(() => (detail.value?.permissions.assignableRoles.length ?? 0) > 0)
const outOfReach = computed(
  () =>
    !isSelf.value &&
    detail.value !== undefined &&
    !canChangeRole.value &&
    !detail.value.permissions.canDisable &&
    !detail.value.permissions.canEnable,
)

function done(message: string) {
  failure.value = null
  notice.value = message
}

function failed(error: unknown) {
  notice.value = null
  failure.value = error
}

function saveRole(value: MemberDetail) {
  changeRole.mutate(
    { id: value.member.id, role: selectedRole.value },
    {
      onSuccess: () => {
        done(t('member.roleChanged'))
      },
      onError: failed,
    },
  )
}

function setStatus(status: 'ACTIVE' | 'DISABLED') {
  changeStatus.mutate(
    { id: id.value, status },
    {
      onSuccess: () => {
        done(status === 'DISABLED' ? t('member.disabled') : t('member.enabled'))
      },
      onError: failed,
    },
  )
}

function validate(role: 'TOURIST' | 'CLIMBER') {
  validateMember.mutate(
    { id: id.value, role },
    {
      onSuccess: () => {
        done(t('member.validated', { name: name.value, role: t(`roles.${role}`) }))
      },
      onError: failed,
    },
  )
}

function remove() {
  deleteMember.mutate(id.value, {
    onSuccess: () => void router.replace('/gestion/membres'),
    onError: failed,
  })
}

function makeResetLink() {
  createLink.mutate(id.value, {
    onSuccess: (link) => {
      failure.value = null
      // The token goes in the fragment: never sent to a server, nor in a Referer.
      resetLink.value = {
        url: `${window.location.origin}/nouveau-mot-de-passe#${link.token}`,
        expiresAt: link.expiresAt,
      }
    },
    onError: failed,
  })
}
</script>

<template>
  <ManagementTitle back="/gestion/membres" :back-label="t('member.back')">
    {{ detail ? name : t('members.title') }}
  </ManagementTitle>

  <div
    v-if="member.isPending.value"
    class="flex flex-col gap-3 px-4 py-6 lg:px-0"
    aria-hidden="true"
  >
    <SkeletonBlock class="size-24 rounded-full" />
    <SkeletonBlock class="h-6 w-1/2" />
  </div>
  <div v-else-if="member.isError.value" class="px-4 py-6 lg:px-0">
    <FormAlert>{{ t(errorMessageKey(member.error.value)) }}</FormAlert>
  </div>

  <div v-else-if="detail" class="flex flex-col lg:max-w-2xl">
    <section class="flex items-center gap-4 px-4 py-5 lg:px-0">
      <UserAvatar
        :first-name="detail.member.firstName"
        :last-name="detail.member.lastName"
        :seed="detail.member.nickname"
        :src="detail.member.avatarUrl"
        :size="80"
      />
      <div class="flex min-w-0 flex-col items-start gap-1">
        <p class="text-ink-muted">@{{ detail.member.nickname }}</p>
        <RoleBadge :role="detail.member.role" />
        <p v-if="detail.member.status === 'DISABLED'" class="text-sm font-semibold text-danger">
          {{ t('members.status.DISABLED') }}
        </p>
      </div>
    </section>

    <div class="flex flex-col gap-3 px-4 lg:px-0" aria-live="polite">
      <FormAlert v-if="notice" tone="success">{{ notice }}</FormAlert>
      <FormAlert v-if="failure">{{ t(errorMessageKey(failure)) }}</FormAlert>
      <FormAlert v-if="isSelf" tone="success">{{ t('member.self') }}</FormAlert>
      <p v-else-if="outOfReach" class="text-ink-muted">{{ t('member.outOfReach') }}</p>
    </div>

    <section
      v-if="detail.permissions.canValidate"
      class="mx-4 mt-4 flex flex-col gap-3 border-l-4 border-brand bg-surface px-4 py-4 lg:mx-0"
      aria-labelledby="validation-title"
    >
      <h2 id="validation-title" class="text-lg font-title">
        {{ t('member.validationTitle') }}
      </h2>
      <p class="text-ink-muted">{{ t('member.validationIntro') }}</p>
      <p v-if="detail.member.declaredLicensed === true" class="font-semibold">
        {{ t('member.declaredLicensed') }}
      </p>
      <template v-else-if="detail.member.declaredLicensed === false">
        <p class="font-semibold">{{ t('member.declaredVisitor') }}</p>
        <blockquote
          v-if="detail.joinNote"
          class="border-l-2 border-rule pl-3 whitespace-pre-line italic"
        >
          {{ detail.joinNote }}
        </blockquote>
      </template>
      <p v-else class="text-ink-muted">{{ t('member.declaredUnknown') }}</p>
      <p v-if="detail.joinNote" class="text-sm text-ink-muted">{{ t('member.noteErased') }}</p>
      <div class="flex flex-wrap gap-3">
        <GgButton
          :variant="detail.member.declaredLicensed === false ? 'secondary' : 'primary'"
          :disabled="validateMember.isPending.value"
          @click="validate('CLIMBER')"
        >
          {{ t('member.validateClimber') }}
        </GgButton>
        <GgButton
          :variant="detail.member.declaredLicensed === false ? 'primary' : 'secondary'"
          :disabled="validateMember.isPending.value"
          @click="validate('TOURIST')"
        >
          {{ t('member.validateTourist') }}
        </GgButton>
      </div>
      <GgButton
        v-if="detail.permissions.canDelete"
        variant="danger"
        class="self-start"
        :disabled="deleteMember.isPending.value"
        @click="confirmingRefusal = true"
      >
        {{ t('member.refuse') }}
      </GgButton>
    </section>

    <dl
      class="mt-4 grid grid-cols-1 gap-x-6 gap-y-3 border-t border-rule px-4 py-5 sm:grid-cols-2 lg:px-0"
    >
      <div>
        <dt class="text-sm text-ink-muted">{{ t('member.email') }}</dt>
        <dd class="break-all">{{ detail.email ?? t('member.noEmail') }}</dd>
      </div>
      <div>
        <dt class="text-sm text-ink-muted">{{ t('members.columns.registeredAt') }}</dt>
        <dd>{{ formatDate(detail.member.registeredAt) }}</dd>
      </div>
      <div>
        <dt class="text-sm text-ink-muted">{{ t('members.columns.lastSeenAt') }}</dt>
        <dd>
          {{
            detail.member.lastSeenAt
              ? formatRelative(detail.member.lastSeenAt)
              : t('member.neverSeen')
          }}
        </dd>
      </div>
      <div>
        <dt class="text-sm text-ink-muted">{{ t('members.columns.sessionCount') }}</dt>
        <dd class="tabular-nums">{{ detail.member.sessionCount }}</dd>
      </div>
      <div v-if="detail.clubArrivalYear || detail.climbingSinceYear">
        <dt class="text-sm text-ink-muted">{{ t('profileFields.clubArrivalYear') }}</dt>
        <dd>
          {{ detail.clubArrivalYear ?? '—' }}
          <span v-if="detail.climbingSinceYear" class="text-ink-muted">
            · {{ t('member.climbingSince', { year: detail.climbingSinceYear }) }}</span
          >
        </dd>
      </div>
      <div v-if="detail.underFifteenWithParentalConsent" class="sm:col-span-2">
        <dd class="font-semibold">{{ t('member.minor') }}</dd>
      </div>
    </dl>

    <section
      v-if="canChangeRole"
      class="flex flex-col gap-3 border-t border-rule px-4 py-5 lg:px-0"
      aria-labelledby="role-title"
    >
      <h2 id="role-title" class="text-lg font-title">{{ t('member.role') }}</h2>
      <label :for="roleSelectId" class="flex flex-col gap-1.5">
        <span class="font-semibold">{{ t('member.roleLabel') }}</span>
        <select
          :id="roleSelectId"
          v-model="selectedRole"
          class="min-h-12 rounded-control border-2 border-rule bg-surface px-3 text-base sm:max-w-xs"
        >
          <option v-for="role in roleChoices" :key="role" :value="role">
            {{ t(`roleNames.${role}`) }}
          </option>
        </select>
        <span class="text-sm text-ink-muted">{{
          t('member.roleCurrent', { role: t(`roleNames.${detail.member.role}`) })
        }}</span>
      </label>
      <GgButton
        class="self-start"
        :disabled="selectedRole === detail.member.role || changeRole.isPending.value"
        @click="saveRole(detail)"
      >
        {{ t('member.changeRole') }}
      </GgButton>
    </section>

    <section
      v-if="detail.permissions.canCreateResetLink"
      class="flex flex-col gap-3 border-t border-rule px-4 py-5 lg:px-0"
      aria-labelledby="reset-title"
    >
      <h2 id="reset-title" class="text-lg font-title">{{ t('member.resetTitle') }}</h2>
      <p class="max-w-prose text-ink-muted">{{ t('member.resetIntro') }}</p>
      <GgButton
        variant="secondary"
        class="self-start"
        :disabled="createLink.isPending.value"
        @click="makeResetLink"
      >
        {{ t('member.resetCreate') }}
      </GgButton>
      <div v-if="resetLink" class="flex flex-col gap-2 rounded-control bg-surface-sunken p-3">
        <p>
          {{ t('member.resetReady', { date: formatDateTime(resetLink.expiresAt), name }) }}
        </p>
        <label :for="resetInputId" class="flex flex-col gap-1">
          <span class="sr-only">{{ t('member.resetLinkLabel') }}</span>
          <input
            :id="resetInputId"
            :value="resetLink.url"
            readonly
            class="min-h-11 w-full rounded-control border-2 border-rule bg-surface px-3 font-mono text-sm"
            @focus="($event.target as HTMLInputElement).select()"
          />
        </label>
        <div class="flex flex-wrap gap-2">
          <GgButton v-if="canCopy" @click="copy(resetLink.url)">
            {{ copied ? t('common.copied') : t('common.copy') }}
          </GgButton>
          <GgButton
            v-if="canShare"
            variant="secondary"
            @click="share({ title: t('member.shareTitle'), url: resetLink.url })"
          >
            {{ t('common.share') }}
          </GgButton>
        </div>
        <p class="text-sm font-semibold">{{ t('member.resetWarning') }}</p>
      </div>
    </section>

    <section
      v-if="
        detail.permissions.canDisable ||
        detail.permissions.canEnable ||
        (detail.permissions.canDelete && !detail.permissions.canValidate)
      "
      class="flex flex-col gap-3 border-t border-rule px-4 py-5 lg:px-0"
      aria-labelledby="account-title"
    >
      <h2 id="account-title" class="text-lg font-title">{{ t('member.account') }}</h2>
      <GgButton
        v-if="detail.permissions.canDisable"
        variant="danger"
        class="self-start"
        :disabled="changeStatus.isPending.value"
        @click="confirmingDisable = true"
      >
        {{ t('member.disable') }}
      </GgButton>
      <GgButton
        v-else
        class="self-start"
        :disabled="changeStatus.isPending.value"
        @click="setStatus('ACTIVE')"
      >
        {{ t('member.enable') }}
      </GgButton>
      <GgButton
        v-if="detail.permissions.canDelete && !detail.permissions.canValidate"
        variant="danger"
        class="self-start"
        :disabled="deleteMember.isPending.value"
        @click="confirmingDelete = true"
      >
        {{ t('member.delete') }}
      </GgButton>
    </section>

    <ConfirmDialog
      v-model:open="confirmingDisable"
      :title="t('member.disableConfirmTitle', { name })"
      :body="t('member.disableConfirmBody')"
      :confirm-label="t('member.disable')"
      danger
      @confirm="setStatus('DISABLED')"
    />
    <ConfirmDialog
      v-model:open="confirmingDelete"
      :title="t('member.deleteConfirmTitle', { name })"
      :body="t('member.deleteConfirmBody')"
      :confirm-label="t('member.deleteConfirm')"
      danger
      @confirm="remove"
    />
    <ConfirmDialog
      v-model:open="confirmingRefusal"
      :title="t('member.refuseConfirmTitle', { name })"
      :body="t('member.refuseConfirmBody')"
      :confirm-label="t('member.refuseConfirm')"
      danger
      @confirm="remove"
    />
  </div>
</template>
