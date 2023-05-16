#include "Buffer.h"
#include <android/log.h>
#include <GLES3/gl3.h>
#include <GLES2/gl2ext.h>
#include "logutils.h"
#include "Shader.h"

unsigned int Buffer::createOESTexture() {
    //创建OES纹理
    glGenTextures(1, &oesTextureId);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, oesTextureId);
    glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
    glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    return oesTextureId;
}

void Buffer::init(const int width, const int height, const unsigned int textureId) {
    bufferWidth = width;
    bufferHeight = height;
    unityTextureId = textureId;
    //创建FBO
    glGenFramebuffers(1, &FBO);
    //创建VAO
    glGenVertexArrays(1, &VAO);
    glBindVertexArray(VAO);
    //创建VBO
    unsigned int VBO;
    glGenBuffers(1, &VBO);
    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);
    //创建EBO
    unsigned int EBO;
    glGenBuffers(1, &EBO);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(indices), indices, GL_STATIC_DRAW);
    // create and compile Shader
    shader = new Shader(vertexShaderSource, fragmentShaderSource);
    shader->use();
    // position attribute
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), (void *) 0);
    glEnableVertexAttribArray(0);
    // uv attribute
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float),
                          (void *) (2 * sizeof(float)));
    glEnableVertexAttribArray(1);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, oesTextureId);
    glUniform1i(glGetUniformLocation(shader->ID, "s_Texture"), 0);
    glBindVertexArray(0);
}

void Buffer::draw() {
    glViewport(0, 0, bufferWidth, bufferHeight);
    glBindFramebuffer(GL_FRAMEBUFFER, FBO);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, unityTextureId, 0);
    glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
    glClear(GL_COLOR_BUFFER_BIT);
    glEnable(GL_CULL_FACE);
    glCullFace(GL_FRONT);
    shader->use();
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, oesTextureId);
    glBindVertexArray(VAO);
    glDrawElements(GL_TRIANGLE_STRIP, 4, GL_UNSIGNED_INT, 0);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0);
    glBindVertexArray(0);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
}

void Buffer::release() {
    glDeleteBuffers(1, &oesTextureId);
    glDeleteVertexArrays(1, &VAO);
    glDeleteFramebuffers(1, &FBO);
    glDeleteProgram(shader->ID);
    delete shader;
}

