import json, importlib
import os
import django
from django import apps

def export_schema_to_json(schema_class):
    with open(f"schemas/{schema_class.__name__}.json", "w") as f:
        json.dump(schema_class.model_json_schema(), f)

    os.system(f"quicktype -s schema schemas/{schema_class.__name__}.json -o {KOTLIN_OUTPUT_DIR}/{schema_class.__name__}.kt --lang kotlin --framework kotlinx")
    os.system(f"quicktype -s schema schemas/{schema_class.__name__}.json -o {TYPESCRIPT_OUTPUT_DIR}/{schema_class.__name__}.ts --lang typescript --just-types --nice-property-names")
    os.remove(f"schemas/{schema_class.__name__}.json")



KOTLIN_OUTPUT_DIR = os.getenv("KOTLIN_OUTPUT_DIR", "../frontend/patient/app/src/main/java/com/pinghtdog/amping/api_schemas")
TYPESCRIPT_OUTPUT_DIR = os.getenv("TYPESCRIPT_OUTPUT_DIR", "../frontend/hc_professional/src/api_types")

for d in ["schemas", KOTLIN_OUTPUT_DIR, TYPESCRIPT_OUTPUT_DIR]:
    if not os.path.exists(d):
        os.makedirs(d)



os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'Amping.settings')
django.setup()

for app_config in apps.apps.get_app_configs():
    app_name = app_config.name

    try:
        module = importlib.import_module(f'{app_name}.schemas')
    except ModuleNotFoundError:
        continue

    if not hasattr(module, '__ALL__'):
        continue

    for schema_name in module.__ALL__:
        schema_class = getattr(module, schema_name)
        export_schema_to_json(schema_class)

try:
    module =importlib.import_module('Amping.schemas')

    if hasattr(module, '__ALL__'):
        for schema_name in module.__ALL__:
            schema_class = getattr(module, schema_name)
            export_schema_to_json(schema_class)
except:
    pass