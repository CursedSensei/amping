#!/usr/bin/env python3
import sys
import os
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.backends import default_backend

def decrypt_vdot_video(encrypted_file_path: str, output_file_path: str):
    """
    Decrypts a VDOT check-in video file encrypted by the mobile patient application.
    
    The encryption scheme is AES-256-CBC with PKCS5Padding.
    The first 16 bytes of the encrypted file represent the Initialization Vector (IV).
    The remaining bytes represent the ciphertext.
    """
    # Standard 16-byte shared key used by the mobile app: "VDOTSecureKey202"
    key = b"VDOTSecureKey202" 
    
    if not os.path.exists(encrypted_file_path):
        print(f"Error: Encrypted file not found at {encrypted_file_path}")
        return False
        
    try:
        with open(encrypted_file_path, "rb") as f:
            payload = f.read()
            
        if len(payload) < 16:
            print("Error: Encrypted file size is too small (less than 16 bytes IV)")
            return False
            
        # 1. Extract the IV (first 16 bytes)
        iv = payload[:16]
        # 2. Extract the ciphertext (everything after)
        ciphertext = payload[16:]
        
        # 3. Setup AES-CBC decryptor
        cipher = Cipher(algorithms.AES(key), modes.CBC(iv), backend=default_backend())
        decryptor = cipher.decryptor()
        
        # 4. Decrypt and remove PKCS5 padding
        decrypted_padded = decryptor.update(ciphertext) + decryptor.finalize()
        
        # Remove PKCS5 padding manually:
        padding_len = decrypted_padded[-1]
        if padding_len < 1 or padding_len > 16:
            print("Error: Invalid padding length detected. Verify key or ciphertext integrity.")
            return False
            
        decrypted_data = decrypted_padded[:-padding_len]
        
        # 5. Write the original, playable .mp4 back to disk!
        with open(output_file_path, "wb") as f_out:
            f_out.write(decrypted_data)
            
        print(f"Success! Video decrypted and saved to: {output_file_path}")
        return True
        
    except Exception as e:
        print(f"Failed to decrypt video: {e}")
        return False

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python decrypt_video.py <encrypted_input_file.enc> <decrypted_output_file.mp4>")
        sys.exit(1)
        
    input_file = sys.argv[1]
    output_file = sys.argv[2]
    decrypt_vdot_video(input_file, output_file)
