import random
import string

def get_random_string(length):
    letters_and_digits = string.ascii_letters + string.digits
    result_str = ''.join(random.choices(letters_and_digits, k=length))
    return result_str