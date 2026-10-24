/**
 * Photos are prepared in the browser before upload: shrunk (profile photos also cropped to a
 * square) and redrawn the right way up (browsers apply the EXIF orientation when drawing). The
 * upload is then small even from a 48-megapixel phone, and the server only has to clean it
 * (DECISIONS D-054, D-079).
 */
export const MAX_PHOTO_BYTES = 10 * 1024 * 1024
export const AVATAR_UPLOAD_SIDE = 1024
/** Route photos keep their whole frame: the wall, from the floor to the top. */
export const ROUTE_PHOTO_SIDE = 1600
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

/** Size of a whole picture shrunk to fit in `maxSide`, never enlarged, proportions kept. */
export function fitWithin(
  width: number,
  height: number,
  maxSide: number,
): { width: number; height: number } {
  const scale = Math.min(1, maxSide / Math.max(width, height))
  return {
    width: Math.max(1, Math.round(width * scale)),
    height: Math.max(1, Math.round(height * scale)),
  }
}

async function readBitmap(file: File): Promise<ImageBitmap> {
  if (file.size > MAX_PHOTO_BYTES) {
    throw new PhotoError('tooBig')
  }
  try {
    return await createImageBitmap(file)
  } catch {
    throw new PhotoError('unreadable')
  }
}

/** Draws part of a bitmap on a canvas of the given size, as a JPEG. Closes the bitmap. */
function redraw(
  bitmap: ImageBitmap,
  source: { sx: number; sy: number; width: number; height: number },
  target: { width: number; height: number },
): Promise<Blob> {
  const canvas = document.createElement('canvas')
  canvas.width = target.width
  canvas.height = target.height
  const context = canvas.getContext('2d')
  if (context === null) {
    bitmap.close()
    return Promise.reject(new PhotoError('unreadable'))
  }
  context.imageSmoothingQuality = 'high'
  context.drawImage(
    bitmap,
    source.sx,
    source.sy,
    source.width,
    source.height,
    0,
    0,
    target.width,
    target.height,
  )
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

export async function prepareAvatar(file: File): Promise<Blob> {
  const bitmap = await readBitmap(file)
  const crop = squareCrop(bitmap.width, bitmap.height, AVATAR_UPLOAD_SIDE)
  return redraw(
    bitmap,
    { sx: crop.sx, sy: crop.sy, width: crop.side, height: crop.side },
    { width: crop.target, height: crop.target },
  )
}

/** A route or session photo: the whole picture, shrunk to fit in 1600 px, not cropped. */
export async function prepareRoutePhoto(file: File): Promise<Blob> {
  const bitmap = await readBitmap(file)
  return redraw(
    bitmap,
    { sx: 0, sy: 0, width: bitmap.width, height: bitmap.height },
    fitWithin(bitmap.width, bitmap.height, ROUTE_PHOTO_SIDE),
  )
}
