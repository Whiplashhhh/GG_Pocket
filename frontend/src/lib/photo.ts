/**
 * Profile photos are prepared in the browser before upload: cropped to a square, shrunk, and
 * redrawn the right way up (browsers apply the EXIF orientation when drawing). The upload is then
 * small even from a 48-megapixel phone, and the server only has to clean it (DECISIONS D-054).
 */
export const MAX_PHOTO_BYTES = 10 * 1024 * 1024
export const AVATAR_UPLOAD_SIDE = 1024
const JPEG_QUALITY = 0.9

export interface SquareCrop {
  sx: number
  sy: number
  side: number
  /** Side of the drawn square: never larger than the source. */
  target: number
}

/** The centred square of a picture, scaled down to `maxSide` at most. */
export function squareCrop(width: number, height: number, maxSide: number): SquareCrop {
  const side = Math.min(width, height)
  return {
    sx: Math.floor((width - side) / 2),
    sy: Math.floor((height - side) / 2),
    side,
    target: Math.min(side, maxSide),
  }
}

export class PhotoError extends Error {
  constructor(readonly reason: 'unreadable' | 'tooBig') {
    super(`Photo refused: ${reason}`)
    this.name = 'PhotoError'
  }
}

export async function prepareAvatar(file: File): Promise<Blob> {
  if (file.size > MAX_PHOTO_BYTES) {
    throw new PhotoError('tooBig')
  }
  let bitmap: ImageBitmap
  try {
    bitmap = await createImageBitmap(file)
  } catch {
    throw new PhotoError('unreadable')
  }
  const crop = squareCrop(bitmap.width, bitmap.height, AVATAR_UPLOAD_SIDE)
  const canvas = document.createElement('canvas')
  canvas.width = crop.target
  canvas.height = crop.target
  const context = canvas.getContext('2d')
  if (context === null) {
    bitmap.close()
    throw new PhotoError('unreadable')
  }
  context.imageSmoothingQuality = 'high'
  context.drawImage(bitmap, crop.sx, crop.sy, crop.side, crop.side, 0, 0, crop.target, crop.target)
  bitmap.close()
  return new Promise((resolve, reject) => {
    canvas.toBlob(
      (blob) => {
        if (blob === null) {
          reject(new PhotoError('unreadable'))
        } else {
          resolve(blob)
        }
      },
      'image/jpeg',
      JPEG_QUALITY,
    )
  })
}
