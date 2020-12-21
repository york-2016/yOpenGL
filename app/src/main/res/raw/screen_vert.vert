attribute vec4 vPosition;
attribute vec2 fPosition;
varying vec2 ft_Position;

void main(){
    gl_Position = vPosition;
    ft_Position = fPosition;
}