attribute vec4 vPosition;
attribute vec2 fPosition;
varying vec2 ft_Position;
uniform mat4 u_Matrix;
void main(){
    gl_Position = vPosition * u_Matrix;
    ft_Position = fPosition;
}