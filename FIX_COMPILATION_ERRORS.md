# Jaqpot API Compilation Errors - Fix Guide

## Current Compilation Errors

The jaqpot-api has 4 compilation errors in `ModelService.kt` related to the new presigned URL download methods:

### Error 1: Method signature mismatch for getModelDownloadUrl
```
'getModelDownloadUrl' overrides nothing. Potential signatures for overriding:
fun getModelDownloadUrl(modelId: Long, expirationMinutes: Int): ResponseEntity<GetModelDownloadUrl200ResponseDto>
```

**Current Implementation** (line 490):
```kotlin
override fun getModelDownloadUrl(modelId: Long, expirationMinutes: Int?): ResponseEntity<GetModelDownloadUrl200ResponseDto>
```

**Expected Implementation**:
```kotlin
override fun getModelDownloadUrl(modelId: Long, expirationMinutes: Int): ResponseEntity<GetModelDownloadUrl200ResponseDto>
```

**Fix**: Change `expirationMinutes: Int?` to `expirationMinutes: Int` and handle default value in OpenAPI spec.

### Error 2: Type mismatch for downloadUrl parameter  
```
Argument type mismatch: actual type is 'String', but 'URI?' was expected.
```

**Current Code** (line 510):
```kotlin
GetModelDownloadUrl200ResponseDto(
    downloadUrl = downloadUrl,  // downloadUrl is String
    expiresAt = expiresAt
)
```

**Fix**: Convert String to URI or update DTO to accept String:
```kotlin
GetModelDownloadUrl200ResponseDto(
    downloadUrl = URI(downloadUrl),  // Convert String to URI
    expiresAt = expiresAt
)
```

### Error 3: Method signature mismatch for getModelPreprocessorDownloadUrl
```
'getModelPreprocessorDownloadUrl' overrides nothing. Potential signatures for overriding:
fun getModelPreprocessorDownloadUrl(modelId: Long, expirationMinutes: Int): ResponseEntity<GetModelPreprocessorDownloadUrl200ResponseDto>
```

**Current Implementation** (line 522):
```kotlin
override fun getModelPreprocessorDownloadUrl(modelId: Long, expirationMinutes: Int?): ResponseEntity<GetModelPreprocessorDownloadUrl200ResponseDto>
```

**Expected Implementation**:
```kotlin
override fun getModelPreprocessorDownloadUrl(modelId: Long, expirationMinutes: Int): ResponseEntity<GetModelPreprocessorDownloadUrl200ResponseDto>
```

**Fix**: Change `expirationMinutes: Int?` to `expirationMinutes: Int`.

### Error 4: Type mismatch for preprocessor downloadUrl parameter
```
Argument type mismatch: actual type is 'String', but 'URI?' was expected.
```

**Current Code** (line 546):
```kotlin
GetModelPreprocessorDownloadUrl200ResponseDto(
    downloadUrl = downloadUrl,  // downloadUrl is String
    expiresAt = expiresAt
)
```

**Fix**: Convert String to URI or update DTO to accept String:
```kotlin
GetModelPreprocessorDownloadUrl200ResponseDto(
    downloadUrl = URI(downloadUrl),  // Convert String to URI
    expiresAt = expiresAt
)
```

## Root Cause Analysis

The errors occur because:

1. **OpenAPI Code Generation**: The generated interfaces expect `Int` but implementation uses `Int?`
2. **Type Mismatches**: Generated DTOs expect `URI?` but `StorageService` methods return `String`

## Required Steps to Fix

### Step 1: Update OpenAPI Specification
Ensure the OpenAPI spec correctly defines optional parameters:

```yaml
# In openapi.yaml for both endpoints
parameters:
  - name: expirationMinutes
    in: query
    required: false  # This should generate Int? not Int
    description: URL expiration time in minutes (default 10, max 60)
    schema:
      type: integer
      minimum: 1
      maximum: 60
      default: 10
```

### Step 2: Regenerate OpenAPI Code
```bash
./gradlew openApiGenerate
```

### Step 3: Fix Type Mismatches in ModelService.kt

**Option A**: Convert String to URI in service methods:
```kotlin
import java.net.URI

// In both methods, change:
GetModelDownloadUrl200ResponseDto(
    downloadUrl = URI(downloadUrl),
    expiresAt = expiresAt
)
```

**Option B**: Update StorageService to return URI instead of String:
```kotlin
// Update StorageService interface methods to return URI
fun getPreSignedModelDownloadUrl(model: Model, expirationMinutes: Int): URI
fun getPreSignedPreprocessorDownloadUrl(model: Model, expirationMinutes: Int): URI
```

### Step 4: Handle Default Values
Since OpenAPI generates non-nullable Int parameters, handle defaults in the service:

```kotlin
override fun getModelDownloadUrl(modelId: Long, expirationMinutes: Int): ResponseEntity<GetModelDownloadUrl200ResponseDto> {
    // Default handling now done in OpenAPI spec or here if needed
    val effectiveExpirationMinutes = expirationMinutes.coerceIn(1, 60)
    // ... rest of implementation
}
```

## Quick Fix Implementation

Here's the immediate fix to get compilation working:

```kotlin
// ModelService.kt - Quick fix for compilation

override fun getModelDownloadUrl(modelId: Long, expirationMinutes: Int): ResponseEntity<GetModelDownloadUrl200ResponseDto> {
    val model = modelRepository.findById(modelId).orElseThrow {
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $modelId not found")
    }

    val modelContentLength = try {
        storageService.readRawModelContentLength(model)
    } catch (e: Exception) {
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model file not found in storage")
    }

    val effectiveExpirationMinutes = expirationMinutes.coerceIn(1, 60)
    
    try {
        val downloadUrl = storageService.getPreSignedModelDownloadUrl(model, effectiveExpirationMinutes)
        val expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(effectiveExpirationMinutes.toLong())
        
        return ResponseEntity.ok(
            GetModelDownloadUrl200ResponseDto(
                downloadUrl = URI(downloadUrl),  // Convert String to URI
                expiresAt = expiresAt
            )
        )
    } catch (e: Exception) {
        logger.error(e) { "Failed to generate presigned download URL for model $modelId" }
        throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate download URL")
    }
}

override fun getModelPreprocessorDownloadUrl(modelId: Long, expirationMinutes: Int): ResponseEntity<GetModelPreprocessorDownloadUrl200ResponseDto> {
    val model = modelRepository.findById(modelId).orElseThrow {
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "Model with id $modelId not found")
    }

    val preprocessor = try {
        storageService.readRawPreprocessor(model)
    } catch (e: Exception) {
        null
    }

    if (preprocessor == null) {
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "Preprocessor not found for model $modelId")
    }

    val effectiveExpirationMinutes = expirationMinutes.coerceIn(1, 60)
    
    try {
        val downloadUrl = storageService.getPreSignedPreprocessorDownloadUrl(model, effectiveExpirationMinutes)
        val expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(effectiveExpirationMinutes.toLong())
        
        return ResponseEntity.ok(
            GetModelPreprocessorDownloadUrl200ResponseDto(
                downloadUrl = URI(downloadUrl),  // Convert String to URI
                expiresAt = expiresAt
            )
        )
    } catch (e: Exception) {
        logger.error(e) { "Failed to generate presigned download URL for preprocessor of model $modelId" }
        throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate download URL")
    }
}
```

## Testing After Fix

1. **Compile**: `./gradlew build`
2. **Run**: `./gradlew bootRun`
3. **Test endpoints**:
   - `GET /v1/models/{id}/download-url?expirationMinutes=30`
   - `GET /v1/models/{id}/preprocessor/download-url?expirationMinutes=30`

## Integration with jaqpotpy

Once these fixes are applied and jaqpot-api is running:

1. **Test jaqpotpy local model download**: 
   ```bash
   cd /path/to/jaqpotpy
   python test_local_model_download.py --model-id <existing-model-id> --local
   ```

2. **Verify presigned URL flow**:
   - Model downloads from S3 via presigned URL
   - Preprocessor downloads from S3 via presigned URL  
   - Fallback to database base64 encoding still works

This will enable the full local model testing workflow needed for the jaqpotpy ↔ jaqpotpy-inference integration.