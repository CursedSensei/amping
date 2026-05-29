const VIDEO_DECRYPTION_ALGORITHM = 'AES-CBC';
const VIDEO_DECRYPTION_KEY = 'VDOTSecureKey202';
const IV_LENGTH_BYTES = 16;

const keyPromise = crypto.subtle.importKey(
  'raw',
  new TextEncoder().encode(VIDEO_DECRYPTION_KEY),
  VIDEO_DECRYPTION_ALGORITHM,
  false,
  ['decrypt'],
);

async function decryptEncryptedVideoBytes(encryptedBytes: ArrayBuffer): Promise<ArrayBuffer> {
  if (encryptedBytes.byteLength <= IV_LENGTH_BYTES) {
    throw new Error('Encrypted video payload is too small to contain an IV.');
  }

  const payload = new Uint8Array(encryptedBytes);
  const iv = payload.slice(0, IV_LENGTH_BYTES);
  const ciphertext = payload.slice(IV_LENGTH_BYTES);
  const key = await keyPromise;

  return crypto.subtle.decrypt(
    { name: VIDEO_DECRYPTION_ALGORITHM, iv },
    key,
    ciphertext,
  );
}

export async function decryptVideoBlob(videoUrl: string, mimeType = 'video/mp4'): Promise<Blob> {
  const response = await fetch(videoUrl);

  if (!response.ok) {
    throw new Error(`Unable to fetch encrypted video: ${response.status} ${response.statusText}`);
  }

  const decryptedBytes = await decryptEncryptedVideoBytes(await response.arrayBuffer());
  return new Blob([decryptedBytes], { type: mimeType });
}

export async function decryptVideoObjectUrl(videoUrl: string, mimeType = 'video/mp4'): Promise<string> {
  const videoBlob = await decryptVideoBlob(videoUrl, mimeType);
  return URL.createObjectURL(videoBlob);
}

export function revokeVideoObjectUrl(objectUrl: string): void {
  URL.revokeObjectURL(objectUrl);
}