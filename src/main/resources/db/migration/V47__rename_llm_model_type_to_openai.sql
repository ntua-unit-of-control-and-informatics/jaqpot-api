UPDATE model
SET type = 'OPENAI_LLM'
WHERE type = 'DOCKER_LLM';
