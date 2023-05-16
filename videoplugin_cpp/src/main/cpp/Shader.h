#ifndef VRSHELLNATIVE_SHADER_H
#define VRSHELLNATIVE_SHADER_H

#include <string>

class Shader {
public:
    unsigned int ID; //Shader Program ID
    Shader(const char *vertexSource = "", const char *fragmentSource = "");

    ~Shader();

    void use(); //Use Shader program
private:
    void checkCompileErrors(unsigned int id, const std::string &type);
};


#endif //VRSHELLNATIVE_SHADER_H
