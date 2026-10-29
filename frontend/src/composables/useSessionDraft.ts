import { watchDebounced } from '@vueuse/core'
import { ref, toRaw, type Ref } from 'vue'

import { localStore } from '@/lib/localStore'
import { restoredForm, type SessionForm } from '@/lib/sessionForm'

/** What the phone keeps of a session being typed (spec §8.3). */
export interface SessionDraft {
  form: SessionForm
  /** The new photo, already shrunk; none when unchanged. */
  photo?: Blob
  /** Removing the photo of a saved session. */
  removePhoto?: boolean
  savedAt: string
}

const store = localStore<SessionDraft>('drafts')
const SAVE_DELAY_MS = 300

/** The draft key: "new" for a new session, the id of a saved one being changed. */
export function draftKey(sessionId?: string): string {
  return sessionId === undefined ? 'new' : `session:${sessionId}`
}

/**
 * Saves the sheet at every change (debounced) and gives back the last draft on opening. A form
 * is plain data: it goes through JSON so no Vue proxy reaches IndexedDB; the photo stays a Blob.
 */
export function useSessionDraft(
  key: string,
  form: Ref<SessionForm>,
  photo: Ref<Blob | undefined>,
  removePhoto: Ref<boolean>,
) {
  const restored = ref(false)
  let paused = true

  async function restore(): Promise<boolean> {
    const draft = await store.get(key)
    if (draft !== undefined) {
      form.value = restoredForm(draft.form)
      photo.value = draft.photo
      removePhoto.value = draft.removePhoto === true
      restored.value = true
    }
    paused = false
    return draft !== undefined
  }

  async function save() {
    if (paused) return
    const draft: SessionDraft = {
      form: JSON.parse(JSON.stringify(toRaw(form.value))) as SessionForm,
      savedAt: new Date().toISOString(),
    }
    if (photo.value !== undefined) draft.photo = toRaw(photo.value)
    if (removePhoto.value) draft.removePhoto = true
    await store.set(key, draft)
  }

  /**
   * Once sent: nothing to restore next time. `keepSaving` when the climber starts over in
   * the same sheet, whose next changes are a new draft.
   */
  async function clear(keepSaving = false) {
    paused = true
    restored.value = false
    await store.delete(key)
    paused = !keepSaving
  }

  watchDebounced([form, photo, removePhoto], () => void save(), {
    debounce: SAVE_DELAY_MS,
    deep: true,
  })

  return { restored, restore, save, clear }
}
