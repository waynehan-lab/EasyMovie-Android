#ifndef EASYMOVIE_ANDROID_BUFFER_H
#define EASYMOVIE_ANDROID_BUFFER_H


#include "Shader.h"

class Buffer {
private:
    const char *vertexShaderSource =
            "#version 300 es                                         \n"
            "layout(location = 0) in vec2 a_Position;                \n"
            "layout(location = 1) in vec2 a_TexCoord;                \n"
            "out vec2 v_TexCoord;                                    \n"
            "void main() {                                           \n"
            "   gl_Position = vec4(a_Position, 0.0, 1.0);            \n"
            "   v_TexCoord = a_TexCoord;                             \n"
            "}                                                       \n";

    const char *fragmentShaderSource =
            "#version 300 es                                         \n"
            "#extension GL_OES_EGL_image_external_essl3 : require    \n"
            "precision mediump float;                                \n"
            "in vec2 v_TexCoord;                                     \n"
            "out vec4 fragColor;                                     \n"
            "uniform samplerExternalOES s_Texture;                   \n"
            "void main() {                                           \n"
            "   fragColor = texture(s_Texture, v_TexCoord);          \n"
            "}                                                       \n";

    const float vertices[16] = {
            -1.0f, -1.0f, 0.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 0.0f
    };

    const unsigned int indices[4] = {
            0, 1, 2, 3
    };
    Shader *shader;
    unsigned int FBO;
    unsigned int VAO;
    unsigned int oesTextureId;
    unsigned int unityTextureId;
    int bufferWidth = 0;
    int bufferHeight = 0;
public:
    unsigned int createOESTexture();

    void init(const int width, const int height, const unsigned int textureId);

    void draw();

    void release();
};


#endif //EASYMOVIE_ANDROID_BUFFER_H
