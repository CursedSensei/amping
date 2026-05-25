import json, importlib
import os
import django

KOTLIN_OUTPUT_DIR = os.getenv("KOTLIN_OUTPUT_DIR", "../frontend/patient/app/src/main/java/com/pinghtdog/amping/api_types")
TYPESCRIPT_OUTPUT_DIR = os.getenv("TYPESCRIPT_OUTPUT_DIR", "../frontend/hc_professional/src/api_schemas")


if not os.path.exists("schemas"):
    os.makedirs("schemas")

if not os.path.exists(KOTLIN_OUTPUT_DIR):
    os.makedirs(KOTLIN_OUTPUT_DIR)

if not os.path.exists(TYPESCRIPT_OUTPUT_DIR):
    os.makedirs(TYPESCRIPT_OUTPUT_DIR)



os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'Amping.settings')
django.setup()

from users.schemas import __ALL__
module = importlib.import_module('users.schemas')

for schema_name in __ALL__:
    schema_class = getattr(module, schema_name)

    with open(f"schemas/{schema_class.__name__}.json", "w") as f:
        json.dump(schema_class.model_json_schema(), f, indent=4)

    os.system(f"quicktype -s schema schemas/{schema_class.__name__}.json -o {KOTLIN_OUTPUT_DIR}/{schema_class.__name__}.kt --lang kotlin --framework kotlinx")
    os.system(f"quicktype -s schema schemas/{schema_class.__name__}.json -o {TYPESCRIPT_OUTPUT_DIR}/{schema_class.__name__}.ts --lang typescript --just-types --nice-property-names")
    os.remove(f"schemas/{schema_class.__name__}.json")