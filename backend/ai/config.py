import os
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    # Model Configuration
    MODEL_NAME: str = "NousResearch/Hermes-4-14B-FP8"
    SERVED_MODEL_NAME: str = "gabby-model"
    
    # Execution Mode: "mock" or "modal"
    # In "mock" mode, a high-quality offline rule-based empathetic chatbot will simulate Hermes responses.
    # In "modal" mode, it will forward LLM requests to your Modal deployment URL.
    EXECUTION_MODE: str = os.getenv("EXECUTION_MODE", "mock")
    
    # Modal URL (populated after modal deployment)
    MODAL_API_URL: str = os.getenv("MODAL_API_URL", "")
    
    # Server host and port
    HOST: str = "127.0.0.1"
    PORT: int = 8000
    
    class Config:
        env_file = ".env"
        extra = "ignore"

settings = Settings()
