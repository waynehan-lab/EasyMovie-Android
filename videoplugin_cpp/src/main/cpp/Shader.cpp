#include "Shader.h"
#include <GLES3/gl3.h>
#include <string>
#include <iostream>
#include "logutils.h"

Shader::Shader(const char *vertexSource, const char *fragmentSource) {
    unsigned int vertexHandle, fragmentHandle;

    vertexHandle = glCreateShader(GL_VERTEX_SHADER);
    glShaderSource(vertexHandle, 1, &vertexSource, nullptr);
    glCompileShader(vertexHandle);
    checkCompileErrors(vertexHandle, "VERTEX");

    fragmentHandle = glCreateShader(GL_FRAGMENT_SHADER);
    glShaderSource(fragmentHandle, 1, &fragmentSource, nullptr);
    glCompileShader(fragmentHandle);
    checkCompileErrors(fragmentHandle, "FRAGMENT");

    ID = glCreateProgram();
    glAttachShader(ID, vertexHandle);
    glAttachShader(ID, fragmentHandle);
    glLinkProgram(ID);
    checkCompileErrors(ID, "PROGRAM");

    glDeleteShader(vertexHandle);
    glDeleteShader(fragmentHandle);
}

Shader::~Shader() {

}

void Shader::use() {
    glUseProgram(ID);
}

void Shader::checkCompileErrors(unsigned int id, const std::string &type) {
    int success;
    char infoLog[1024];
    if (type != "PROGRAM") {
        glGetShaderiv(id, GL_COMPILE_STATUS, &success);
        if (!success) {
            glGetShaderInfoLog(id, 1024, nullptr, infoLog);
            LOGI("SHADER_COMPILATION_ERROR of type: %s, info: %s", type.c_str(), infoLog);
        } else {
            LOGI("SHADER_COMPILATION_SUCCESS of type: %s", type.c_str());
        }
    } else {
        glGetProgramiv(id, GL_LINK_STATUS, &success);
        if (!success) {
            glGetProgramInfoLog(id, 1024, nullptr, infoLog);
            LOGI("PROGRAM_LINKING_ERROR of type: %s, info: %s", type.c_str(), infoLog);
        } else {
            LOGI("PROGRAM_LINKING_SUCCESS of type: %s", type.c_str());
        }
    }
}
