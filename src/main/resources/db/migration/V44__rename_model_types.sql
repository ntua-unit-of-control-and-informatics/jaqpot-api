UPDATE model
SET type = 'SKLEARN_ONNX'
WHERE type = 'SKLEARN';

UPDATE model
SET type = 'TORCH_GEOMETRIC_ONNX'
WHERE type = 'TORCH_ONNX';