package org.jaqpot.api.entity

enum class ModelType {
    // DOCKER models
    DOCKER,

    // LLM
    OPENAI_LLM,
    CUSTOM_LLM,

    SKLEARN_ONNX,

    // torch
    TORCH_GEOMETRIC_ONNX,
    TORCH_ONNX,
    TORCHSCRIPT,

    // R models
    R_BNLEARN_DISCRETE,
    R_CARET,
    R_GBM,
    R_NAIVE_BAYES,
    R_PBPK,
    R_RF,
    R_RPART,
    R_SVM,
    R_TREE_CLASS,
    R_TREE_REGR,

    // QSARTOOLBOX
    QSAR_TOOLBOX_CALCULATOR,
    QSAR_TOOLBOX_QSAR_MODEL,
    QSAR_TOOLBOX_PROFILER,
}
