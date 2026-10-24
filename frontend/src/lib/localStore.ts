/**
 * A small key-value store in IndexedDB (base « gg-pocket »), for what the phone keeps while
 * offline: the session draft and the queue of sessions waiting for the network.
 *
 * It is a comfort, never a guarantee (DECISIONS D-041: Safari may wipe it after 7 days without a
 * visit). When IndexedDB is missing or refuses (private browsing), the store falls back to memory:
 * the app keeps working, it just forgets on reload.
 */
export interface KeyValueStore<T> {
  get(key: string): Promise<T | undefined>
  set(key: string, value: T): Promise<void>
  delete(key: string): Promise<void>
  values(): Promise<T[]>
}

export type StoreName = 'drafts' | 'outbox'

const DATABASE = 'gg-pocket'
const VERSION = 1
const STORES: StoreName[] = ['drafts', 'outbox']

let database: Promise<IDBDatabase> | undefined

function openDatabase(): Promise<IDBDatabase> {
  database ??= new Promise<IDBDatabase>((resolve, reject) => {
    const opening = indexedDB.open(DATABASE, VERSION)
    opening.onupgradeneeded = () => {
      for (const name of STORES) {
        if (!opening.result.objectStoreNames.contains(name)) opening.result.createObjectStore(name)
      }
    }
    opening.onsuccess = () => {
      resolve(opening.result)
    }
    opening.onerror = () => {
      reject(opening.error ?? new Error('IndexedDB refused'))
    }
  })
  return database
}

function done<R>(
  requestOf: (store: IDBObjectStore) => IDBRequest<R>,
  name: StoreName,
  mode: IDBTransactionMode,
) {
  return openDatabase().then(
    (db) =>
      new Promise<R>((resolve, reject) => {
        const transaction = db.transaction(name, mode)
        const pending = requestOf(transaction.objectStore(name))
        transaction.oncomplete = () => {
          resolve(pending.result)
        }
        transaction.onerror = () => {
          reject(transaction.error ?? new Error('IndexedDB failed'))
        }
        transaction.onabort = () => {
          reject(transaction.error ?? new Error('IndexedDB aborted'))
        }
      }),
  )
}

export function memoryStore<T>(): KeyValueStore<T> {
  const entries = new Map<string, T>()
  return {
    get: (key) => Promise.resolve(entries.get(key)),
    set: (key, value) => {
      entries.set(key, value)
      return Promise.resolve()
    },
    delete: (key) => {
      entries.delete(key)
      return Promise.resolve()
    },
    values: () => Promise.resolve([...entries.values()]),
  }
}

/** IndexedDB when it works, memory otherwise (the first failure switches for good). */
export function localStore<T>(name: StoreName): KeyValueStore<T> {
  const fallback = memoryStore<T>()
  let usable = typeof indexedDB !== 'undefined'
  async function attempt<R>(real: () => Promise<R>, memory: () => Promise<R>): Promise<R> {
    if (!usable) return memory()
    try {
      return await real()
    } catch {
      usable = false
      return memory()
    }
  }
  return {
    get: (key) =>
      attempt(
        () => done((store) => store.get(key) as IDBRequest<T | undefined>, name, 'readonly'),
        () => fallback.get(key),
      ),
    set: (key, value) =>
      attempt(
        () => done((store) => store.put(value, key), name, 'readwrite').then(() => undefined),
        () => fallback.set(key, value),
      ),
    delete: (key) =>
      attempt(
        () => done((store) => store.delete(key), name, 'readwrite').then(() => undefined),
        () => fallback.delete(key),
      ),
    values: () =>
      attempt(
        () => done((store) => store.getAll() as IDBRequest<T[]>, name, 'readonly'),
        () => fallback.values(),
      ),
  }
}
